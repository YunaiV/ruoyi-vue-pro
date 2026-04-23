<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  selectRefs, generateCollection, deduplicateAdvanced,
  designScore, refineImage, saveDesign
} from '@/api/ai'
import { initUserId } from '@/utils/user'

const route  = useRoute()
const router = useRouter()
const userId = initUserId()

// ── State ────────────────────────────────────────────────────────────
const inputRefs      = ref([])          // raw URLs from /inspiration
const autoRefs       = ref({ structure: null, style: null, detail: null })
const brandId        = ref('brand_minimal_01')
const style          = ref('minimal')
const designs        = ref([])          // { url, score, level, tags, similarity, status }
const selected       = ref(null)
const loading        = ref(false)
const loadingStep    = ref('')
const error          = ref('')
const refineResults  = ref([])
const showRefine     = ref(false)

const BRAND_PRESETS = [
  { id: 'brand_minimal_01', name: '极简黑白', style: 'minimal' },
  { id: 'brand_modern_02',  name: '现代灰系', style: 'modern'  },
  { id: 'brand_luxury_03',  name: '高端奢华', style: 'luxury'  },
  { id: 'brand_avant_04',   name: '前卫极简', style: 'avant'   },
]

// ── Init: read refs from query param ─────────────────────────────────
onMounted(() => {
  if (route.query.refs) {
    inputRefs.value = route.query.refs.split(',').map(u => decodeURIComponent(u)).filter(Boolean)
  }
  if (route.query.style) style.value = route.query.style
})

// ── Step 1: AI auto-select 3 refs ────────────────────────────────────
async function doSelectRefs() {
  if (inputRefs.value.length === 0) return
  try {
    const res = await selectRefs({
      images: inputRefs.value.map(u => ({ url: u, score: 75 }))
    })
    const d = res.data || res
    autoRefs.value = {
      structure: d.structure || inputRefs.value[0] || null,
      style:     d.style     || inputRefs.value[1] || null,
      detail:    d.detail    || inputRefs.value[2] || null,
    }
  } catch (e) {
    // fallback: assign manually
    autoRefs.value = {
      structure: inputRefs.value[0] || null,
      style:     inputRefs.value[1] || null,
      detail:    inputRefs.value[2] || null,
    }
  }
}

// ── Step 2-4: Generate → Deduplicate → Score ─────────────────────────
async function handleGenerate() {
  if (Object.values(autoRefs.value).every(v => !v) && inputRefs.value.length === 0) {
    error.value = '请先从灵感库选择参考图'
    return
  }
  error.value = ''
  loading.value = true
  designs.value = []
  selected.value = null
  showRefine.value = false

  try {
    // Auto-select refs if not done
    if (!autoRefs.value.structure && inputRefs.value.length > 0) {
      loadingStep.value = '🤖 AI正在分析参考图…'
      await doSelectRefs()
    }

    const refs = Object.values(autoRefs.value).filter(Boolean)

    // Step 2: generate
    loadingStep.value = '🎨 生成设计系列（6张）…'
    const genRes = await generateCollection({
      refs,
      styleProfileId: brandId.value,
      style: style.value,
      count: 6,
      userId,
    })
    const genData = genRes.data || genRes
    let list = (genData.images || []).map(url => ({ url }))

    // Step 3: deduplication
    loadingStep.value = '🔍 防撞检测…'
    let dedupeMap = {}
    try {
      const dedupeRes = await deduplicateAdvanced({
        generated: list.map(i => i.url),
        refs,
        library: [],
      })
      const dd = dedupeRes.data || dedupeRes
      ;(dd.results || []).forEach(r => { dedupeMap[r.image] = r })
    } catch (_) {}

    // Step 4: design score
    loadingStep.value = '⭐ 质量评分…'
    let scoreMap = {}
    try {
      const scoreRes = await designScore({
        images: list.map(i => i.url),
        style: style.value,
      })
      const sd = scoreRes.data || scoreRes
      ;(sd.results || []).forEach(r => { scoreMap[r.image] = r })
    } catch (_) {}

    // Merge + filter rejected
    designs.value = list
      .map(item => ({
        url:        item.url,
        score:      scoreMap[item.url]?.score      ?? 70,
        level:      scoreMap[item.url]?.level      ?? 'B',
        tags:       scoreMap[item.url]?.tags       ?? [],
        advice:     scoreMap[item.url]?.advice     ?? '',
        similarity: dedupeMap[item.url]?.similarity ?? 0,
        status:     dedupeMap[item.url]?.status    ?? 'ok',
      }))
      .filter(d => d.status !== 'reject')
      .sort((a, b) => b.score - a.score)

  } catch (e) {
    error.value = '生成失败：' + (e?.message || '请重试')
  } finally {
    loading.value = false
    loadingStep.value = ''
  }
}

