<!--
  Generate.vue — AI一键开店（全链路版）
  路径：/generate?prompt=...

  流程：
    ① 选品类 / 风格 / 市场
    ② 点击生成 → 出图（逐张渐显）
    ③ 选图 → AI生成商品信息（标题 + 卖点 + 价格 + 预估收益）
    ④ 可选：AI优化文案
    ⑤ 一键开店 → 跳转店铺页

  规则：style chips 只改内容，不改模板风格；AI prompt 锁定风格规则
-->
<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createGenerateTask, getTaskResult, selectImage, getQuotaInfo } from '@/api/design'
import { generateProduct, generateShop, optimizeCopy } from '@/api/ai'
import { createShop } from '@/api/shop'
import PaywallModal from './PaywallModal.vue'
import { initUserId } from '@/utils/user'

const route  = useRoute()
const router = useRouter()

const USER_ID = initUserId()

// ── 筛选项 ──────────────────────────────────────────────────────────────
const CATEGORIES = ['外套', '连衣裙', '裤子', '上衣', '运动', '内衣']
const STYLES     = ['欧美', '工装', '韩系', '极简', '性感', '休闲']
const MARKETS    = ['欧美', '东南亚', '国内', '中东']

const category = ref('外套')
const style    = ref('欧美')
const market   = ref('欧美')

// ── AI修改 chips（只改内容，不改模板风格）───────────────────────────────
const STYLE_CHIPS = [
  { label: '红点缀',  value: 'red accent details' },
  { label: '白LOGO',  value: 'white logo print' },
  { label: '图案更大', value: 'bigger graphic print' },
  { label: '极简',    value: 'minimalist clean design' },
  { label: '高级感',  value: 'luxury premium feel' },
  { label: '街头风',  value: 'urban streetwear style' },
]

const extraPrompt  = ref('')
const appliedChips = ref([])

function updateStyle(chip) {
  if (appliedChips.value.length >= 3) {
    appliedChips.value = []
    extraPrompt.value  = ''
  }
  if (!appliedChips.value.includes(chip.label)) {
    appliedChips.value.push(chip.label)
    extraPrompt.value += ` ${chip.value},`
  }
  generate()
}

function resetStyle() {
  extraPrompt.value  = ''
  appliedChips.value = []
}

// ── 状态 ──────────────────────────────────────────────────────────────
const images      = ref([])
const selected    = ref(null)
const loading     = ref(false)
const loadingMsg  = ref('')
const error       = ref('')

// ── 配额 ──────────────────────────────────────────────────────────────
const remainFree   = ref(3)
const remainPaid   = ref(0)
const paywallShow  = ref(false)
const paywallMsg   = ref('')
const paywallPlans = ref([])
const clickCount   = ref(0)

const totalRemain     = computed(() => remainFree.value + remainPaid.value)
const quotaLabel      = computed(() => {
  if (totalRemain.value <= 0) return '次数已用完 👇'
  if (remainFree.value > 0)  return `免费剩余 ${remainFree.value} 次`
  return `付费剩余 ${remainPaid.value} 次`
})
const quotaBadgeClass = computed(() =>
  totalRemain.value <= 0  ? 'badge-empty' :
  totalRemain.value <= 1  ? 'badge-low'   : 'badge-ok'
)

// ── 轮询 ──────────────────────────────────────────────────────────────
let pollTimer = null
const MAX_POLL = 60

onMounted(async () => {
  if (route.query.prompt) {
    const p = decodeURIComponent(route.query.prompt)
    extraPrompt.value = p + ','
  }
  try {
    const info = await getQuotaInfo(USER_ID)
    remainFree.value = info?.remainFree ?? 3
    remainPaid.value = info?.remainPaid ?? 0
  } catch (_) {}
})
onUnmounted(() => clearInterval(pollTimer))

