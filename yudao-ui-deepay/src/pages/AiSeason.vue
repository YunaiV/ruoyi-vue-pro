<!--
  AiSeason.vue — 整季系列生成
  路径：/ai/season
-->
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { generateSeason, createStyleProfile, designScore, saveDesign } from '@/api/ai'
import { initUserId } from '@/utils/user'

const router = useRouter()
const route  = useRoute()
initUserId()

// ── 品牌设置 ──────────────────────────────────────────────────────────
const brandName   = ref('')
const brandStyle  = ref('minimal')
const brandColors = ref(['黑','白','灰'])
const brandAvoid  = ref(['花纹','logo'])

const STYLE_OPTIONS = [
  { key: 'minimal', label: '极简' },
  { key: 'luxury',  label: '高端' },
  { key: 'modern',  label: '现代' },
  { key: 'avant',   label: '前卫' },
]

// ── 参考图槽位 ────────────────────────────────────────────────────────
const orderedRefs = ref([null, null, null])

const REF_SLOTS = [
  { label: '版型参考', hint: '廓形结构', icon: '◻' },
  { label: '风格参考', hint: '整体感觉', icon: '✦' },
  { label: '细节参考', hint: '局部元素', icon: '⋯' },
]

// ── 细节控制 ──────────────────────────────────────────────────────────
const CONTROLS_DEF = [
  {
    key: 'neck', label: '领口', icon: '🔵',
    options: [
      { value: 'round',     label: '圆领' },
      { value: 'v-neck',    label: 'V领' },
      { value: 'high-neck', label: '高领' },
    ],
  },
  {
    key: 'sleeve', label: '袖子', icon: '💪',
    options: [
      { value: 'short',      label: '短袖' },
      { value: 'long',       label: '长袖' },
      { value: 'sleeveless', label: '无袖' },
    ],
  },
  {
    key: 'length', label: '长度', icon: '📏',
    options: [
      { value: 'short',   label: '短款' },
      { value: 'regular', label: '常规' },
      { value: 'long',    label: '长款' },
    ],
  },
  {
    key: 'fit', label: '版型', icon: '📐',
    options: [
      { value: 'slim',    label: '修身' },
      { value: 'regular', label: '常规' },
      { value: 'loose',   label: '宽松' },
    ],
  },
]
const controls = reactive({ neck: 'round', sleeve: 'short', length: 'regular', fit: 'loose' })

// ── 数量 ──────────────────────────────────────────────────────────────
const count = ref(12)

// ── 生成状态 ──────────────────────────────────────────────────────────
const loading      = ref(false)
const loadingPhase = ref('')
const season       = ref(null)
const scored       = ref(null)
const error        = ref('')

const selectedSeries = ref('A')
const selectedImage  = ref(null)
const savingUrl      = ref(null)
const savedUrls      = ref(new Set())

const SERIES_NAMES = { A: '基础款', B: '设计款', C: '变化款' }

const currentSeries = computed(() => season.value?.[selectedSeries.value] ?? [])
const allImages = computed(() => {
  if (!season.value) return []
  return ['A','B','C'].flatMap(k => season.value[k] ?? [])
})

// init from query params
onMounted(() => {
  if (route.query.refs) {
    const decoded = String(route.query.refs).split(',').map(r => decodeURIComponent(r.trim())).filter(Boolean)
    decoded.forEach((url, i) => { if (i < 3) orderedRefs.value[i] = url })
  }
})

async function generate() {
  loading.value = true
  error.value   = ''
  season.value  = null
  scored.value  = null
  try {
    loadingPhase.value = '① 创建风格档案…'
    let styleProfileId = null
    try {
      const sp = await createStyleProfile({
        name:  brandName.value || 'MyBrand',
        style: brandStyle.value,
        rules: { colors: brandColors.value, avoid: brandAvoid.value },
      })
      styleProfileId = sp?.id ?? null
    } catch (_) {}

    loadingPhase.value = '② 生成整季系列…'
    const res = await generateSeason({
      styleProfileId,
      style:    brandStyle.value,
      refs:     orderedRefs.value.filter(Boolean),
      controls: { ...controls },
      count:    count.value,
    })
    season.value = res

    loadingPhase.value = '③ AI 评分…'
    if (allImages.value.length) {
      try {
        const sc = await designScore({ images: allImages.value, style: brandStyle.value })
        scored.value = sc
      } catch (_) {}
    }
  } catch (e) {
    error.value = e?.message || '生成失败，请重试'
  } finally {
    loading.value = false
    loadingPhase.value = ''
  }
}

function scoreFor(url) {
  return scored.value?.results?.find(r => r.url === url)?.score ?? null
}

function levelFor(url) {
  return scored.value?.results?.find(r => r.url === url)?.level ?? null
}

function scoreBg(score) {
  if (!score) return '#1A1A1A'
  if (score >= 85) return '#00FF8833'
  if (score >= 70) return '#F59E0B33'
  return '#9CA3AF33'
}