// ── Step 5: Refine selected ───────────────────────────────────────────
async function handleRefine() {
  if (!selected.value) return
  loading.value = true
  loadingStep.value = '✨ 精修中…'
  try {
    const res = await refineImage({ image: selected.value.url, style: style.value, userId })
    const d = res.data || res
    refineResults.value = (d.images || []).map(url => ({
      url, score: 0, level: 'B', tags: ['refined'], similarity: 0, status: 'ok'
    }))
    showRefine.value = true
  } catch (e) {
    error.value = '精修失败：' + (e?.message || '')
  } finally {
    loading.value = false
    loadingStep.value = ''
  }
}

// ── Step 6: Go to TechPack ────────────────────────────────────────────
function goDetail() {
  if (!selected.value) return
  router.push(`/design/detail?img=${encodeURIComponent(selected.value.url)}&style=${style.value}`)
}

// ── Helpers ───────────────────────────────────────────────────────────
const top3 = computed(() => designs.value.slice(0, 3))
const hasRef = computed(() => inputRefs.value.length > 0 || Object.values(autoRefs.value).some(Boolean))

function levelColor(level) {
  return level === 'A' ? '#00FF88' : level === 'B' ? '#F59E0B' : '#9CA3AF'
}
function scoreLabel(score) {
  if (score >= 85) return '#00FF88'
  if (score >= 70) return '#F59E0B'
  return '#9CA3AF'
}
function selectDesign(item) {
  selected.value = selected.value?.url === item.url ? null : item
}
</script>