// ── 生成图片 ──────────────────────────────────────────────────────────
async function generate() {
  clickCount.value++

  if (clickCount.value >= 2 && totalRemain.value <= 1) {
    openPaywall('再一次就能找到爆款 — 解锁更多次数！')
    return
  }

  if (loading.value) return
  loading.value    = true
  error.value      = ''
  images.value     = []
  selected.value   = null
  productInfo.value = null
  loadingMsg.value = 'AI正在分析1688 / TikTok趋势…'

  try {
    const res = await createGenerateTask(
      USER_ID, category.value, style.value, market.value,
      extraPrompt.value || null
    )

    if (res?.exceeded || res?.code === 'NO_QUOTA' || res?.code === 402) {
      loading.value = false
      openPaywall(res?.message || '今日免费次数已用完', res?.plans)
      return
    }

    if (res?.quota) {
      remainFree.value = res.quota.remainFree ?? remainFree.value
      remainPaid.value = res.quota.remainPaid ?? remainPaid.value
    }

    loadingMsg.value = '正在生成10款热卖款式…'
    startPoll(res.taskId)
  } catch {
    loading.value = false
    error.value   = '生成失败，请稍后重试'
  }
}

function startPoll(taskId) {
  let attempts = 0
  pollTimer = setInterval(async () => {
    if (++attempts > MAX_POLL) {
      clearInterval(pollTimer)
      loading.value = false
      error.value   = '生成超时，请重试'
      return
    }
    try {
      const res = await getTaskResult(taskId)
      if (res?.status === 'success') {
        clearInterval(pollTimer)
        images.value   = res.images || []
        loading.value  = false
        clickCount.value = 0
        if (remainFree.value > 0) remainFree.value--
        else if (remainPaid.value > 0) remainPaid.value--
      } else if (res?.status === 'failed') {
        clearInterval(pollTimer)
        loading.value = false
        error.value   = res.error || '生成失败，请重试'
      }
    } catch (_) {}
  }, 1000)
}

// ── 选图 → 触发 AI 商品生成 ──────────────────────────────────────────
const productInfo    = ref(null)   // AI生成的商品信息
const productLoading = ref(false)
const productError   = ref('')

// 当后端不可用时使用的 mock fallback
const MOCK_PRODUCT = {
  title:   null,   // 用 category + style 动态填
  points:  ['采用优质面料，手感柔软舒适', '版型修身，显瘦提气质', '百搭风格，全场合适用'],
  price:   29.99,
  tags:    [],
  estimatedMonthlyEarn: 120,
}

async function onSelect(img) {
  selected.value   = img
  productInfo.value = null
  productError.value = ''

  try {
    await selectImage(USER_ID, img, images.value, null, category.value, style.value)
  } catch (_) {}

  // 调用 AI 商品生成
  productLoading.value = true
  try {
    const res = await generateProduct({
      image:    img,
      category: category.value,
      style:    style.value,
      market:   market.value,
      prompt:   extraPrompt.value || undefined,
    })
    productInfo.value = res
  } catch (_) {
    // 后端未接时用 mock，保证前端可展示
    productInfo.value = {
      ...MOCK_PRODUCT,
      title: `${category.value} · ${style.value} 爆款`,
      tags:  [category.value, style.value, market.value],
    }
  } finally {
    productLoading.value = false
  }
}

// ── AI优化文案 ────────────────────────────────────────────────────────
const optimizing = ref(false)

async function doOptimize() {
  if (!productInfo.value || optimizing.value) return
  optimizing.value = true
  try {
    const res = await optimizeCopy({
      title:  productInfo.value.title,
      points: productInfo.value.points,
      price:  productInfo.value.price,
      style:  style.value,
    })
    productInfo.value = { ...productInfo.value, ...res }
  } catch (_) {
    // 静默失败：mock 优化（在标题前加"热销"）
    productInfo.value = {
      ...productInfo.value,
      title: `热销 · ${productInfo.value.title}`,
      points: productInfo.value.points.map((p, i) =>
        i === 0 ? `⭐ ${p}` : p
      ),
    }
  } finally {
    optimizing.value = false
  }
}

// ── 一键开店 ─────────────────────────────────────────────────────────
const creating = ref(false)

