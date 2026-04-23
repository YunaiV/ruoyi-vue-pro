<!--
  TechPack.vue — 设计稿生成
  路径：/ai/techpack
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { generateTechPack } from '@/api/ai'

const router = useRouter()
const route  = useRoute()

const image   = route.query.image ? decodeURIComponent(String(route.query.image)) : null
const style   = route.query.style || 'minimal'

const loading  = ref(false)
const techPack = ref(null)
const error    = ref('')
const copied   = ref(false)

onMounted(() => {
  if (image) generate()
})

async function generate() {
  loading.value  = true
  techPack.value = null
  error.value    = ''
  try {
    const res = await generateTechPack({ image, style })
    techPack.value = res
  } catch (e) {
    error.value = e?.message || '生成失败，请重试'
  } finally {
    loading.value = false
  }
}

function formatText(tp) {
  if (!tp) return ''
  const lines = [
    `款式类型: ${tp.garmentType ?? ''}`,
    `版型: ${tp.fit ?? ''}`,
    `风格: ${tp.style ?? ''}`,
    `面料: ${tp.fabric ?? ''}`,
    '',
    '细节:',
    ...(tp.details ?? []).map(d => `  • ${d}`),
    '',
    '生产备注:',
    ...(tp.productionNotes ?? []).map((n,i) => `  ${i+1}. ${n}`),
  ]
  if (tp.patternNotes) lines.push('', `版型注记: ${tp.patternNotes}`)
  return lines.join('\n')
}

