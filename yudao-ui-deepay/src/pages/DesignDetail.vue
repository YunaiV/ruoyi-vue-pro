<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { generateTechPack } from '@/api/ai'

const route  = useRoute()
const router = useRouter()
const image   = ref('')
const style   = ref('minimal')
const tech    = ref(null)
const loading = ref(false)
const error   = ref('')
const copied  = ref(false)

onMounted(async () => {
  image.value = route.query.img   ? decodeURIComponent(route.query.img) : ''
  style.value = route.query.style || 'minimal'
  if (image.value) await generate()
})

async function generate() {
  loading.value = true
  error.value   = ''
  try {
    const res = await generateTechPack({ image: image.value, style: style.value })
    const d   = res.data || res
    tech.value = d.techPack || d
  } catch (e) {
    error.value = '生成失败：' + (e?.message || '请重试')
  } finally {
    loading.value = false
  }
}

async function copy() {
  if (!tech.value) return
  const text = [
    `款式说明：${tech.value.overview || ''}`,
    `版型：${tech.value.fit || ''}`,
    `面料：${tech.value.fabric || ''}`,
    `颜色：${(tech.value.colors || []).join(' / ')}`,
    '',
    '细节：',
    ...(tech.value.details || []).map(d => `  · ${d}`),
    '',
    '打版备注：',
    ...(tech.value.notes || []).map((n, i) => `  ${i + 1}. ${n}`),
    '',
    tech.value.patternNotes || '',
  ].join('\n')
  await navigator.clipboard.writeText(text)
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}

function exportPDF() {
  window.print()
}
</script>

<template>
  <div class="min-h-screen bg-[#0A0A0A] text-white pb-32 print:bg-white print:text-black">

    <!-- Header (hidden when printing) -->
    <header class="sticky top-0 z-20 bg-[#0A0A0A]/90 backdrop-blur border-b border-[#222] px-4 py-3 flex items-center gap-3 print:hidden">
      <button @click="router.back()" class="text-[#6B7280]">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="flex-1">
        <p class="font-bold text-sm">📋 设计稿</p>
        <p class="text-[10px] text-[#9CA3AF]">可直接发给版师 / 工厂</p>
      </div>
      <div class="flex gap-2">
        <button @click="copy"
          class="text-xs px-3 py-1.5 rounded-full border border-[#333] text-[#9CA3AF] active:scale-95 transition-all">
          {{ copied ? '✓ 已复制' : '📋 复制' }}
        </button>
        <button @click="exportPDF"
          class="text-xs px-3 py-1.5 rounded-full font-semibold active:scale-95 transition-all print:hidden"
          style="background:#00FF88;color:#000">
          🖨 导出PDF
        </button>
      </div>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-4 space-y-4 print:px-6 print:pt-6" id="tech-pack-content">

      <!-- Design image -->
      <img v-if="image" :src="image"
           class="w-full max-h-80 object-contain rounded-2xl bg-[#111] print:rounded-none print:max-h-64"/>

      <!-- Loading -->
      <div v-if="loading" class="flex flex-col items-center py-8 gap-3">
        <div class="w-8 h-8 rounded-full border-2 border-[#00FF88] border-t-transparent animate-spin"/>
        <p class="text-xs text-[#6B7280]">正在生成设计说明…</p>
      </div>

      <!-- Error -->
      <div v-if="error" class="bg-red-900/20 rounded-xl p-4 text-red-400 text-sm">
        {{ error }}
        <button @click="generate" class="mt-2 underline text-xs">重试</button>
      </div>

      <!-- TechPack card -->
      <div v-if="tech && !loading" class="space-y-4">

        <!-- Overview -->
        <div class="bg-[#111] rounded-2xl p-4 print:border print:border-gray-300">
          <p class="text-[10px] text-[#6B7280] uppercase tracking-widest mb-1">款式说明</p>
          <p class="font-semibold text-sm leading-relaxed">{{ tech.overview }}</p>
          <div class="flex gap-3 mt-2">
            <span class="text-[11px] bg-[#1A1A1A] px-2 py-1 rounded-lg text-[#9CA3AF] print:bg-gray-100">
              版型：{{ tech.fit }}
            </span>
            <span class="text-[11px] bg-[#1A1A1A] px-2 py-1 rounded-lg text-[#9CA3AF] print:bg-gray-100">
              面料：{{ tech.fabric }}
            </span>
          </div>
        </div>

        <!-- Colors -->
        <div class="bg-[#111] rounded-2xl p-4 print:border print:border-gray-300">
          <p class="text-[10px] text-[#6B7280] uppercase tracking-widest mb-2">配色方案</p>
          <div class="flex gap-2 flex-wrap">
            <div v-for="c in (tech.colors || [])" :key="c"
              class="flex items-center gap-1.5 bg-[#1A1A1A] px-2.5 py-1.5 rounded-xl print:bg-gray-100">
              <div class="w-3 h-3 rounded-full"
                :style="`background:${c === '黑' || c === 'black' ? '#111' :
                          c === '白' || c === 'white' ? '#f5f5f5' :
                          c === '灰' || c === 'grey' ? '#888' :
                          c === '驼色' || c === 'beige' ? '#c8a97e' : '#999'};
                         border:1px solid #444`"/>
              <span class="text-xs text-white print:text-black">{{ c }}</span>
            </div>
          </div>
        </div>

        <!-- Details -->
        <div class="bg-[#111] rounded-2xl p-4 print:border print:border-gray-300">
          <p class="text-[10px] text-[#6B7280] uppercase tracking-widest mb-2">设计细节</p>
          <ul class="space-y-1.5">
            <li v-for="d in (tech.details || [])" :key="d"
              class="flex items-start gap-2 text-sm">
              <span class="text-[#00FF88] mt-0.5 flex-shrink-0">·</span>
              <span class="print:text-black">{{ d }}</span>
            </li>
          </ul>
        </div>

        <!-- Production Notes -->
        <div class="bg-[#111] rounded-2xl p-4 print:border print:border-gray-300">
          <p class="text-[10px] text-[#6B7280] uppercase tracking-widest mb-2">打版备注</p>
          <ol class="space-y-1.5 list-decimal list-inside">
            <li v-for="n in (tech.notes || [])" :key="n"
              class="text-sm text-[#D1D5DB] print:text-black">{{ n }}</li>
          </ol>
        </div>

        <!-- Pattern Notes (for factory) -->
        <div v-if="tech.patternNotes"
          class="rounded-2xl p-4 border border-amber-900/40 bg-amber-900/10 print:border-amber-400">
          <p class="text-[10px] text-amber-400 uppercase tracking-widest mb-1 print:text-amber-700">⚠ 版师须知</p>
          <p class="text-xs text-amber-200 leading-relaxed italic print:text-amber-800">{{ tech.patternNotes }}</p>
        </div>

      </div>

    </div>

    <!-- Bottom actions (not printed) -->
    <div class="fixed bottom-0 left-0 right-0 bg-[#111] border-t border-[#222] px-4 py-3 flex gap-3 print:hidden">
      <button @click="router.push('/design')"
        class="flex-1 py-3 rounded-xl font-bold text-sm border border-[#333] text-[#9CA3AF] active:scale-95">
        ← 返回设计
      </button>
      <button @click="router.push('/')"
        class="flex-1 py-3 rounded-xl font-bold text-sm active:scale-95"
        style="background:#00FF88;color:#000">
        🏠 首页
      </button>
    </div>

  </div>
</template>