async function createFromAI() {
  if (!selected.value || creating.value) return
  creating.value = true
  try {
    // 尝试 AI 完整店铺生成（有后端时增强）
    let shopExtra = {}
    try {
      shopExtra = await generateShop({
        category: category.value,
        style:    style.value,
        market:   market.value,
        image:    selected.value,
        productInfo: productInfo.value,
      })
    } catch (_) {}   // 不阻断流程

    const price = productInfo.value?.price ?? 29.99
    const title = productInfo.value?.title ?? `${category.value} · ${style.value}`

    const { shopId } = await createShop({
      templateId: 'minimal',
      type:       'ai',
      name:       shopExtra.shopName || title,
      image:      selected.value,
      banner:     shopExtra.banner  || selected.value,
      category:   category.value,
      style:      style.value,
      theme:      shopExtra.theme   || defaultTheme(),
      gradient:   shopExtra.gradient || 'linear-gradient(135deg,#0f2d1a,#111)',
      products:   shopExtra.products || [{
        title: title,
        img:   selected.value,
        price: price,
        points: productInfo.value?.points ?? [],
        tags:   productInfo.value?.tags   ?? [],
      }],
    })
    router.push(`/shop/${shopId}`)
  } catch (e) {
    error.value = '开店失败，请稍后重试'
  } finally {
    creating.value = false
  }
}

function defaultTheme() {
  return {
    bg: '#0B0B0B', card: '#111111', border: '#1A1A1A',
    text: '#FFFFFF', subText: '#9CA3AF', primary: '#00FF88',
  }
}

// ── 弹窗 ──────────────────────────────────────────────────────────────
function openPaywall(msg, plans) {
  paywallMsg.value   = msg   || '今日免费次数已用完'
  paywallPlans.value = plans || []
  paywallShow.value  = true
}
</script>

