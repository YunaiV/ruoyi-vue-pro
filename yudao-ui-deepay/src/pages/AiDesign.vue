<!--
  AiDesign.vue — AI 出款工具（可控设计系统）
  路径：/ai/design

  完整流程：
  ① 选灵感图（最多 3 张：版型 / 风格 / 细节）
  ② 选风格（极简 / 高级 / 现代）
  ③ 选细节控制（领口 / 袖子 / 长度 / 版型）
  ④ 点击【生成系列】→ generateCollection → 6 张
     → 去重 → 评分 → 自动标 Top-3
  ⑤ 点击选中一张 → 查看细节控制选项
  ⑥ 局部细节修改（改袖子 / 改领口 / …）→ updateDetail
  ⑦ 精修 → refine（3 张升级版）
  ⑧ 改色 → recolor（5 种配色）
  ⑨ 保存 → saveDesign
-->
<script setup>
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  generateCollection,
  updateDetail,
  refineImage,
  recolorImage,
  scoreImages,
  selectBestImages,
  deduplicateImages,
  saveDesign,
} from '@/api/ai'
import { initUserId } from '@/utils/user'

const route  = useRoute()
const router = useRouter()
const userId = initUserId()

// ── 风格（3 种，spec 规定）──────────────────────────────────────────
const STYLES = [
  { key: 'minimal', label: '极简', desc: '黑白 · 干净线条', color: '#E5E7EB' },
  { key: 'luxury',  label: '高级', desc: '奢华 · 精致面料', color: '#F59E0B' },
  { key: 'modern',  label: '现代', desc: '当代 · 几何平衡', color: '#60A5FA' },
]
const selectedStyle = ref('minimal')

// ── 细节控制（4 维，spec 规定）────────────────────────────────────
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
      { value: 'short',     label: '短袖' },
      { value: 'long',      label: '长袖' },
      { value: 'sleeveless',label: '无袖' },
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

// ── 3 槽位参考图（版型 / 风格 / 细节）──────────────────────────────
const REF_SLOTS = [
  { key: 'structure', label: '版型', hint: '廓形', icon: '◻' },
  { key: 'style',     label: '风格', hint: '感觉', icon: '✦' },
  { key: 'detail',    label: '细节', hint: '元素', icon: '⋯' },
]
const refSlots      = ref({ structure: null, style: null, detail: null })
const fileInputRefs = ref({})
let objectUrls      = []

onUnmounted(() => { objectUrls.forEach(u => URL.revokeObjectURL(u)) })

onMounted(() => {
  const refsParam = route.query.refs
  if (refsParam) {
    const decoded = String(refsParam).split(',').map(r => decodeURIComponent(r.trim())).filter(Boolean)
    const slotKeys = ['structure', 'style', 'detail']
    decoded.forEach((url, i) => { if (i < slotKeys.length) refSlots.value[slotKeys[i]] = url })
  }
})

function onFileChange(slotKey, e) {
  const file = e.target.files?.[0]
  if (!file) return
  const objectUrl = URL.createObjectURL(file)
  objectUrls.push(objectUrl)
  refSlots.value[slotKey] = objectUrl
  e.target.value = ''
}
function clearSlot(slotKey) { refSlots.value[slotKey] = null }

const orderedRefs = computed(() =>
  ['structure', 'style', 'detail'].map(k => refSlots.value[k]).filter(Boolean)
)

// ── Generate pipeline ────────────────────────────────────────────────
const loading          = ref(false)
const loadingPhase     = ref('')
const error            = ref('')
const generatedImages  = ref([])
const collectionName   = ref('')
const scoreMap         = ref({})
const selected         = ref(null)

function loadingLabel() {
  if (loadingPhase.value === 'generating')    return '设计系列中…'
  if (loadingPhase.value === 'deduplicating') return '过滤重复款…'
  if (loadingPhase.value === 'scoring')       return '评分选推荐…'
  return '处理中…'
}