async function copy() {
  try {
    await navigator.clipboard.writeText(formatText(techPack.value))
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch (_) {}
}

function exportPDF() {
  window.print()
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
        <span class="font-semibold text-sm">📋 设计稿</span>
        <span class="text-[10px]" style="color:#F59E0B">AI 技术规格生成</span>
      </div>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-4 pb-28 space-y-4 techpack-print-content">

      <!-- 参考图预览 -->
      <div v-if="image" class="rounded-2xl overflow-hidden">
        <img :src="image" class="w-full max-h-64 object-cover" alt="参考图"/>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-16 gap-4">
        <div class="w-10 h-10 rounded-full border-2 border-t-transparent animate-spin"
             style="border-color:#A855F7;border-top-color:transparent"/>
        <p class="text-muted text-sm">AI 正在生成设计稿…</p>
      </div>

      <!-- 错误 -->
      <p v-if="error" class="text-center text-sm py-8" style="color:#F87171">{{ error }}</p>

      <!-- ── TechPack 卡片 ──────────────────────────────── -->
      <div v-if="techPack" class="rounded-2xl p-5 space-y-5" style="background:#111;border:1px solid #222">

        <!-- 概览 -->
        <div>
          <p class="text-xs font-black uppercase tracking-widest mb-3" style="color:#A855F7">概览</p>
          <div class="grid grid-cols-3 gap-2">
            <div class="rounded-xl p-3 text-center" style="background:#1A1A1A">
              <p class="text-[10px] text-muted mb-1">款式</p>
              <p class="text-xs font-bold">{{ techPack.garmentType ?? '—' }}</p>
            </div>
            <div class="rounded-xl p-3 text-center" style="background:#1A1A1A">
              <p class="text-[10px] text-muted mb-1">版型</p>
              <p class="text-xs font-bold">{{ techPack.fit ?? '—' }}</p>
            </div>
            <div class="rounded-xl p-3 text-center" style="background:#1A1A1A">
              <p class="text-[10px] text-muted mb-1">风格</p>
              <p class="text-xs font-bold">{{ techPack.style ?? '—' }}</p>
            </div>
          </div>
        </div>

        <!-- 配色 -->
        <div v-if="techPack.colors?.length">
          <p class="text-xs font-black uppercase tracking-widest mb-3" style="color:#A855F7">配色方案</p>
          <div class="flex flex-wrap gap-2">
            <div
              v-for="(c, i) in techPack.colors"
              :key="i"
              class="flex items-center gap-2 px-3 py-1.5 rounded-full text-xs"
              style="background:#1A1A1A;border:1px solid #333"
            >
              <span
                class="w-4 h-4 rounded-full shrink-0"
                :style="{ background: c.hex ?? '#888' }"
              />
              {{ c.label ?? c.name ?? c }}
            </div>
          </div>
        </div>

        <!-- 面料 -->
        <div v-if="techPack.fabric">
          <p class="text-xs font-black uppercase tracking-widest mb-2" style="color:#A855F7">面料</p>
          <p class="text-sm" style="color:#E5E7EB">{{ techPack.fabric }}</p>
        </div>

        <!-- 细节列表 -->
        <div v-if="techPack.details?.length">
          <p class="text-xs font-black uppercase tracking-widest mb-3" style="color:#A855F7">设计细节</p>
          <ul class="space-y-2">
            <li
              v-for="(d, i) in techPack.details"
              :key="i"
              class="flex items-start gap-2 text-sm"
              style="color:#E5E7EB"
            >
              <span style="color:#00FF88">◆</span>
              {{ d }}
            </li>
          </ul>
        </div>

        <!-- 生产备注 -->
        <div v-if="techPack.productionNotes?.length">
          <p class="text-xs font-black uppercase tracking-widest mb-3" style="color:#F59E0B">⚠ 生产备注</p>
          <ol class="space-y-2">
            <li
              v-for="(n, i) in techPack.productionNotes"
              :key="i"
              class="flex items-start gap-2 text-sm"
              style="color:#E5E7EB"
            >
              <span class="shrink-0 w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-black"
                    style="background:#F59E0B22;color:#F59E0B;border:1px solid #F59E0B44">{{ i+1 }}</span>
              {{ n }}
            </li>
          </ol>
        </div>

        <!-- 版型注记 -->
        <div v-if="techPack.patternNotes">
          <p class="text-xs font-black uppercase tracking-widest mb-2" style="color:#A855F7">版型注记</p>
          <p class="text-sm italic" style="color:#F59E0B">{{ techPack.patternNotes }}</p>
        </div>
      </div>

      <!-- ── 操作按钮 ──────────────────────────────────── -->
      <div v-if="techPack" class="flex gap-3">
        <button
          class="flex-1 h-12 rounded-full font-bold text-sm active:scale-95 transition-transform"
          style="background:#1A1A1A;border:1px solid #333;color:#fff"
          @click="copy"
        >{{ copied ? '✓ 已复制' : '📋 复制' }}</button>
        <button
          class="flex-1 h-12 rounded-full font-bold text-sm active:scale-95 transition-transform"
          style="background:#1A1A1A;border:1px solid #A855F7;color:#A855F7"
          @click="exportPDF"
        >🖨 导出PDF</button>
        <button
          class="flex-1 h-12 rounded-full font-bold text-sm active:scale-95 transition-transform"
          style="background:#00FF88;color:#000"
          @click="router.back()"
        >← 返回</button>
      </div>

      <!-- 重新生成 -->
      <button
        v-if="!loading && !techPack"
        class="w-full h-12 rounded-full font-bold text-sm active:scale-95 transition-transform"
        style="background:#A855F7;color:#fff"
        @click="generate"
      >📋 生成设计稿</button>

    </div>

    <!-- ── 底部导航 ──────────────────────────────────── -->
    <nav class="fixed bottom-0 left-0 right-0 z-20 bg-bg/95 backdrop-blur-md border-t border-border
                flex items-center justify-around px-2 pt-2 pb-[calc(.5rem+env(safe-area-inset-bottom))]
                print:hidden">
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
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
        </svg>
        <span class="text-[10px] font-semibold">设计稿</span>
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

<style>
@media print {
  body *:not(.techpack-print-content):not(.techpack-print-content *) { visibility: hidden !important; }
  .techpack-print-content, .techpack-print-content * { visibility: visible; }
  .techpack-print-content { position: absolute; left: 0; top: 0; width: 100%; }
}
</style>
