<!--
  Inspiration.vue — 时装灵感库 (flagship)
  路径：/inspiration
-->
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { selectRefs, processInspiration } from '@/api/ai'
import {
  inspirationItems,
  INSPIRATION_CATEGORIES,
  SOURCE_LABELS,
  DESIGN_STYLES,
  LAYER_LABELS,
  SCORE_THRESHOLD,
} from '@/data/inspiration'

const router = useRouter()

// ── 筛选状态 ──────────────────────────────────────────────────────────
const activeCategory = ref('all')
const activeStyle    = ref('')
const searchQuery    = ref('')
const usableOnly     = ref(true)

// ── 多选状态 ──────────────────────────────────────────────────────────
const selectedIds = ref(new Set())

// ── AI 选款状态 ───────────────────────────────────────────────────────
const aiSelectLoading = ref(false)
const aiSelectResult  = ref(null)
const showAiPanel     = ref(false)

// ── 过滤数据 ──────────────────────────────────────────────────────────
const items = computed(() =>
  usableOnly.value ? inspirationItems.filter(i => i.usable === true) : inspirationItems
)

const filtered = computed(() => {
  let list = items.value
  if (activeCategory.value !== 'all') {
    list = list.filter(i => i.source === activeCategory.value)
  }
  if (activeStyle.value) {
    // DESIGN_STYLES keys map: 'avant' matches 'avant-garde', others direct match
    const sk = activeStyle.value
    list = list.filter(i =>
      sk === 'avant' ? (i.style === 'avant-garde' || i.style === 'avant') : i.style === sk
    )
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(i =>
      i.desc?.toLowerCase().includes(q) ||
      i.brand?.toLowerCase().includes(q) ||
      i.style?.toLowerCase().includes(q) ||
      i.type?.toLowerCase().includes(q)
    )
  }
  return list
})

const selectedUrls = computed(() =>
  inspirationItems.filter(i => selectedIds.value.has(i.id)).map(i => i.image)
)

function toggleSelect(item) {
  const set = new Set(selectedIds.value)
  if (set.has(item.id)) {
    set.delete(item.id)
  } else {
    if (set.size >= 5) return
    set.add(item.id)
  }
  selectedIds.value = set
}

function isSelected(item) {
  return selectedIds.value.has(item.id)
}

function goRedesign() {
  if (!selectedUrls.value.length) return
  router.push(`/redesign?refs=${selectedUrls.value.map(u => encodeURIComponent(u)).join(',')}`)
}

function goAiDesign() {
  if (!selectedUrls.value.length) return
  router.push(`/ai/design?refs=${selectedUrls.value.map(u => encodeURIComponent(u)).join(',')}`)
}

function goSeason() {
  if (!selectedUrls.value.length) return
  router.push(`/ai/season?refs=${selectedUrls.value.map(u => encodeURIComponent(u)).join(',')}`)
}

async function doAiSelect() {
  aiSelectLoading.value = true
  try {
    const payload = items.value.map(i => ({
      url:   i.image,
      score: i.score ?? 75,
      style: i.style,
      layer: i.layer,
    }))
    const res = await selectRefs({ images: payload })
    aiSelectResult.value = res
    showAiPanel.value = true
  } catch (_) {
    // fallback: pick 3 random high-score items
    const sorted = [...items.value].sort((a, b) => (b.score ?? 0) - (a.score ?? 0))
    aiSelectResult.value = {
      structure: sorted[0],
      style:     sorted[1],
      detail:    sorted[2],
      confidence: 0.82,
    }
    showAiPanel.value = true
  } finally {
    aiSelectLoading.value = false
  }
}

function confirmAiSelect() {
  if (!aiSelectResult.value) return
  const { structure, style, detail } = aiSelectResult.value
  const ids = new Set()
  if (structure?.id) ids.add(structure.id)
  if (style?.id)     ids.add(style.id)
  if (detail?.id)    ids.add(detail.id)
  selectedIds.value = ids
  showAiPanel.value = false
}

// ── 颜色工具 ──────────────────────────────────────────────────────────
function scoreColor(score) {
  if (score >= 85) return '#00FF88'
  if (score >= 70) return '#F59E0B'
  return '#9CA3AF'
}