async function generate() {
  if (loading.value) return
  error.value           = ''
  loading.value         = true
  selected.value        = null
  generatedImages.value = []
  scoreMap.value        = {}
  collectionName.value  = ''
  refineResults.value   = []
  refineSelected.value  = null
  recolorResult.value   = null
  saveOk.value          = false
  detailUpdating.value  = false

  try {
    // Step 1: generateCollection with controls
    loadingPhase.value = 'generating'
    const genRes = await generateCollection({
      refs:     orderedRefs.value,
      style:    selectedStyle.value,
      controls: { ...controls },
      count:    6,
      userId,
    })
    let rawImages = genRes?.images ?? []
    if (rawImages.length < 1) throw new Error('生成结果为空，请重试')
    collectionName.value = genRes?.collectionName ?? 'Collection'

    // Step 2: Deduplicate
    loadingPhase.value = 'deduplicating'
    let cleanImages = rawImages
    try {
      const dedupRes = await deduplicateImages({ images: rawImages, reference: orderedRefs.value, threshold: 0.85 })
      if (dedupRes?.filtered?.length) cleanImages = dedupRes.filtered
    } catch (_) {}

    // Step 3: Score + select (bad filtered server-side)
    loadingPhase.value = 'scoring'
    let scores = []
    let best   = []
    try {
      const selRes = await selectBestImages({ images: cleanImages, style: selectedStyle.value })
      scores = selRes?.scores ?? []
      best   = selRes?.best   ?? []
    } catch (_) {
      try {
        const scoreRes = await scoreImages({ images: cleanImages, style: selectedStyle.value })
        const raw = scoreRes?.scores ?? []
        const sorted = [...raw].filter(s => !s.bad).sort((a, b) => b.score - a.score)
        best = sorted.slice(0, 3).map(s => s.url)
        scores = raw.map(s => ({ ...s, recommended: best.includes(s.url) }))
      } catch (_2) {
        scores = cleanImages.map((url, i) => ({ url, score: 80 - i * 5, bad: false, recommended: i < 3 }))
        best   = cleanImages.slice(0, 3)
      }
    }

    scores.forEach(s => { scoreMap.value[s.url] = s.score })
    generatedImages.value = cleanImages.map(url => {
      const entry = scores.find(s => s.url === url) ?? {}
      return { url, score: entry.score ?? 50, bad: entry.bad ?? false, recommended: entry.recommended ?? best.includes(url) }
    })
    generatedImages.value.sort((a, b) => {
      if (a.bad !== b.bad) return a.bad ? 1 : -1
      if (a.recommended !== b.recommended) return a.recommended ? -1 : 1
      return b.score - a.score
    })
  } catch (e) {
    error.value = e?.message || '生成失败，请稍后重试'
  } finally {
    loading.value      = false
    loadingPhase.value = ''
  }
}

// ── Per-image detail update ──────────────────────────────────────────
const detailUpdating = ref(false)
const detailError    = ref('')
// Which control key is being patched on the selected image
const patchControl   = ref({})

async function applyDetailPatch(controlKey, value) {
  if (!selected.value || detailUpdating.value) return
  detailError.value    = ''
  detailUpdating.value = true
  patchControl.value   = { [controlKey]: value }

  try {
    const res = await updateDetail({
      image:   selected.value,
      control: { [controlKey]: value },
      userId,
    })
    const newUrl = res?.image
    if (newUrl && newUrl !== selected.value) {
      // Replace in the list
      const idx = generatedImages.value.findIndex(img => img.url === selected.value)
      if (idx >= 0) {
        generatedImages.value[idx] = { ...generatedImages.value[idx], url: newUrl }
        scoreMap.value[newUrl] = scoreMap.value[selected.value] ?? 75
      }
      selected.value = newUrl
    }
  } catch (e) {
    detailError.value = e?.message || '细节修改失败'
  } finally {
    detailUpdating.value = false
    patchControl.value   = {}
  }
}

// ── Refine ────────────────────────────────────────────────────────────
const refining       = ref(false)
const refineResults  = ref([])
const refineSelected = ref(null)
const refineError    = ref('')
const refineNote     = ref('')