<template>
  <div class="min-h-screen bg-[#0A0A0A] text-white pb-32">

    <!-- Header -->
    <header class="sticky top-0 z-20 bg-[#0A0A0A]/90 backdrop-blur border-b border-[#222] px-4 py-3 flex items-center gap-3">
      <button @click="$router.back()" class="text-[#6B7280] active:text-white">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="flex-1">
        <p class="font-bold text-sm">🎨 AI设计流水线</p>
        <p class="text-[10px] text-[#A855F7]">参考图 → 生成 → 防撞 → 评分 → 设计稿</p>
      </div>
      <button @click="$router.push('/brand')"
        class="text-[10px] px-2 py-1 rounded-full border border-[#333] text-[#9CA3AF]">
        品牌风格
      </button>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-4 space-y-5">

      <!-- ① 参考图区 -->
      <div class="bg-[#111] rounded-2xl p-4">
        <div class="flex items-center justify-between mb-3">
          <p class="text-xs font-semibold text-[#9CA3AF]">参考图</p>
          <button v-if="inputRefs.length === 0"
            @click="$router.push('/inspiration')"
            class="text-xs px-3 py-1.5 rounded-full font-bold"
            style="background:#A855F7;color:#fff">
            + 从灵感库选图
          </button>
          <button v-else @click="doSelectRefs"
            class="text-xs px-3 py-1.5 rounded-full font-bold"
            style="background:#1F2937;color:#A855F7">
            🤖 重新分析
          </button>
        </div>

        <!-- Ref slots: structure / style / detail -->
        <div v-if="autoRefs.structure || autoRefs.style || autoRefs.detail"
             class="grid grid-cols-3 gap-2">
          <div v-for="(entry, key) in { '版型': autoRefs.structure, '风格': autoRefs.style, '细节': autoRefs.detail }"
               :key="key" class="relative">
            <img v-if="entry" :src="entry" class="w-full aspect-square object-cover rounded-xl"/>
            <div v-else class="w-full aspect-square rounded-xl bg-[#1A1A1A] flex items-center justify-center text-[#444] text-xs">
              {{ key }}
            </div>
            <span class="absolute bottom-1 left-1 text-[9px] bg-black/70 text-white px-1 rounded">{{ key }}</span>
          </div>
        </div>
        <div v-else-if="inputRefs.length > 0" class="flex gap-2 overflow-x-auto pb-1">
          <img v-for="(url, i) in inputRefs.slice(0,5)" :key="i"
               :src="url" class="w-16 h-16 object-cover rounded-xl flex-shrink-0"/>
        </div>
        <div v-else class="text-center py-6 text-[#444] text-xs">
          暂无参考图 → 点击"从灵感库选图"
        </div>
      </div>

      <!-- ② 品牌风格 + 生成按钮 -->
      <div class="bg-[#111] rounded-2xl p-4 space-y-3">
        <p class="text-xs font-semibold text-[#9CA3AF]">品牌风格</p>
        <div class="grid grid-cols-2 gap-2">
          <button v-for="b in BRAND_PRESETS" :key="b.id"
            @click="brandId = b.id; style = b.style"
            :class="['py-2 px-3 rounded-xl text-xs font-semibold transition-all',
                     brandId === b.id ? 'text-black' : 'border border-[#333] text-[#9CA3AF]']"
            :style="brandId === b.id ? 'background:#00FF88' : ''">
            {{ b.name }}
          </button>
        </div>

        <button @click="handleGenerate" :disabled="loading"
          class="w-full py-3.5 rounded-2xl font-bold text-sm transition-all active:scale-95"
          :style="loading ? 'background:#1F2937;color:#6B7280' : 'background:#00FF88;color:#000'">
          <span v-if="loading">{{ loadingStep || '生成中…' }}</span>
          <span v-else>🚀 生成设计系列（6张）</span>
        </button>
      </div>

      <!-- Error -->
      <p v-if="error" class="text-red-400 text-xs text-center">{{ error }}</p>

      <!-- ③ 设计结果 -->
      <div v-if="designs.length > 0">
        <div class="flex items-center gap-2 mb-3">
          <p class="text-sm font-bold">设计结果</p>
          <span class="text-xs text-[#6B7280]">{{ designs.length }} 款（已过滤相似款）</span>
          <span class="ml-auto text-[10px] text-[#A855F7]">Top3 推荐 ↓</span>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div v-for="(item, idx) in designs" :key="item.url"
            @click="selectDesign(item)"
            :class="['relative rounded-2xl overflow-hidden cursor-pointer transition-all',
                     selected?.url === item.url ? 'ring-2 ring-[#00FF88]' : 'ring-1 ring-[#222]']">

            <!-- Image -->
            <img :src="item.url" class="w-full aspect-[3/4] object-cover"/>

            <!-- Score badge -->
            <div class="absolute top-2 right-2 text-[11px] font-bold px-2 py-0.5 rounded-full"
                 :style="`background:${scoreLabel(item.score)}22;color:${scoreLabel(item.score)};border:1px solid ${scoreLabel(item.score)}55`">
              {{ item.score }}
            </div>

            <!-- Level badge -->
            <div class="absolute top-2 left-2 text-[10px] font-bold px-1.5 py-0.5 rounded-full"
                 :style="`background:${levelColor(item.level)}22;color:${levelColor(item.level)}`">
              {{ item.level }}
            </div>

            <!-- Top3 crown -->
            <div v-if="idx < 3" class="absolute bottom-2 left-2 text-[11px]">
              {{ idx === 0 ? '👑' : idx === 1 ? '⭐' : '✨' }}
            </div>

            <!-- Selected overlay -->
            <div v-if="selected?.url === item.url"
                 class="absolute inset-0 flex items-center justify-center bg-[#00FF88]/10">
              <div class="w-8 h-8 rounded-full bg-[#00FF88] flex items-center justify-center">
                <svg class="w-5 h-5 text-black" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
                </svg>
              </div>
            </div>

            <!-- Tags -->
            <div v-if="item.tags?.length" class="absolute bottom-2 right-2 flex flex-wrap gap-1 max-w-[80px] justify-end">
              <span v-for="t in item.tags.slice(0,2)" :key="t"
                class="text-[9px] bg-black/70 text-white px-1 rounded">{{ t }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ④ 精修结果 -->
      <div v-if="showRefine && refineResults.length">
        <div class="flex items-center gap-2 mb-3">
          <p class="text-sm font-bold text-[#A855F7]">✨ 精修结果</p>
          <button @click="showRefine=false" class="ml-auto text-[10px] text-[#6B7280]">收起</button>
        </div>
        <div class="flex gap-3 overflow-x-auto pb-2">
          <div v-for="item in refineResults" :key="item.url"
            @click="selectDesign(item)"
            :class="['flex-shrink-0 w-36 relative rounded-2xl overflow-hidden cursor-pointer',
                     selected?.url === item.url ? 'ring-2 ring-[#A855F7]' : 'ring-1 ring-[#333]']">
            <img :src="item.url" class="w-full aspect-[3/4] object-cover"/>
            <div class="absolute top-1 right-1 text-[9px] bg-[#A855F7]/20 text-[#A855F7] px-1.5 rounded-full">精修</div>
          </div>
        </div>
      </div>

    </div>

    <!-- ⑤ Bottom action bar -->
    <div v-if="selected" class="fixed bottom-0 left-0 right-0 bg-[#111] border-t border-[#222] px-4 py-3 flex gap-3">
      <button @click="handleRefine" :disabled="loading"
        class="flex-1 py-3 rounded-xl font-bold text-sm border border-[#A855F7] text-[#A855F7] active:scale-95 transition-all">
        ✨ 精修
      </button>
      <button @click="goDetail"
        class="flex-1 py-3 rounded-xl font-bold text-sm active:scale-95 transition-all"
        style="background:#00FF88;color:#000">
        📋 生成设计稿
      </button>
    </div>

  </div>
</template>
