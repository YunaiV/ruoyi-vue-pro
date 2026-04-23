<!--
  Redesign.vue — AI 改款设计师页（核心）
  路径：/redesign

  完整流程：
  ① 上传参考图（多图，手机本地或直接粘贴 URL）
  ② 选风格（极简 / 潮流 / 高端 / 街头 / 优雅）
  ③ 点击【生成】→ 后端生成 6 张
     → 防撞款过滤
     → 自动评分 + 选 Top-3（标记"推荐"）
  ④ 点击图片选中，显示评分
  ⑤ 输入微调指令 → 再生成（单图微调）
  ⑥ 一键保存到款库

  UI：
  [上传区域]  [风格按钮]
  [生成按钮]
  ────────────
  [6张图 + 推荐徽章 + 评分]
  ────────────
  [微调输入框]  [再生成]
  ────────────
  [保存到款库]  [一键看推荐]
-->
<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  redesignImages,
  editImage,
  scoreImages,
  selectBestImages,
  deduplicateImages,
  saveDesign,
} from '@/api/ai'
import { initUserId } from '@/utils/user'

const route  = useRoute()
const router = useRouter()
const userId = initUserId()

// ── 风格配置 ──────────────────────────────────────────────────────────
const STYLES = [
  { key: 'minimal',    label: '极简',  icon: '◻' },
  { key: 'trendy',     label: '潮流',  icon: '🔥' },
  { key: 'luxury',     label: '高端',  icon: '💎' },
  { key: 'streetwear', label: '街头',  icon: '🏙' },
  { key: 'elegant',    label: '优雅',  icon: '🌸' },
]

const selectedStyle    = ref('minimal')
const strength         = ref(0.6)
const refImageUrls     = ref([])       // 用户上传/粘贴的参考图 URL 列表
const urlInputValue    = ref('')       // URL 粘贴输入框

// ── 生成结果状态 ───────────────────────────────────────────────────────
const loading          = ref(false)
const loadingPhase     = ref('')       // 'generating' | 'deduplicating' | 'scoring'
const error            = ref('')
const generatedImages  = ref([])       // [{ url, score, recommended }]
const selected         = ref(null)     // 当前选中图片 url

// ── 评分 ───────────────────────────────────────────────────────────────
const scoreMap         = ref({})       // url → score
const recommendedUrls  = ref([])       // top-3 url list
const showOnlyBest     = ref(false)

const displayImages = computed(() =>
  showOnlyBest.value
    ? generatedImages.value.filter(img => img.recommended)
    : generatedImages.value
)

// ── 微调 ────────────────────────────────────────────────────────────────
const tweakInstruction = ref('')
const tweaking         = ref(false)
const tweakError       = ref('')

// ── 保存 ────────────────────────────────────────────────────────────────
const saving           = ref(false)
const saveOk           = ref(false)
const saveError        = ref('')

// ── 上传相关 ────────────────────────────────────────────────────────────
const fileInput        = ref(null)
let objectUrls         = []   // track blob URLs for cleanup

onUnmounted(() => {
  objectUrls.forEach(u => URL.revokeObjectURL(u))
})

// ── Read ?refs= query param from Inspiration.vue ──────────────────────
onMounted(() => {
  const refsParam = route.query.refs
  if (refsParam) {
    const decoded = String(refsParam).split(',').map(r => decodeURIComponent(r.trim())).filter(Boolean)
    decoded.forEach(url => {
      if (!refImageUrls.value.includes(url)) refImageUrls.value.push(url)
    })
  }
})

function onFileChange(e) {
  const files = Array.from(e.target.files || [])
  files.slice(0, 5).forEach(file => {
    const objectUrl = URL.createObjectURL(file)
    objectUrls.push(objectUrl)
    refImageUrls.value.push(objectUrl)
  })
  e.target.value = ''
}

function addUrlInput() {
  const raw = urlInputValue.value.trim()
  if (!raw) return
  if (refImageUrls.value.includes(raw)) {
    urlInputValue.value = ''
    return
  }
  refImageUrls.value.push(raw)
  urlInputValue.value = ''
}

function removeRefImage(idx) {
  refImageUrls.value.splice(idx, 1)
}