function scoreTextColor(score) {
  if (!score) return '#9CA3AF'
  if (score >= 85) return '#00FF88'
  if (score >= 70) return '#F59E0B'
  return '#9CA3AF'
}

async function doSave(url) {
  savingUrl.value = url
  try {
    await saveDesign({ imageUrl: url, style: brandStyle.value, source: 'ai-season' })
    const set = new Set(savedUrls.value)
    set.add(url)
    savedUrls.value = set
  } catch (_) {}
  savingUrl.value = null
}
</script>

<template>
  <div class="min-h-screen bg-bg text-white">

    <!-- ── 顶部导航 ──────────────────────────────────── -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md border-b border-border px-4 py-3 flex items-center gap-3">
      <button class="text-muted active:text-white transition-colors" @click="router.back()">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="flex flex-col flex-1">
        <span class="font-semibold text-sm">🌿 整季系列生成</span>
        <span class="text-[10px]" style="color:#00FF88">一键生成 A/B/C 三档系列</span>
      </div>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-4 pb-28 space-y-6">

      <!-- ── Section 1: 品牌设置 ──────────────────────── -->
      <section>
        <h2 class="font-bold text-sm mb-3" style="color:#A855F7">① 品牌设置</h2>
        <div class="rounded-2xl p-4 space-y-3" style="background:#111;border:1px solid #222">
          <div>
            <label class="text-xs text-muted mb-1 block">品牌名称</label>
            <input
              v-model="brandName"
              type="text"
              placeholder="例如：MUSE Studio"
              class="w-full h-10 px-3 rounded-xl text-sm bg-surface2 border border-border
                     text-white placeholder:text-muted focus:outline-none focus:border-accent"
            />
          </div>
          <div>
            <label class="text-xs text-muted mb-2 block">品牌风格</label>
            <div class="flex gap-2 flex-wrap">
              <button
                v-for="s in STYLE_OPTIONS"
                :key="s.key"
                class="px-3 py-1.5 rounded-full text-xs font-semibold transition-all"
                :style="brandStyle === s.key
                  ? 'background:#A855F7;color:#fff'
                  : 'background:#1A1A1A;border:1px solid #333;color:#9CA3AF'"
                @click="brandStyle = s.key"
              >{{ s.label }}</button>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Section 2: 参考图槽位 ─────────────────────── -->
      <section>
        <h2 class="font-bold text-sm mb-3" style="color:#A855F7">② 参考图（最多3张）</h2>
        <div class="grid grid-cols-3 gap-3">
          <div
            v-for="(slot, i) in REF_SLOTS"
            :key="i"
            class="aspect-[3/4] rounded-2xl overflow-hidden relative"
            style="background:#111;border:1px solid #222"
          >
            <img
              v-if="orderedRefs[i]"
              :src="orderedRefs[i]"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex flex-col items-center justify-center gap-1 p-2">
              <span class="text-xl">{{ slot.icon }}</span>
              <span class="text-[10px] text-center font-semibold" style="color:#A855F7">{{ slot.label }}</span>
              <span class="text-[9px] text-center text-muted">{{ slot.hint }}</span>
            </div>
            <button
              v-if="orderedRefs[i]"
              class="absolute top-1 right-1 w-5 h-5 rounded-full text-[10px] font-black
                     flex items-center justify-center"
              style="background:#000;color:#fff;border:1px solid #333"
              @click="orderedRefs[i] = null"
            >×</button>
          </div>
        </div>
      </section>

      <!-- ── Section 3: 细节控制 ───────────────────────── -->
      <section>
        <h2 class="font-bold text-sm mb-3" style="color:#A855F7">③ 细节控制</h2>
        <div class="rounded-2xl p-4 space-y-3" style="background:#111;border:1px solid #222">
          <div v-for="ctrl in CONTROLS_DEF" :key="ctrl.key">
            <p class="text-xs text-muted mb-2">{{ ctrl.icon }} {{ ctrl.label }}</p>
            <div class="flex gap-2">
              <button
                v-for="opt in ctrl.options"
                :key="opt.value"
                class="flex-1 h-9 rounded-xl text-xs font-semibold transition-all"
                :style="controls[ctrl.key] === opt.value
                  ? 'background:#00FF88;color:#000'
                  : 'background:#1A1A1A;border:1px solid #333;color:#9CA3AF'"
                @click="controls[ctrl.key] = opt.value"
              >{{ opt.label }}</button>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Section 4: 数量 ───────────────────────────── -->
      <section>
        <h2 class="font-bold text-sm mb-3" style="color:#A855F7">④ 生成数量</h2>
        <div class="flex gap-3">
          <button
            v-for="n in [6,9,12]"
            :key="n"
            class="flex-1 h-11 rounded-full text-sm font-bold transition-all"
            :style="count === n
              ? 'background:#00FF88;color:#000'
              : 'background:#1A1A1A;border:1px solid #333;color:#9CA3AF'"
            @click="count = n"
          >{{ n }} 款</button>
        </div>
      </section>

      <!-- ── 生成按钮 ──────────────────────────────────── -->
      <button
        class="w-full h-14 rounded-full font-black text-base active:scale-95 transition-transform"
        style="background:linear-gradient(135deg,#A855F7,#00FF88);color:#000"
        :disabled="loading"
        @click="generate"
      >
        <span v-if="loading">{{ loadingPhase || '生成中…' }}</span>
        <span v-else>🌿 生成整季系列（A/B/C）</span>
      </button>

      <!-- 错误提示 -->
      <p v-if="error" class="text-center text-sm" style="color:#F87171">{{ error }}</p>

      <!-- ── 结果区 ─────────────────────────────────────── -->
      <section v-if="season">
        <h2 class="font-bold text-sm mb-3" style="color:#00FF88">✨ 生成结果</h2>

        <!-- 系列 tabs -->
        <div class="flex gap-2 mb-4">
          <button
            v-for="k in ['A','B','C']"
            :key="k"
            class="flex-1 h-10 rounded-full text-sm font-bold transition-all"
            :style="selectedSeries === k
              ? 'background:#A855F7;color:#fff'
              : 'background:#1A1A1A;border:1px solid #333;color:#9CA3AF'"
            @click="selectedSeries = k"
          >{{ SERIES_NAMES[k] }} {{ k }}</button>
        </div>

        <!-- 4-column grid -->
        <div class="grid grid-cols-2 gap-3">
          <div
            v-for="(url, idx) in currentSeries.slice(0,4)"
            :key="idx"
            class="rounded-2xl overflow-hidden relative cursor-pointer"
            :style="selectedImage === url ? 'border:2px solid #00FF88' : 'border:2px solid #222'"
            @click="selectedImage = selectedImage === url ? null : url"
          >
            <img :src="url" class="w-full aspect-[3/4] object-cover" loading="lazy"/>

            <!-- 评分徽章 -->
            <div
              v-if="scoreFor(url)"
              class="absolute top-2 right-2 text-[10px] font-black px-1.5 py-0.5 rounded-full"
              :style="`background:${scoreBg(scoreFor(url))};color:${scoreTextColor(scoreFor(url))}`"
            >
              {{ levelFor(url) ?? '' }} {{ scoreFor(url) }}
            </div>

            <!-- 保存按钮（选中时显示）-->
            <button
              v-if="selectedImage === url"
              class="absolute bottom-2 left-2 right-2 h-9 rounded-full text-xs font-bold active:scale-95 transition-transform"
              :style="savedUrls.has(url) ? 'background:#1A1A1A;border:1px solid #00FF88;color:#00FF88' : 'background:#00FF88;color:#000'"
              :disabled="savingUrl === url"
              @click.stop="doSave(url)"
            >
              {{ savingUrl === url ? '保存中…' : savedUrls.has(url) ? '✓ 已保存' : '💾 保存到款库' }}
            </button>
          </div>
        </div>

        <!-- 导航到 AI 设计 -->
        <button
          v-if="selectedImage"
          class="w-full h-12 rounded-full font-bold text-sm mt-4 active:scale-95 transition-transform"
          style="background:#A855F7;color:#fff"
          @click="router.push(`/ai/design?refs=${encodeURIComponent(selectedImage)}`)"
        >🎯 用这张出款</button>
        <button
          v-if="selectedImage"
          class="w-full h-12 rounded-full font-bold text-sm mt-2 active:scale-95 transition-transform"
          style="background:#1A1A1A;border:1px solid #333;color:#fff"
          @click="router.push(`/ai/techpack?image=${encodeURIComponent(selectedImage)}&style=${brandStyle}`)"
        >📋 生成设计稿</button>
      </section>

    </div>

    <!-- ── 底部导航 ──────────────────────────────────── -->
    <nav class="fixed bottom-0 left-0 right-0 z-20 bg-bg/95 backdrop-blur-md border-t border-border
                flex items-center justify-around px-2 pt-2 pb-[calc(.5rem+env(safe-area-inset-bottom))]">
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white transition-colors"
              @click="router.push('/')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M3 12l9-9 9 9M5 10v9a1 1 0 001 1h4v-5h4v5h4a1 1 0 001-1v-9"/>
        </svg>
        <span class="text-[10px] font-semibold">首页</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white transition-colors"
              @click="router.push('/inspiration')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M5 3l14 9-14 9V3z"/>
        </svg>
        <span class="text-[10px] font-semibold">灵感</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-accent">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"/>
        </svg>
        <span class="text-[10px] font-semibold">整季</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white transition-colors"
              @click="router.push('/ai/design')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/>
        </svg>
        <span class="text-[10px] font-semibold">出款</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white transition-colors"
              @click="router.push('/me')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
        </svg>
        <span class="text-[10px] font-semibold">我的</span>
      </button>
    </nav>

  </div>
</template>
