<!--
  Design.vue — AI爆款设计选款页（核心页面）

  完整用户路径：
    进入 → 看到"今日剩余N次" → 点生成 → loading → 看图 → 点选图
    → 再生成 → 用完 → 弹弹窗 → 支付 → 继续生成
-->
<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { createGenerateTask, getTaskResult, selectImage, getQuotaInfo } from '@/api'
import PaywallModal from './PaywallModal.vue'
import { initUserId } from '@/utils/user'

// ── State ──────────────────────────────────────────────────────────────
const USER_ID = initUserId()

const category   = ref('外套')
const style      = ref('工装')
const market     = ref('欧美')
const images     = ref([])
const selected   = ref(null)
const loading    = ref(false)
const loadingMsg = ref('正在分析爆款趋势…')
const error      = ref('')

// Quota
const remainFree  = ref(3)
const remainPaid  = ref(0)
const paywallShow = ref(false)
const paywallMsg  = ref('')
const paywallPlans = ref([])
const generateCount = ref(0)  // 连续点击计数，≥2 次时提前触发弹窗

// Poll
let pollTimer = null
const MAX_POLL_ATTEMPTS = 60 // 60s 最大等待时间

const totalRemain = computed(() => remainFree.value + remainPaid.value)
const quotaLabel  = computed(() => {
  if (totalRemain.value <= 0) return '已用完，解锁更多爆款 👇'
  if (remainFree.value > 0)   return `今日剩余：${remainFree.value} 次免费`
  return `付费剩余：${remainPaid.value} 次`
})
const quotaClass = computed(() => totalRemain.value <= 0 ? 'quota-empty' : totalRemain.value <= 1 ? 'quota-low' : 'quota-ok')

const CATEGORIES = ['外套', '连衣裙', '裤子', '上衣', '内衣', '运动']
const STYLES     = ['工装', '欧美', '韩系', '极简', '性感', '休闲']

// ── Init ──────────────────────────────────────────────────────────────
onMounted(async () => {
  try {
    const info = await getQuotaInfo(USER_ID)
    remainFree.value = info?.remainFree ?? 3
    remainPaid.value = info?.remainPaid ?? 0
  } catch (_) {}
})
onUnmounted(() => clearInterval(pollTimer))

// ── Generate ──────────────────────────────────────────────────────────
async function generate() {
  // 连续点击 ≥2 次 且 剩余次数 ≤1 → 提前弹窗提示购买
  generateCount.value++
  if (generateCount.value >= 2 && totalRemain.value <= 1) {
    openPaywall('再生成1次就能找到爆款 — 解锁更多次数吧！')
    return
  }

  if (loading.value) return
  loading.value = true
  error.value   = ''
  images.value  = []
  selected.value = null
  loadingMsg.value = 'AI正在分析1688/TikTok/Shein爆款趋势…'

  try {
    const res = await createGenerateTask(USER_ID, category.value, style.value, market.value, null)

    // 配额超限 → 弹窗
    if (res?.exceeded || res?.code === 'NO_QUOTA' || res?.code === 402) {
      loading.value = false
      openPaywall(res.message || '今日免费次数已用完', res.plans)
      return
    }

    // 更新剩余配额
    if (res?.quota) {
      remainFree.value = res.quota.remainFree ?? remainFree.value
      remainPaid.value = res.quota.remainPaid ?? remainPaid.value
    }

    loadingMsg.value = '正在生成10款当前热卖风格…'
    startPoll(res.taskId)
  } catch (e) {
    loading.value = false
    error.value = '生成失败，请稍后重试'
  }
}

function startPoll(taskId) {
  let attempts = 0
  pollTimer = setInterval(async () => {
    attempts++
    if (attempts > MAX_POLL_ATTEMPTS) {
      clearInterval(pollTimer)
      loading.value = false
      error.value = '生成超时，请重试'
      return
    }
    try {
      const res = await getTaskResult(taskId)
      if (res?.status === 'success') {
        clearInterval(pollTimer)
        images.value  = res.images || []
        loading.value = false
        generateCount.value = 0
        loadingMsg.value = ''
      } else if (res?.status === 'failed') {
        clearInterval(pollTimer)
        loading.value = false
        error.value = res.error || '生成失败，请重试'
      }
      // pending / running → 继续等待
    } catch (_) {}
  }, 1000)
}

// ── Select image ──────────────────────────────────────────────────────
async function onSelect(img) {
  selected.value = img
  try {
    await selectImage(USER_ID, img, images.value, null, category.value, style.value)
  } catch (_) {}
}

// ── Paywall ───────────────────────────────────────────────────────────
function openPaywall(msg, plans) {
  paywallMsg.value   = msg  || '今日免费次数已用完'
  paywallPlans.value = plans || []
  paywallShow.value  = true
}
</script>