// ── 核心：生成流水线 ─────────────────────────────────────────────────
async function generate() {
  if (loading.value) return
  error.value      = ''
  loading.value    = true
  selected.value   = null
  generatedImages.value = []
  scoreMap.value   = {}
  recommendedUrls.value = []
  saveOk.value     = false

  try {
    // Step 1: Generate 6 images
    loadingPhase.value = 'generating'
    const genRes = await redesignImages({
      images:   refImageUrls.value,
      style:    selectedStyle.value,
      strength: strength.value,
      count:    6,
      userId,
    })
    let rawImages = genRes?.images ?? []
    if (rawImages.length < 1) throw new Error('生成结果为空，请重试')

    // Step 2: Deduplicate (filter too-similar images + against reference)
    loadingPhase.value = 'deduplicating'
    let cleanImages = rawImages
    try {
      const dedupRes = await deduplicateImages({
        images:    rawImages,
        reference: refImageUrls.value,
        threshold: 0.85,
      })
      if (dedupRes?.filtered?.length) cleanImages = dedupRes.filtered
    } catch (_) { /* keep original on error */ }

    // Step 3: Score + select top-3
    loadingPhase.value = 'scoring'
    let scores = []
    let best   = []
    try {
      const selRes = await selectBestImages({
        images: cleanImages,
        style:  selectedStyle.value,
      })
      scores = selRes?.scores ?? []
      best   = selRes?.best   ?? []
    } catch (_) {
      // Fallback: use raw score endpoint
      try {
        const scoreRes = await scoreImages({ images: cleanImages, style: selectedStyle.value })
        scores = (scoreRes?.scores ?? []).map(s => ({ ...s, recommended: false }))
        const sorted = [...scores].sort((a, b) => b.score - a.score)
        best = sorted.slice(0, 3).map(s => s.url)
        scores.forEach(s => { s.recommended = best.includes(s.url) })
      } catch (_2) {
        scores = cleanImages.map((url, i) => ({ url, score: 80 - i * 5, recommended: i < 3 }))
        best   = cleanImages.slice(0, 3)
      }
    }

    recommendedUrls.value = best
    scores.forEach(s => { scoreMap.value[s.url] = s.score })

    generatedImages.value = cleanImages.map(url => ({
      url,
      score:       scoreMap.value[url] ?? 0,
      recommended: best.includes(url),
    }))
    // Sort recommended first, then by score
    generatedImages.value.sort((a, b) => {
      if (a.recommended !== b.recommended) return a.recommended ? -1 : 1
      return b.score - a.score
    })
  } catch (e) {
    error.value = e?.message || '生成失败，请稍后重试'
  } finally {
    loading.value    = false
    loadingPhase.value = ''
  }
}

function loadingLabel() {
  if (loadingPhase.value === 'generating')    return 'AI 正在生成款式…'
  if (loadingPhase.value === 'deduplicating') return '正在去除重复款…'
  if (loadingPhase.value === 'scoring')       return '正在打分选最优…'
  return '处理中…'
}

// ── 微调单张图 ──────────────────────────────────────────────────────────
async function doTweak() {
  if (!selected.value || !tweakInstruction.value.trim() || tweaking.value) return
  tweakError.value = ''
  tweaking.value   = true
  try {
    const res = await editImage({
      image:       selected.value,
      instruction: tweakInstruction.value.trim(),
      userId,
    })
    if (res?.image) {
      // Replace the selected image in the list with the tweaked result
      const idx = generatedImages.value.findIndex(img => img.url === selected.value)
      const newEntry = { url: res.image, score: 85, recommended: false }
      if (idx >= 0) {
        generatedImages.value.splice(idx, 1, newEntry)
      } else {
        generatedImages.value.unshift(newEntry)
      }
      selected.value = res.image
      scoreMap.value[res.image] = 85
    }
    tweakInstruction.value = ''
  } catch (e) {
    tweakError.value = e?.message || '微调失败，请重试'
  } finally {
    tweaking.value = false
  }
}