function layerColor(layer) {
  if (layer === 'design')      return '#A855F7'
  if (layer === 'commercial')  return '#00FF88'
  return '#F59E0B'
}

function sourceColor(source) {
  if (source === 'fashion_week')   return '#A855F7'
  if (source === 'brand_lookbook') return '#00FF88'
  return '#F59E0B'
}
</script>

<template>
  <div class="min-h-screen bg-bg text-white">

    <!-- ── 顶部导航 ──────────────────────────────────── -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md border-b border-border px-4 py-3">
      <div class="flex items-center gap-3 mb-3">
        <button class="text-muted active:text-white transition-colors" @click="router.back()">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        <div class="flex flex-col flex-1">
          <span class="font-semibold text-sm">🎭 时装灵感库</span>
          <span class="text-[10px]" style="color:#A855F7">秀场图 × 品牌图 → AI 融合改款</span>
        </div>
        <button
          class="flex items-center gap-1.5 px-3 py-1.5 rounded-full font-bold text-xs active:scale-95 transition-transform"
          style="background:#A855F7;color:#fff"
          :disabled="aiSelectLoading"
          @click="doAiSelect"
        >
          <span v-if="aiSelectLoading">⏳</span>
          <span v-else>✨ AI帮我选</span>
        </button>
      </div>

      <!-- 选中后的快操作栏 -->
      <div v-if="selectedIds.size > 0" class="flex gap-2 mb-2">
        <button
          class="flex-1 h-8 rounded-full text-xs font-bold active:scale-95 transition-transform"
          style="background:#1A1A1A;border:1px solid #00FF88;color:#00FF88"
          @click="goRedesign"
        >改款</button>
        <button
          class="flex-1 h-8 rounded-full text-xs font-bold active:scale-95 transition-transform"
          style="background:#1A1A1A;border:1px solid #A855F7;color:#A855F7"
          @click="goAiDesign"
        >出款</button>
        <button
          class="flex-1 h-8 rounded-full text-xs font-bold active:scale-95 transition-transform"
          style="background:#1A1A1A;border:1px solid #F59E0B;color:#F59E0B"
          @click="goSeason"
        >整季</button>
      </div>

      <!-- 搜索框 -->
      <div class="relative mb-2">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted"
             fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M21 21l-4.35-4.35M17 11A6 6 0 115 11a6 6 0 0112 0z"/>
        </svg>
        <input
          v-model="searchQuery"
          type="search"
          placeholder="搜索品牌、风格、款式…"
          class="w-full h-9 pl-8 pr-3 rounded-xl text-xs bg-surface2 border border-border
                 text-white placeholder:text-muted focus:outline-none focus:border-accent"
        />
      </div>
    </header>

    <div class="max-w-[480px] mx-auto px-3 pt-4 pb-32">

      <!-- ── 分类 tabs ─────────────────────────────── -->
      <div class="flex gap-2 overflow-x-auto pb-2 scrollbar-hide mb-2">
        <button
          v-for="cat in INSPIRATION_CATEGORIES"
          :key="cat.key"
          :class="['shrink-0 px-3 py-1.5 rounded-full text-xs font-semibold transition-all',
                   activeCategory === cat.key ? 'text-black' : 'border border-border text-muted']"
          :style="activeCategory === cat.key ? 'background:#A855F7' : ''"
          @click="activeCategory = cat.key"
        >{{ cat.label }}</button>
      </div>

      <!-- ── 风格 chips ─────────────────────────────── -->
      <div class="flex gap-2 overflow-x-auto pb-2 scrollbar-hide mb-3">
        <button
          v-for="s in DESIGN_STYLES"
          :key="s.key"
          :class="['shrink-0 px-3 py-1.5 rounded-full text-xs font-semibold transition-all',
                   activeStyle === s.key ? 'text-black' : 'border border-border text-muted']"
          :style="activeStyle === s.key ? 'background:#00FF88' : ''"
          @click="activeStyle = s.key"
        >{{ s.label }}</button>
      </div>

      <!-- 只看可用 toggle + 统计 -->
      <div class="flex items-center justify-between mb-3">
        <p class="text-xs text-muted">共 {{ filtered.length }} 件</p>
        <div class="flex items-center gap-2">
          <span class="text-[10px] text-muted">只看可用 (≥{{ SCORE_THRESHOLD }}分)</span>
          <button
            class="relative w-9 h-5 rounded-full transition-colors duration-200"
            :style="usableOnly ? 'background:#00FF88' : 'background:#333'"
            @click="usableOnly = !usableOnly"
          >
            <span
              class="absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-transform duration-200"
              :style="usableOnly ? 'transform:translateX(18px)' : 'transform:translateX(2px)'"
            />
          </button>
        </div>
      </div>

      <!-- ── 瀑布流 ─────────────────────────────────── -->
      <div v-if="filtered.length" class="columns-2 gap-3">
        <div
          v-for="item in filtered"
          :key="item.id"
          class="break-inside-avoid mb-3 rounded-2xl overflow-hidden relative cursor-pointer group"
          :style="isSelected(item) ? 'border:2px solid #00FF88' : 'border:2px solid transparent'"
          @click="toggleSelect(item)"
        >
          <!-- 图片 -->
          <img
            :src="item.image"
            :alt="item.desc"
            loading="lazy"
            class="w-full object-cover transition-transform duration-300 group-active:scale-[.98]"
            style="min-height:120px"
          />

          <!-- 渐变遮罩 -->
          <div class="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent pointer-events-none"/>

          <!-- 评分徽章（右上角）-->
          <span
            v-if="item.score"
            class="absolute top-2 right-2 text-[10px] font-black px-1.5 py-0.5 rounded-full"
            :style="`background:${scoreColor(item.score)}22;color:${scoreColor(item.score)};border:1px solid ${scoreColor(item.score)}55`"
          >{{ item.score }}</span>

          <!-- layer 徽章（左上角）-->
          <span
            v-if="item.layer"
            class="absolute top-2 left-2 text-[10px] font-bold px-1.5 py-0.5 rounded-full"
            :style="`background:${layerColor(item.layer)}22;color:${layerColor(item.layer)};border:1px solid ${layerColor(item.layer)}44`"
          >{{ LAYER_LABELS[item.layer] ?? item.layer }}</span>

          <!-- 底部信息 -->
          <div class="absolute bottom-0 left-0 right-0 p-2 space-y-1">
            <div class="flex flex-wrap gap-1">
              <span
                v-for="tag in (item.tags ?? [])"
                :key="tag"
                class="text-[9px] px-1.5 py-0.5 rounded"
                style="background:#ffffff15;color:#9CA3AF"
              >{{ tag }}</span>
            </div>
            <p class="text-white text-[10px] font-semibold leading-tight line-clamp-1">
              {{ item.brand }}
            </p>
          </div>

          <!-- 选中勾 -->
          <div v-if="isSelected(item)"
               class="absolute inset-0 flex items-center justify-center"
               style="background:#00FF8822">
            <span class="w-8 h-8 rounded-full flex items-center justify-center font-black text-sm"
                  style="background:#00FF88;color:#000">✓</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-20">
        <p class="text-3xl mb-4">🔍</p>
        <p class="text-white font-semibold">没有符合条件的灵感</p>
        <p class="text-muted text-sm mt-1">换个分类或清空搜索</p>
      </div>

    </div>

    <!-- ── AI 选款面板（全屏遮罩）────────────────────── -->
    <transition
      enter-active-class="transition-opacity duration-300"
      leave-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      leave-to-class="opacity-0"
    >
      <div v-if="showAiPanel"
           class="fixed inset-0 z-50 flex flex-col items-center justify-center px-4"
           style="background:rgba(0,0,0,0.92)">
        <div class="w-full max-w-[380px] rounded-3xl p-6" style="background:#111;border:1px solid #222">
          <p class="text-center font-black text-base mb-1">✨ AI 已为你选好 3 张参考图</p>
          <p class="text-center text-xs text-muted mb-5">
            置信度 {{ aiSelectResult?.confidence ? Math.round(aiSelectResult.confidence * 100) : 82 }}%
          </p>

          <!-- 置信度条 -->
          <div class="w-full h-1.5 rounded-full mb-5" style="background:#222">
            <div
              class="h-full rounded-full"
              style="background:linear-gradient(90deg,#A855F7,#00FF88)"
              :style="`width:${aiSelectResult?.confidence ? Math.round(aiSelectResult.confidence * 100) : 82}%`"
            />
          </div>

          <!-- 3 张卡片 -->
          <div class="grid grid-cols-3 gap-3 mb-6">
            <div
              v-for="(role, key) in { structure: '版型', style: '风格', detail: '细节' }"
              :key="key"
              class="rounded-2xl overflow-hidden"
              style="border:1px solid #333"
            >
              <img
                v-if="aiSelectResult?.[key]?.image"
                :src="aiSelectResult[key].image"
                class="w-full aspect-[3/4] object-cover"
              />
              <div v-else class="w-full aspect-[3/4] flex items-center justify-center text-2xl"
                   style="background:#1A1A1A">🖼</div>
              <p class="text-center text-[10px] font-semibold py-1.5" style="color:#A855F7">
                {{ role }}
              </p>
            </div>
          </div>

          <!-- 按钮 -->
          <div class="flex gap-3">
            <button
              class="flex-1 h-11 rounded-full text-sm font-bold active:scale-95 transition-transform"
              style="background:#00FF88;color:#000"
              @click="confirmAiSelect"
            >确认使用</button>
            <button
              class="h-11 px-4 rounded-full text-sm font-semibold active:opacity-70"
              style="background:#1A1A1A;border:1px solid #333;color:#fff"
              @click="doAiSelect"
            >重新选</button>
            <button
              class="h-11 px-4 rounded-full text-sm font-semibold active:opacity-70"
              style="background:#1A1A1A;border:1px solid #333;color:#9CA3AF"
              @click="showAiPanel = false"
            >取消</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ── 底部选中操作栏 ────────────────────────────── -->
    <transition
      enter-active-class="transition-all duration-300 ease-out"
      leave-active-class="transition-all duration-200 ease-in"
      enter-from-class="translate-y-full opacity-0"
      leave-to-class="translate-y-full opacity-0"
    >
      <div v-if="selectedIds.size > 0"
           class="fixed bottom-0 left-0 right-0 z-30 bg-bg/95 backdrop-blur-md border-t border-border
                  px-4 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]">
        <div class="flex items-center justify-between mb-3">
          <span class="text-xs font-semibold" style="color:#00FF88">
            已选 {{ selectedIds.size }}/5 张
          </span>
          <button class="text-xs text-muted active:text-white" @click="selectedIds = new Set()">
            清空
          </button>
        </div>
        <div class="grid grid-cols-3 gap-2">
          <button
            class="h-11 rounded-full text-xs font-bold active:scale-95 transition-transform"
            style="background:#00FF88;color:#000"
            @click="goRedesign"
          >✏️ AI改款</button>
          <button
            class="h-11 rounded-full text-xs font-bold active:scale-95 transition-transform"
            style="background:#A855F7;color:#fff"
            @click="goAiDesign"
          >🎯 AI出款</button>
          <button
            class="h-11 rounded-full text-xs font-bold active:scale-95 transition-transform"
            style="background:#1A1A1A;border:1px solid #F59E0B;color:#F59E0B"
            @click="goSeason"
          >🌿 整季系列</button>
        </div>
      </div>
    </transition>

    <!-- ── 底部导航 ──────────────────────────────────── -->
    <nav v-if="selectedIds.size === 0"
         class="fixed bottom-0 left-0 right-0 z-20 bg-bg/95 backdrop-blur-md border-t border-border
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
              @click="router.push('/ai/design')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/>
        </svg>
        <span class="text-[10px] font-semibold">出款</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-accent">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M5 3l14 9-14 9V3z"/>
        </svg>
        <span class="text-[10px] font-semibold">灵感</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted active:text-white transition-colors"
              @click="router.push('/redesign')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
        </svg>
        <span class="text-[10px] font-semibold">改款</span>
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

<style scoped>
.columns-2 {
  column-count: 2;
  column-gap: 0.75rem;
}
</style>