<template>
  <div class="min-h-screen bg-bg text-white">

    <!-- ── 顶部导航 ─────────────────────────────────────────── -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md
                   border-b border-border px-4 py-3
                   flex items-center justify-between">
      <button class="text-muted active:text-white transition-colors"
              @click="router.back()">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="flex flex-col items-center">
        <span class="font-semibold text-sm tracking-wide">AI一键开店</span>
        <span class="text-[10px]" style="color:#00FF88">输入想法 → 自动赚钱</span>
      </div>
      <button
        :class="['text-xs font-semibold px-3 py-1.5 rounded-full', quotaBadgeClass]"
        @click="totalRemain <= 0 && openPaywall()"
      >
        {{ quotaLabel }}
      </button>
    </header>

    <!-- ── 信任背书 banner ──────────────────────────────────── -->
    <div class="bg-surface2 border-b border-border px-4 py-2
                flex items-center justify-around text-center">
      <div>
        <p class="text-[10px] text-muted">已有用户赚了</p>
        <p class="text-xs font-bold" style="color:#00FF88">€320+</p>
      </div>
      <div class="w-px h-6 bg-border" />
      <div>
        <p class="text-[10px] text-muted">平均月收益</p>
        <p class="text-xs font-bold" style="color:#00FF88">€120</p>
      </div>
      <div class="w-px h-6 bg-border" />
      <div>
        <p class="text-[10px] text-muted">邀请好友额外得</p>
        <p class="text-xs font-bold" style="color:#00FF88">€2 / 人</p>
      </div>
    </div>

    <!-- ── 主内容 ─────────────────────────────────────────── -->
    <div class="max-w-[480px] mx-auto px-4 pb-10">

      <!-- 品类筛选 -->
      <section class="mt-5 mb-3">
        <p class="text-[11px] text-muted font-semibold uppercase tracking-widest mb-2.5">品类</p>
        <div class="flex gap-2 overflow-x-auto pb-1 scrollbar-hide">
          <button
            v-for="c in CATEGORIES" :key="c"
            :class="['chip shrink-0', category === c ? 'chip-active' : '']"
            @click="category = c; selected = null; productInfo = null"
          >{{ c }}</button>
        </div>
      </section>

      <!-- 风格筛选 -->
      <section class="mb-5">
        <p class="text-[11px] text-muted font-semibold uppercase tracking-widest mb-2.5">风格</p>
        <div class="flex gap-2 overflow-x-auto pb-1 scrollbar-hide">
          <button
            v-for="s in STYLES" :key="s"
            :class="['chip shrink-0', style === s ? 'chip-active' : '']"
            @click="style = s; selected = null; productInfo = null"
          >{{ s }}</button>
        </div>
      </section>

      <!-- 生成按钮 -->
      <button
        :disabled="loading"
        class="bg-accent text-black font-bold h-14 w-full rounded-2xl mb-2
               text-sm tracking-wide active:scale-95 transition-transform duration-100
               disabled:opacity-50 disabled:cursor-not-allowed
               flex items-center justify-center gap-2"
        @click="generate"
      >
        <svg v-if="loading" class="animate-spin h-4 w-4 shrink-0"
             viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor"
                  stroke-opacity=".3" stroke-width="3"/>
          <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                stroke-width="3" stroke-linecap="round"/>
        </svg>
        <span v-if="loading">{{ loadingMsg }}</span>
        <span v-else>🚀 生成10款「{{ category }} · {{ style }}」爆款</span>
      </button>

      <!-- 转化率暗示 -->
      <p class="text-center text-xs text-muted mb-5">
        这个风格转化率高 · 预计月收益 <span style="color:#00FF88">€80~€200</span>
      </p>

      <!-- 错误 -->
      <p v-if="error" class="text-danger text-sm text-center mb-5">{{ error }}</p>

      <!-- 空状态 -->
      <div v-if="!loading && !images.length && !error"
           class="text-center py-16 fade-in">
        <p class="text-4xl mb-4">✨</p>
        <p class="text-white font-semibold text-lg mb-2">AI已分析爆款趋势</p>
        <p class="text-muted text-sm leading-loose">
          基于 1688 · TikTok · Shein 实时数据<br>
          点击上方按钮，立即生成专属款式
        </p>
        <p class="mt-4 text-xs" style="color:#00FF88">
          👆 已有用户靠这个赚到 €320+
        </p>
      </div>

      <!-- 骨架屏 -->
      <div v-if="loading" class="grid grid-cols-2 gap-3 animate-pulse">
        <div v-for="i in 10" :key="i" class="aspect-[3/4] rounded-2xl bg-surface2" />
      </div>

      <!-- ── 图片网格 ────────────────────────────────────── -->
      <div v-if="images.length">

        <!-- AI风格修改 chips -->
        <div class="mb-4">
          <div class="flex items-center justify-between mb-2">
            <p class="text-[11px] text-muted font-semibold uppercase tracking-widest">
              调整风格
            </p>
            <button
              v-if="appliedChips.length"
              class="text-xs text-muted underline active:opacity-60"
              @click="resetStyle(); generate()"
            >
              重置
            </button>
          </div>
          <div class="flex gap-2 overflow-x-auto pb-1 scrollbar-hide">
            <button
              v-for="chip in STYLE_CHIPS" :key="chip.label"
              :class="[
                'chip shrink-0 text-xs',
                appliedChips.includes(chip.label) ? 'chip-active' : ''
              ]"
              @click="updateStyle(chip)"
            >
              {{ chip.label }}
            </button>
            <button
              class="chip shrink-0 text-xs"
              @click="resetStyle(); generate()"
            >
              🔄 再生成
            </button>
          </div>
        </div>

        <!-- 图片瀑布 -->
        <div class="grid grid-cols-2 gap-3">
          <div
            v-for="(img, idx) in images" :key="img"
            :class="['img-card img-in', selected === img ? 'img-card-selected' : '']"
            :style="{ animationDelay: `${idx * 0.06}s` }"
            @click="onSelect(img)"
          >
            <img :src="img" :alt="`${category} ${style}`"
                 loading="lazy" class="w-full h-full object-cover" />
            <div v-if="selected === img"
                 class="absolute bottom-2.5 left-1/2 -translate-x-1/2
                        bg-accent text-black text-xs font-bold
                        px-3 py-1 rounded-full whitespace-nowrap">
              ✓ 已选
            </div>
          </div>
        </div>

        <!-- ── AI商品信息卡（选图后出现）────────────────── -->
        <div v-if="selected" class="mt-5 fade-in space-y-4">

          <!-- 加载中 -->
          <div v-if="productLoading"
               class="card p-4 flex items-center gap-3 animate-pulse">
            <div class="w-14 h-14 rounded-xl bg-surface2 shrink-0" />
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-surface2 rounded w-3/4" />
              <div class="h-3 bg-surface2 rounded w-1/2" />
              <div class="h-3 bg-surface2 rounded w-2/3" />
            </div>
          </div>

          <!-- 商品信息卡 -->
          <div v-else-if="productInfo" class="card p-4 rounded-2xl">

            <!-- 头部：图 + 标题 + 价格 -->
            <div class="flex gap-3 mb-4">
              <img :src="selected"
                   class="w-16 h-16 rounded-xl object-cover shrink-0" />
              <div class="flex-1 min-w-0">
                <p class="font-bold text-sm leading-snug mb-1">
                  {{ productInfo.title }}
                </p>
                <div class="flex items-center gap-2">
                  <span class="text-lg font-black" style="color:#00FF88">
                    €{{ Number(productInfo.price).toFixed(2) }}
                  </span>
                  <span class="text-xs text-muted line-through">
                    €{{ (productInfo.price * 1.4).toFixed(2) }}
                  </span>
                </div>
                <!-- 预估收益 -->
                <p class="text-[10px] mt-1" style="color:#00FF88">
                  预计月赚 €{{ productInfo.estimatedMonthlyEarn ?? Math.round(productInfo.price * 0.1 * 40) }}
                </p>
              </div>
            </div>

            <!-- 卖点 -->
            <div class="space-y-1.5 mb-4">
              <p class="text-[10px] text-muted uppercase tracking-widest mb-1">核心卖点</p>
              <div
                v-for="(pt, i) in (productInfo.points || []).slice(0, 3)"
                :key="i"
                class="flex items-start gap-2 text-xs"
              >
                <span style="color:#00FF88">✓</span>
                <span>{{ pt }}</span>
              </div>
            </div>

            <!-- 标签 -->
            <div v-if="productInfo.tags?.length"
                 class="flex flex-wrap gap-1.5 mb-4">
              <span
                v-for="t in productInfo.tags" :key="t"
                class="text-[10px] px-2 py-0.5 rounded-full bg-surface2 text-muted"
              >
                #{{ t }}
              </span>
            </div>

            <!-- 操作行 -->
            <div class="flex gap-2">
              <!-- AI优化 -->
              <button
                :disabled="optimizing"
                class="flex-1 h-10 rounded-xl border border-border text-xs font-semibold
                       text-muted active:opacity-80 transition-opacity
                       disabled:opacity-50 disabled:cursor-not-allowed
                       flex items-center justify-center gap-1"
                @click="doOptimize"
              >
                <svg v-if="optimizing" class="animate-spin h-3 w-3 shrink-0"
                     viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor"
                          stroke-opacity=".3" stroke-width="3"/>
                  <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                        stroke-width="3" stroke-linecap="round"/>
                </svg>
                <span>{{ optimizing ? 'AI优化中…' : '✨ AI优化文案' }}</span>
              </button>

              <!-- 一键开店 -->
              <button
                :disabled="creating"
                class="flex-1 h-10 rounded-xl font-bold text-sm
                       active:scale-95 transition-transform duration-100
                       disabled:opacity-50 disabled:cursor-not-allowed
                       flex items-center justify-center gap-1"
                style="background:#00FF88;color:#000"
                @click="createFromAI"
              >
                <svg v-if="creating" class="animate-spin h-3.5 w-3.5 shrink-0"
                     viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor"
                          stroke-opacity=".3" stroke-width="3"/>
                  <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                        stroke-width="3" stroke-linecap="round"/>
                </svg>
                <span>{{ creating ? '开店中…' : '🚀 一键开店' }}</span>
              </button>
            </div>

          </div>

          <!-- 邀请好友提示 -->
          <div class="text-center text-xs py-2 border border-dashed border-border rounded-xl"
               style="color:#00FF8899">
            邀请好友下单得 €2 · 分享越多赚越多 →
            <button class="underline ml-1" style="color:#00FF88"
                    @click="router.push('/invite')">
              立即邀请
            </button>
          </div>

        </div><!-- /selected -->

      </div><!-- /images -->

    </div><!-- /max-w -->

    <!-- 付费弹窗 -->
    <PaywallModal
      :show="paywallShow"
      :userId="USER_ID"
      :message="paywallMsg"
      :plans="paywallPlans"
      @close="paywallShow = false"
    />

  </div>
</template>

<style scoped>
.fade-in {
  animation: fadeUp .5s ease both;
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