async function doRefine() {
  if (!selected.value || refining.value) return
  refineError.value  = ''
  refining.value     = true
  refineResults.value = []
  refineSelected.value = null

  try {
    const res = await refineImage({ image: selected.value, style: selectedStyle.value, note: refineNote.value.trim() || undefined, userId })
    const imgs = res?.images ?? []
    let refinedScored = imgs.map((url, i) => ({ url, score: 88 - i * 3, recommended: i === 0 }))
    try {
      const scoreRes = await scoreImages({ images: imgs, style: selectedStyle.value })
      const raw = scoreRes?.scores ?? []
      const sorted = [...raw].sort((a, b) => b.score - a.score)
      const bestUrl = sorted[0]?.url
      refinedScored = imgs.map(url => {
        const s = raw.find(e => e.url === url) ?? {}
        return { url, score: s.score ?? 80, recommended: url === bestUrl }
      })
    } catch (_) {}
    refineResults.value = refinedScored
  } catch (e) {
    refineError.value = e?.message || '精修失败，请重试'
  } finally {
    refining.value = false
  }
}

// ── Recolor ───────────────────────────────────────────────────────────
const COLOR_SCHEMES = [
  { key: 'black_white',  label: '黑白',    swatch: ['#000','#FFF'] },
  { key: 'earth_tone',   label: '大地色',  swatch: ['#C4A882','#8B6914'] },
  { key: 'grey_minimal', label: '灰阶',    swatch: ['#D1D5DB','#4B5563'] },
  { key: 'mono_color',   label: '单色系',  swatch: ['#1E3A5F','#4A90D9'] },
  { key: 'navy_cream',   label: '藏青+米', swatch: ['#1B2A4A','#F5F0E0'] },
]
const recoloring    = ref(false)
const recolorResult = ref(null)
const recolorError  = ref('')
const selectedScheme = ref('black_white')

const recolorSource = computed(() => refineSelected.value ?? selected.value)

async function doRecolor() {
  if (!recolorSource.value || recoloring.value) return
  recolorError.value = ''
  recoloring.value   = true
  recolorResult.value = null

  try {
    const res = await recolorImage({ image: recolorSource.value, colorScheme: selectedScheme.value, userId })
    recolorResult.value = res?.image ?? null
  } catch (e) {
    recolorError.value = e?.message || '改色失败，请重试'
  } finally {
    recoloring.value = false
  }
}

// ── Save ─────────────────────────────────────────────────────────────
const saving    = ref(false)
const saveOk    = ref(false)
const saveError = ref('')

const finalImage = computed(() => recolorResult.value ?? refineSelected.value ?? selected.value)

async function doSave() {
  if (!finalImage.value || saving.value) return
  saveError.value = ''
  saveOk.value    = false
  saving.value    = true
  try {
    await saveDesign({ imageUrl: finalImage.value, style: selectedStyle.value, userId, source: 'ai-design' })
    saveOk.value = true
  } catch (e) {
    saveError.value = e?.message || '保存失败，请重试'
  } finally {
    saving.value = false
  }
}

function scoreColor(score) {
  if (score >= 85) return '#00FF88'
  if (score >= 70) return '#F59E0B'
  return '#9CA3AF'
}
</script>