// ── 保存到款库 ──────────────────────────────────────────────────────────
async function doSave() {
  if (!selected.value || saving.value) return
  saveError.value = ''
  saveOk.value    = false
  saving.value    = true
  try {
    await saveDesign({
      imageUrl: selected.value,
      style:    selectedStyle.value,
      userId,
      source:   'redesign',
    })
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

    <!-- 顶部导航 -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md
                   border-b border-border px-4 py-3
                   flex items-center gap-3">
      <button class="text-muted active:text-white transition-colors"
              @click="router.back()">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="flex flex-col flex-1">
        <span class="font-semibold text-sm">✨ AI 改款设计</span>
        <span class="text-[10px]" style="color:#00FF88">上传参考图 → 生成 6 款 → 自动选最优</span>
      </div>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-5 pb-32">

      <!-- 灵感库来源提示 -->
      <div v-if="refImageUrls.length && route.query.refs"
           class="mb-4 p-3 rounded-xl text-xs leading-relaxed"
           style="background:#A855F71A; border:1px solid #A855F733; color:#A855F7">
        💡 <strong>灵感融合提示：</strong>
        秀场图提供结构/剪裁灵感，品牌图提供可穿性，AI 将融合两者生成新款
      </div>

      <!-- ── 上传参考图 ─────────────────────────────────────── -->
      <section class="mb-5">
        <p class="text-[11px] text-muted font-semibold uppercase tracking-widest mb-2.5">
          参考图（可选，最多5张）
        </p>

        <!-- 已上传缩略图 -->
        <div v-if="refImageUrls.length"
             class="flex gap-2 overflow-x-auto pb-1 scrollbar-hide mb-2.5">
          <div
            v-for="(url, idx) in refImageUrls"
            :key="idx"
            class="relative shrink-0 w-16 h-16 rounded-xl overflow-hidden border border-border"
          >
            <img :src="url" class="w-full h-full object-cover" />
            <button
              class="absolute top-0.5 right-0.5 w-4 h-4 rounded-full bg-black/70
                     text-white text-[10px] flex items-center justify-center"
              @click="removeRefImage(idx)"
            >✕</button>
          </div>
        </div>

        <!-- 上传 / URL 输入行 -->
        <div class="flex gap-2">
          <!-- 本地上传 -->
          <button
            v-if="refImageUrls.length < 5"
            class="chip text-xs shrink-0"
            @click="fileInput?.click()"
          >📁 上传图片</button>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            multiple
            class="hidden"
            @change="onFileChange"
          />

          <!-- 灵感库 -->
          <button
            class="chip text-xs shrink-0"
            @click="router.push('/inspiration')"
          >🎭 灵感库</button>

          <!-- URL 粘贴 -->
          <input
            v-model="urlInputValue"
            type="url"
            placeholder="粘贴图片 URL…"
            class="flex-1 h-9 rounded-xl px-3 text-xs bg-surface2
                   border border-border text-white placeholder:text-muted
                   focus:outline-none focus:border-accent min-w-0"
            @keydown.enter="addUrlInput"
          />
          <button
            class="chip text-xs shrink-0"
            @click="addUrlInput"
          >添加</button>
        </div>
      </section>

      <!-- ── 风格选择 ──────────────────────────────────────── -->
      <section class="mb-5">
        <p class="text-[11px] text-muted font-semibold uppercase tracking-widest mb-2.5">风格</p>
        <div class="flex gap-2 overflow-x-auto pb-1 scrollbar-hide">
          <button
            v-for="s in STYLES"
            :key="s.key"
            :class="['chip shrink-0', selectedStyle === s.key ? 'chip-active' : '']"
            @click="selectedStyle = s.key"
          >
            {{ s.icon }} {{ s.label }}
          </button>
        </div>
      </section>

      <!-- ── 改动强度滑杆 ──────────────────────────────────── -->
      <section class="mb-5">
        <div class="flex items-center justify-between mb-1.5">
          <p class="text-[11px] text-muted font-semibold uppercase tracking-widest">
            改动强度
          </p>
          <span class="text-xs font-bold" style="color:#00FF88">
            {{ strength < 0.4 ? '微调' : strength > 0.7 ? '大改' : '适中' }}
            ({{ Math.round(strength * 100) }}%)
          </span>
        </div>
        <input
          v-model.number="strength"
          type="range" min="0.1" max="1" step="0.05"
          class="w-full h-1.5 rounded-full accent-accent bg-surface2 cursor-pointer"
        />
        <div class="flex justify-between text-[10px] text-muted mt-0.5">
          <span>微调</span><span>大改</span>
        </div>
      </section>

      <!-- ── 生成按钮 ──────────────────────────────────────── -->
      <button
        :disabled="loading"
        class="w-full h-14 rounded-2xl font-bold text-sm mb-2
               active:scale-95 transition-transform duration-100
               disabled:opacity-50 disabled:cursor-not-allowed
               flex items-center justify-center gap-2"
        style="background:#00FF88;color:#000"
        @click="generate"
      >
        <svg v-if="loading" class="animate-spin h-4 w-4 shrink-0"
             viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor"
                  stroke-opacity=".3" stroke-width="3"/>
          <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                stroke-width="3" stroke-linecap="round"/>
        </svg>
        <span v-if="loading">{{ loadingLabel() }}</span>
        <span v-else>🚀 生成 6 款新设计</span>
      </button>
      <p class="text-center text-xs text-muted mb-5">
        生成后自动评分 · 标出推荐 Top-3
      </p>

      <!-- 错误 -->
      <p v-if="error" class="text-danger text-sm text-center mb-5">{{ error }}</p>

      <!-- ── 生成结果 ──────────────────────────────────────── -->
      <div v-if="generatedImages.length">

        <!-- 工具栏 -->
        <div class="flex items-center justify-between mb-3">
          <p class="text-[11px] text-muted font-semibold uppercase tracking-widest">
            生成结果（{{ generatedImages.length }} 款）
          </p>
          <button
            :class="['text-xs font-semibold px-3 py-1 rounded-full transition-all',
                     showOnlyBest
                       ? 'bg-accent text-black'
                       : 'border border-border text-muted']"
            @click="showOnlyBest = !showOnlyBest"
          >
            {{ showOnlyBest ? '✦ 仅看推荐' : '☆ 仅看推荐' }}
          </button>
        </div>

        <!-- 图片网格 -->
        <div class="grid grid-cols-2 gap-3 mb-6">
          <div
            v-for="img in displayImages"
            :key="img.url"
            class="relative cursor-pointer rounded-2xl overflow-hidden
                   border-2 transition-all duration-200 aspect-[3/4]"
            :class="selected === img.url
              ? 'border-accent'
              : 'border-transparent'"
            @click="selected = img.url; saveOk = false"
          >
            <img :src="img.url" class="w-full h-full object-cover" loading="lazy" />

            <!-- 推荐徽章 -->
            <div v-if="img.recommended"
                 class="absolute top-2 left-2 px-2 py-0.5 rounded-full
                        text-[10px] font-black"
                 style="background:#00FF88;color:#000">
              ⭐ 推荐
            </div>

            <!-- 评分角标 -->
            <div class="absolute top-2 right-2 px-2 py-0.5 rounded-full
                        text-[11px] font-bold bg-black/60 backdrop-blur-sm"
                 :style="{ color: scoreColor(img.score) }">
              {{ img.score }}
            </div>

            <!-- 选中提示 -->
            <div v-if="selected === img.url"
                 class="absolute bottom-2 left-1/2 -translate-x-1/2
                        bg-accent text-black text-[10px] font-bold
                        px-3 py-1 rounded-full whitespace-nowrap">
              ✓ 已选
            </div>
          </div>
        </div>

        <!-- ── 微调区 ────────────────────────────────────── -->
        <div v-if="selected" class="mb-6 space-y-3">
          <p class="text-[11px] text-muted font-semibold uppercase tracking-widest">
            微调选中款
          </p>

          <!-- 已选预览 -->
          <div class="flex gap-3 items-center card p-3 rounded-2xl">
            <img :src="selected" class="w-14 h-14 rounded-xl object-cover shrink-0" />
            <div class="flex-1 min-w-0">
              <p class="text-xs text-muted">已选图片</p>
              <p class="text-xs font-semibold mt-0.5">
                评分：<span :style="{ color: scoreColor(scoreMap[selected] ?? 0) }">
                  {{ scoreMap[selected] ?? '—' }}
                </span>
              </p>
            </div>
          </div>

          <!-- 指令输入 -->
          <div class="flex gap-2">
            <input
              v-model="tweakInstruction"
              type="text"
              placeholder="描述修改指令，例如：改成黑白，更高级"
              class="flex-1 h-11 rounded-xl px-3 text-sm bg-surface2
                     border border-border text-white placeholder:text-muted
                     focus:outline-none focus:border-accent"
              @keydown.enter="doTweak"
            />
            <button
              :disabled="tweaking || !tweakInstruction.trim()"
              class="h-11 px-4 rounded-xl font-bold text-sm
                     active:scale-95 transition-transform duration-100
                     disabled:opacity-50 disabled:cursor-not-allowed
                     flex items-center gap-1.5"
              style="background:#00FF88;color:#000"
              @click="doTweak"
            >
              <svg v-if="tweaking" class="animate-spin h-4 w-4 shrink-0"
                   viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor"
                        stroke-opacity=".3" stroke-width="3"/>
                <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                      stroke-width="3" stroke-linecap="round"/>
              </svg>
              <span>{{ tweaking ? '微调中…' : '再生成' }}</span>
            </button>
          </div>
          <p v-if="tweakError" class="text-xs text-danger">{{ tweakError }}</p>
        </div>

        <!-- ── 保存区 ────────────────────────────────────── -->
        <div v-if="selected" class="space-y-2.5">
          <p v-if="saveOk" class="text-center text-xs font-semibold" style="color:#00FF88">
            ✔ 已保存到款库
          </p>
          <p v-if="saveError" class="text-center text-xs text-danger">{{ saveError }}</p>

          <button
            :disabled="saving || saveOk"
            class="w-full h-12 rounded-full font-bold text-sm
                   active:scale-95 transition-transform duration-100
                   disabled:opacity-50 disabled:cursor-not-allowed
                   flex items-center justify-center gap-2"
            style="background:#00FF88;color:#000"
            @click="doSave"
          >
            <svg v-if="saving" class="animate-spin h-4 w-4 shrink-0"
                 viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor"
                      stroke-opacity=".3" stroke-width="3"/>
              <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                    stroke-width="3" stroke-linecap="round"/>
            </svg>
            <span>{{ saving ? '保存中…' : saveOk ? '✔ 已保存' : '💾 保存到款库' }}</span>
          </button>

          <button
            class="w-full h-11 rounded-full border border-border text-muted
                   text-sm font-medium active:opacity-70 transition-opacity"
            @click="router.push('/me')"
          >
            查看款库 →
          </button>
        </div>

      </div><!-- /generatedImages -->

      <!-- 空状态 -->
      <div v-if="!loading && !generatedImages.length && !error"
           class="text-center py-16">
        <p class="text-4xl mb-4">🎨</p>
        <p class="text-white font-semibold text-lg mb-2">上传参考图，AI 帮你改款</p>
        <p class="text-muted text-sm leading-loose">
          可不上传参考图（AI 纯创作）<br>
          选好风格后点击生成
        </p>
        <p class="mt-4 text-xs" style="color:#00FF88">
          🤖 自动评分 · 标出推荐 Top-3
        </p>
      </div>

    </div><!-- /max-w -->

    <!-- 底部导航 -->
    <nav class="fixed bottom-0 left-0 right-0 z-20
                bg-bg/95 backdrop-blur-md border-t border-border
                flex items-center justify-around
                px-2 pt-2 pb-[calc(.5rem+env(safe-area-inset-bottom))]">
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M3 12l9-9 9 9M5 10v9a1 1 0 001 1h4v-5h4v5h4a1 1 0 001-1v-9"/>
        </svg>
        <span class="text-[10px] font-semibold">首页</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/generate')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/>
        </svg>
        <span class="text-[10px] font-semibold">AI生成</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-accent">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
        </svg>
        <span class="text-[10px] font-semibold">改款</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/template')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"/>
        </svg>
        <span class="text-[10px] font-semibold">模板</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/me')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
        </svg>
        <span class="text-[10px] font-semibold">我的</span>
      </button>
    </nav>

  </div>
</template>

<style scoped>
input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px; height: 18px;
  border-radius: 50%;
  background: #00FF88;
  cursor: pointer;
}
input[type="range"]::-moz-range-thumb {
  width: 18px; height: 18px;
  border-radius: 50%;
  background: #00FF88;
  border: none;
  cursor: pointer;
}
</style>