<template>
  <div class="page">
    <!-- Header -->
    <header class="header">
      <h1>Deepay · AI爆款设计</h1>
      <!-- 配额栏（STEP 43） -->
      <div class="quota-bar" :class="quotaClass" @click="totalRemain <= 0 && openPaywall()">
        {{ quotaLabel }}
      </div>
    </header>

    <!-- 筛选区 -->
    <section class="filters">
      <div class="filter-group">
        <label>品类</label>
        <div class="chips">
          <span
            v-for="c in CATEGORIES" :key="c"
            class="chip" :class="{ active: category === c }"
            @click="category = c"
          >{{ c }}</span>
        </div>
      </div>
      <div class="filter-group">
        <label>风格</label>
        <div class="chips">
          <span
            v-for="s in STYLES" :key="s"
            class="chip" :class="{ active: style === s }"
            @click="style = s"
          >{{ s }}</span>
        </div>
      </div>
    </section>

    <!-- 生成按钮 -->
    <button
      class="generate-btn"
      :disabled="loading"
      @click="generate"
    >
      <span v-if="loading">{{ loadingMsg }}</span>
      <span v-else>生成10款「{{ category }}·{{ style }}」热卖爆款</span>
    </button>

    <!-- 错误提示 -->
    <p v-if="error" class="error">{{ error }}</p>

    <!-- Loading skeleton -->
    <div v-if="loading" class="grid skeleton-grid">
      <div v-for="n in 10" :key="n" class="skeleton" />
    </div>

    <!-- 图片网格 -->
    <div v-if="images.length" class="grid">
      <div
        v-for="img in images" :key="img"
        class="img-wrap"
        :class="{ 'img-selected': selected === img }"
        @click="onSelect(img)"
      >
        <img :src="img" :alt="`${category} ${style}`" loading="lazy" />
        <div v-if="selected === img" class="selected-badge">✓ 已选</div>
      </div>
    </div>

    <!-- 空态 -->
    <div v-if="!loading && !images.length && !error" class="empty">
      <p>AI已分析1688 / TikTok / Shein 实时爆款趋势</p>
      <p>点击上方按钮，立即生成专属款式 👆</p>
    </div>

    <!-- 弹窗 -->
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
.page { max-width: 480px; margin: 0 auto; padding: 0 16px 80px; }

/* Header */
.header { padding: 20px 0 12px; display: flex; justify-content: space-between; align-items: center; }
.header h1 { font-size: 18px; font-weight: 800; }

.quota-bar {
  font-size: 13px; font-weight: 600;
  padding: 5px 12px; border-radius: 20px; cursor: pointer;
}
.quota-ok    { background: #dcfce7; color: #166534; }
.quota-low   { background: #fef9c3; color: #854d0e; }
.quota-empty { background: #fee2e2; color: #991b1b; }

/* Filters */
.filters { margin-bottom: 16px; }
.filter-group { margin-bottom: 10px; }
.filter-group label { font-size: 12px; color: #666; margin-bottom: 6px; display: block; font-weight: 600; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  background: #f3f4f6; color: #374151;
  padding: 6px 14px; border-radius: 20px;
  font-size: 13px; font-weight: 500; cursor: pointer;
  transition: background .15s, color .15s;
}
.chip.active { background: #4f46e5; color: #fff; }

/* Generate button */
.generate-btn {
  width: 100%;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  font-size: 16px; font-weight: 700;
  padding: 18px;
  border-radius: 14px;
  margin-bottom: 20px;
  line-height: 1.3;
}

/* Error */
.error { color: #dc2626; font-size: 14px; text-align: center; margin-bottom: 16px; }

/* Grid */
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.img-wrap {
  position: relative; border-radius: 12px; overflow: hidden;
  cursor: pointer; aspect-ratio: 3/4;
  border: 3px solid transparent;
  transition: border-color .2s, transform .15s;
}
.img-wrap:hover    { transform: scale(1.02); }
.img-wrap img      { width: 100%; height: 100%; object-fit: cover; display: block; }
.img-selected      { border-color: #4f46e5; }
.selected-badge {
  position: absolute; bottom: 8px; left: 50%; transform: translateX(-50%);
  background: #4f46e5; color: #fff;
  font-size: 12px; font-weight: 700;
  padding: 3px 12px; border-radius: 12px;
}

/* Skeleton */
.skeleton-grid .skeleton {
  aspect-ratio: 3/4; border-radius: 12px;
  background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer { to { background-position: -200% 0; } }

/* Empty */
.empty {
  text-align: center; padding: 40px 20px; color: #9ca3af; line-height: 2;
}
</style>