<template>
  <div class="min-h-screen bg-bg text-white">

    <!-- ── 顶部导航 ───────────────── -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md border-b border-border px-4 py-3 flex items-center gap-3">
      <button class="text-muted active:text-white" @click="router.back()">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="flex flex-col flex-1">
        <span class="font-semibold text-sm">🎯 AI 可控设计系统</span>
        <span class="text-[10px]" style="color:#00FF88">
          选图 → 控参数 → 生成系列 → 局部改 → 精修 → 改色
        </span>
      </div>
      <button class="text-xs font-semibold px-3 py-1.5 rounded-full border border-border text-muted"
              @click="router.push('/inspiration')">🎭 灵感库</button>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-5 pb-40">

      <!-- ── 1 参考图 ─────────────────────────────── -->
      <section class="mb-5">
        <div class="flex items-center justify-between mb-2">
          <p class="label-xs">灵感图（版型 / 风格 / 细节）</p>
          <button class="text-[11px]" style="color:#00FF88" @click="router.push('/inspiration')">灵感库 →</button>
        </div>
        <div class="grid grid-cols-3 gap-2">
          <div v-for="slot in REF_SLOTS" :key="slot.key"
               class="relative rounded-2xl overflow-hidden aspect-[3/4] border border-dashed border-border
                      flex flex-col items-center justify-center gap-1 bg-surface2 cursor-pointer group"
               :class="refSlots[slot.key] ? 'border-solid border-accent/50' : ''"
               @click="!refSlots[slot.key] && fileInputRefs[slot.key]?.click()">
            <template v-if="refSlots[slot.key]">
              <img :src="refSlots[slot.key]" class="w-full h-full object-cover absolute inset-0" />
              <div class="absolute inset-0 bg-black/30" />
              <span class="relative z-10 text-white text-[11px] font-bold bg-black/50 px-2 py-0.5 rounded-full">{{ slot.label }}</span>
              <button class="absolute top-1.5 right-1.5 z-10 w-5 h-5 rounded-full bg-black/70 text-white text-[10px] flex items-center justify-center"
                      @click.stop="clearSlot(slot.key)">✕</button>
            </template>
            <template v-else>
              <span class="text-xl text-muted">{{ slot.icon }}</span>
              <p class="text-[10px] font-semibold text-muted">{{ slot.label }}</p>
              <p class="text-[9px] text-muted/60">{{ slot.hint }}</p>
            </template>
            <input :ref="el => { fileInputRefs[slot.key] = el }" type="file" accept="image/*" class="hidden"
                   @change="onFileChange(slot.key, $event)" />
          </div>
        </div>
      </section>

      <!-- ── 2 风格 ───────────────────────────────── -->
      <section class="mb-5">
        <p class="label-xs mb-2">风格</p>
        <div class="grid grid-cols-3 gap-2">
          <button v-for="s in STYLES" :key="s.key"
                  class="h-[64px] rounded-2xl flex flex-col items-center justify-center gap-1 border-2 transition-all active:scale-95"
                  :class="selectedStyle === s.key ? 'border-transparent' : 'border-border bg-surface2'"
                  :style="selectedStyle === s.key ? `background:${s.color}18; border-color:${s.color}` : ''"
                  @click="selectedStyle = s.key">
            <span class="font-black text-sm" :style="selectedStyle === s.key ? `color:${s.color}` : 'color:#9CA3AF'">{{ s.label }}</span>
            <span class="text-[10px]" :style="selectedStyle === s.key ? 'color:#6B7280' : 'color:#4B5563'">{{ s.desc }}</span>
          </button>
        </div>
      </section>

      <!-- ── 3 细节控制面板 ────────────────────────── -->
      <section class="mb-5">
        <p class="label-xs mb-2">细节控制</p>
        <div class="card rounded-2xl p-3 space-y-3">
          <div v-for="ctrl in CONTROLS_DEF" :key="ctrl.key" class="flex items-center gap-2">
            <span class="text-base shrink-0">{{ ctrl.icon }}</span>
            <span class="text-xs font-semibold text-muted w-10 shrink-0">{{ ctrl.label }}</span>
            <div class="flex gap-1.5 flex-wrap">
              <button v-for="opt in ctrl.options" :key="opt.value"
                      class="px-2.5 py-1 rounded-full text-[11px] font-semibold transition-all border"
                      :class="controls[ctrl.key] === opt.value
                        ? 'border-transparent text-black'
                        : 'border-border text-muted bg-surface2'"
                      :style="controls[ctrl.key] === opt.value ? 'background:#00FF88' : ''"
                      @click="controls[ctrl.key] = opt.value">
                {{ opt.label }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- ── 4 生成按钮 ────────────────────────────── -->
      <button :disabled="loading"
              class="w-full h-14 rounded-2xl font-bold text-sm mb-2 active:scale-95 transition-transform
                     disabled:opacity-50 flex items-center justify-center gap-2"
              style="background:#00FF88;color:#000"
              @click="generate">
        <svg v-if="loading" class="animate-spin h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity=".3" stroke-width="3"/>
          <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
        </svg>
        <span>{{ loading ? loadingLabel() : '🚀 生成系列（6 款）' }}</span>
      </button>
      <p class="text-center text-xs text-muted mb-6">系列一致 · 自动过滤低质 · 推荐 Top-3</p>
      <p v-if="error" class="text-danger text-sm text-center mb-4">{{ error }}</p>

      <!-- ── 5 系列结果 ─────────────────────────────── -->
      <div v-if="generatedImages.length">
        <div class="flex items-center justify-between mb-3">
          <p class="font-bold text-sm" style="color:#00FF88">{{ collectionName }}</p>
          <span class="text-[10px] text-muted">{{ generatedImages.filter(i => i.recommended).length }} 款推荐</span>
        </div>

        <div class="grid grid-cols-2 gap-3 mb-6">
          <div v-for="img in generatedImages" :key="img.url"
               class="relative cursor-pointer rounded-2xl overflow-hidden border-2 transition-all aspect-[3/4]"
               :class="[selected === img.url ? 'border-accent' : 'border-transparent', img.bad ? 'opacity-40' : '']"
               @click="!img.bad && (selected = img.url, saveOk = false, refineResults = [], refineSelected = null, recolorResult = null)">
            <img :src="img.url" class="w-full h-full object-cover" loading="lazy" />
            <div v-if="img.bad" class="absolute inset-0 flex items-center justify-center bg-black/60 backdrop-blur-sm">
              <span class="text-xs font-bold text-white/60">⚠ 低质</span>
            </div>
            <div v-if="img.recommended && !img.bad"
                 class="absolute top-2 left-2 px-2 py-0.5 rounded-full text-[10px] font-black"
                 style="background:#00FF88;color:#000">⭐ 推荐</div>
            <div v-if="!img.bad"
                 class="absolute top-2 right-2 px-2 py-0.5 rounded-full text-[11px] font-bold bg-black/60"
                 :style="{ color: scoreColor(img.score) }">{{ img.score }}</div>
            <div v-if="selected === img.url"
                 class="absolute bottom-2 left-1/2 -translate-x-1/2 bg-accent text-black text-[10px] font-bold px-3 py-1 rounded-full whitespace-nowrap">✓ 已选</div>
          </div>
        </div>

        <!-- ── 6 局部细节修改 ──────────────────────── -->
        <div v-if="selected" class="mb-5">
          <p class="label-xs mb-2">局部细节修改</p>
          <div class="card rounded-2xl p-3 space-y-2.5">
            <div v-for="ctrl in CONTROLS_DEF" :key="ctrl.key" class="flex items-center gap-2">
              <span class="text-base shrink-0">{{ ctrl.icon }}</span>
              <span class="text-xs text-muted w-10 shrink-0">{{ ctrl.label }}</span>
              <div class="flex gap-1.5 flex-wrap">
                <button v-for="opt in ctrl.options" :key="opt.value"
                        class="px-2.5 py-1 rounded-full text-[10px] font-semibold transition-all border
                               disabled:opacity-50 relative overflow-hidden"
                        :class="controls[ctrl.key] === opt.value
                          ? 'border-transparent text-black'
                          : 'border-border text-muted bg-surface2'"
                        :style="controls[ctrl.key] === opt.value ? 'background:#A855F7' : ''"
                        :disabled="detailUpdating"
                        @click="controls[ctrl.key] = opt.value; applyDetailPatch(ctrl.key, opt.value)">
                  <svg v-if="detailUpdating && patchControl[ctrl.key] === opt.value"
                       class="animate-spin h-3 w-3 absolute inset-0 m-auto" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity=".3" stroke-width="3"/>
                    <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
                  </svg>
                  <span :class="detailUpdating && patchControl[ctrl.key] === opt.value ? 'invisible' : ''">
                    改{{ ctrl.label }}→{{ opt.label }}
                  </span>
                </button>
              </div>
            </div>
          </div>
          <p v-if="detailError" class="text-xs text-danger mt-1.5">{{ detailError }}</p>
        </div>

        <!-- ── 7 精修 ──────────────────────────────── -->
        <div v-if="selected" class="mb-5">
          <p class="label-xs mb-2">精修（→ 3 张设计师级升级版）</p>
          <input v-model="refineNote" type="text" :placeholder='"可选备注，如\"更简洁\""'
                 class="w-full h-10 rounded-xl px-3 text-sm bg-surface2 border border-border text-white
                        placeholder:text-muted focus:outline-none focus:border-accent mb-2.5"
                 @keydown.enter="doRefine" />
          <button :disabled="refining"
                  class="w-full h-12 rounded-full font-bold text-sm active:scale-95 disabled:opacity-50 flex items-center justify-center gap-2"
                  style="background:linear-gradient(135deg,#A855F7,#6366F1);color:#fff"
                  @click="doRefine">
            <svg v-if="refining" class="animate-spin h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity=".3" stroke-width="3"/>
              <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            </svg>
            <span>{{ refining ? '精修中…' : '✨ 再优化一下' }}</span>
          </button>
          <p v-if="refineError" class="text-xs text-danger mt-1.5 text-center">{{ refineError }}</p>
          <div v-if="refineResults.length" class="mt-3">
            <p class="text-[10px] text-muted mb-2">精修结果（点击选择）</p>
            <div class="grid grid-cols-3 gap-2">
              <div v-for="img in refineResults" :key="img.url"
                   class="relative cursor-pointer rounded-xl overflow-hidden border-2 transition-all aspect-[3/4]"
                   :class="refineSelected === img.url ? 'border-accent' : 'border-transparent'"
                   @click="refineSelected = img.url; saveOk = false; recolorResult = null">
                <img :src="img.url" class="w-full h-full object-cover" loading="lazy" />
                <div v-if="img.recommended"
                     class="absolute top-1 left-1 px-1.5 py-0.5 rounded-full text-[9px] font-black"
                     style="background:#A855F7;color:#fff">✦ 最优</div>
                <div class="absolute top-1 right-1 px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-black/60"
                     :style="{ color: scoreColor(img.score) }">{{ img.score }}</div>
                <div v-if="refineSelected === img.url"
                     class="absolute bottom-1 left-1/2 -translate-x-1/2 text-[9px] font-bold px-2 py-0.5 rounded-full bg-accent text-black whitespace-nowrap">✓</div>
              </div>
            </div>
          </div>
        </div>

        <!-- ── 8 改色 ──────────────────────────────── -->
        <div v-if="recolorSource" class="mb-5">
          <p class="label-xs mb-2">改色（只改颜色，结构不变）</p>
          <div class="flex gap-2 overflow-x-auto pb-1 scrollbar-hide mb-2.5">
            <button v-for="scheme in COLOR_SCHEMES" :key="scheme.key"
                    class="shrink-0 flex items-center gap-1.5 px-3 py-2 rounded-full border text-xs font-semibold transition-all"
                    :class="selectedScheme === scheme.key ? 'border-transparent text-black' : 'border-border text-muted bg-surface2'"
                    :style="selectedScheme === scheme.key ? 'background:#00FF88' : ''"
                    @click="selectedScheme = scheme.key; recolorResult = null">
              <span class="flex gap-0.5">
                <span v-for="c in scheme.swatch" :key="c" class="w-3 h-3 rounded-full border border-white/10 shrink-0" :style="`background:${c}`"></span>
              </span>
              {{ scheme.label }}
            </button>
          </div>
          <button :disabled="recoloring"
                  class="w-full h-12 rounded-full font-bold text-sm active:scale-95 disabled:opacity-50 flex items-center justify-center gap-2"
                  style="background:linear-gradient(135deg,#F59E0B,#EF4444);color:#fff"
                  @click="doRecolor">
            <svg v-if="recoloring" class="animate-spin h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity=".3" stroke-width="3"/>
              <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            </svg>
            <span>{{ recoloring ? '改色中…' : '🎨 应用配色' }}</span>
          </button>
          <p v-if="recolorError" class="text-xs text-danger mt-1.5 text-center">{{ recolorError }}</p>
          <div v-if="recolorResult" class="mt-3">
            <p class="text-[10px] text-muted mb-2">改色结果</p>
            <div class="rounded-2xl overflow-hidden border-2 border-accent max-w-[180px]">
              <img :src="recolorResult" class="w-full object-cover aspect-[3/4]" loading="lazy" />
            </div>
          </div>
        </div>

        <!-- ── 9 保存 ───────────────────────────────── -->
        <div v-if="finalImage" class="space-y-2.5">
          <p v-if="saveOk" class="text-center text-xs font-semibold" style="color:#00FF88">✔ 已保存到款库</p>
          <p v-if="saveError" class="text-center text-xs text-danger">{{ saveError }}</p>
          <button :disabled="saving || saveOk"
                  class="w-full h-12 rounded-full font-bold text-sm active:scale-95 disabled:opacity-50 flex items-center justify-center gap-2"
                  style="background:#00FF88;color:#000"
                  @click="doSave">
            <svg v-if="saving" class="animate-spin h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity=".3" stroke-width="3"/>
              <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            </svg>
            <span>{{ saving ? '保存中…' : saveOk ? '✔ 已保存' : '💾 保存成图' }}</span>
          </button>
          <button class="w-full h-11 rounded-full border border-border text-muted text-sm font-medium active:opacity-70"
                  @click="router.push('/me')">查看款库 →</button>
        </div>
      </div><!-- /generatedImages -->

      <!-- 空状态 -->
      <div v-if="!loading && !generatedImages.length && !error" class="text-center py-14">
        <p class="text-5xl mb-4">🎯</p>
        <p class="text-white font-semibold text-lg mb-2">AI 可控设计系统</p>
        <p class="text-muted text-sm leading-loose">
          选 3 张灵感图 + 设置细节参数<br>
          → 生成同风格系列 → 局部改 → 精修 → 改色
        </p>
        <div class="mt-5 grid grid-cols-4 gap-1.5 text-center text-xs text-muted">
          <div class="card p-2 rounded-xl"><p class="text-lg mb-1">🔵</p><p>领口</p></div>
          <div class="card p-2 rounded-xl"><p class="text-lg mb-1">💪</p><p>袖子</p></div>
          <div class="card p-2 rounded-xl"><p class="text-lg mb-1">📏</p><p>长度</p></div>
          <div class="card p-2 rounded-xl"><p class="text-lg mb-1">📐</p><p>版型</p></div>
        </div>
      </div>

    </div><!-- /max-w -->

    <!-- ── 底部导航 ───────────────────────────────── -->
    <nav class="fixed bottom-0 left-0 right-0 z-20 bg-bg/95 backdrop-blur-md border-t border-border
                flex items-center justify-around px-2 pt-2 pb-[calc(.5rem+env(safe-area-inset-bottom))]">
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white" @click="router.push('/')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 12l9-9 9 9M5 10v9a1 1 0 001 1h4v-5h4v5h4a1 1 0 001-1v-9"/></svg>
        <span class="text-[10px] font-semibold">首页</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white" @click="router.push('/inspiration')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 3l14 9-14 9V3z"/></svg>
        <span class="text-[10px] font-semibold">灵感</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-accent">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/></svg>
        <span class="text-[10px] font-semibold">出款</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white" @click="router.push('/redesign')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
        <span class="text-[10px] font-semibold">改款</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white" @click="router.push('/me')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
        <span class="text-[10px] font-semibold">我的</span>
      </button>
    </nav>
  </div>
</template>

<style scoped>
.label-xs {
  font-size: 11px;
  font-weight: 600;
  color: #6B7280;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
</style>
