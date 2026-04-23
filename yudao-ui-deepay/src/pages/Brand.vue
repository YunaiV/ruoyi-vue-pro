<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createStyleProfile } from '@/api/ai'

const router = useRouter()

const PRESET_PROFILES = [
  {
    id: 'brand_minimal_01', name: '极简黑白', style: 'minimal',
    colors: ['黑', '白', '灰'], maxColors: 3,
    avoid: ['logo', 'pattern', 'complex graphics', 'bright colors'],
    desc: '干净、克制、可穿 — 高端极简主义'
  },
  {
    id: 'brand_modern_02', name: '现代灰系', style: 'modern',
    colors: ['灰', '米白', '深炭'], maxColors: 3,
    avoid: ['logo', 'print', 'streetwear elements'],
    desc: '现代都市感，结构清晰，中性色调'
  },
  {
    id: 'brand_luxury_03', name: '高端奢华', style: 'luxury',
    colors: ['黑', '米色', '驼色'], maxColors: 3,
    avoid: ['logo', 'graphic', 'casual elements', 'clutter'],
    desc: '奢华精致，面料感强，高端剪裁'
  },
  {
    id: 'brand_avant_04', name: '前卫极简', style: 'avant',
    colors: ['黑', '白'], maxColors: 2,
    avoid: ['print', 'color-block', 'logo', 'decoration'],
    desc: '解构极简，廓形大胆，克制用色'
  },
]

const selected = ref(null)
const customName = ref('')
const saving = ref(false)

function select(p) { selected.value = p }

async function save() {
  if (!selected.value) return
  saving.value = true
  try {
    await createStyleProfile({
      name:  customName.value || selected.value.name,
      style: selected.value.style,
      rules: {
        colors:    selected.value.colors,
        maxColors: selected.value.maxColors,
        avoid:     selected.value.avoid,
        focus:     ['proportion', 'cutting', 'clean silhouette'],
        principle: 'Less is more. Consistency over creativity.',
      }
    })
    router.push('/design')
  } catch (e) {
    router.push('/design')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0A0A0A] text-white pb-32">
    <header class="sticky top-0 z-20 bg-[#0A0A0A]/90 backdrop-blur border-b border-[#222] px-4 py-3 flex items-center gap-3">
      <button @click="$router.back()" class="text-[#6B7280]">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="flex-1">
        <p class="font-bold text-sm">🎭 品牌风格设置</p>
        <p class="text-[10px] text-[#9CA3AF]">锁定设计语言 → 所有设计像同一品牌</p>
      </div>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-5 space-y-4">
      <p class="text-[11px] text-[#6B7280] text-center">选择品牌调性，AI 将始终在此风格内出款</p>

      <div class="space-y-3">
        <div v-for="p in PRESET_PROFILES" :key="p.id"
          @click="select(p)"
          :class="['rounded-2xl p-4 border cursor-pointer transition-all',
                   selected?.id === p.id ? 'border-[#00FF88] bg-[#00FF88]/5' : 'border-[#222] bg-[#111]']">
          <div class="flex items-center justify-between mb-1">
            <span class="font-bold text-sm">{{ p.name }}</span>
            <div v-if="selected?.id === p.id" class="w-5 h-5 rounded-full bg-[#00FF88] flex items-center justify-center">
              <svg class="w-3 h-3 text-black" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
              </svg>
            </div>
          </div>
          <p class="text-xs text-[#9CA3AF] mb-2">{{ p.desc }}</p>
          <div class="flex gap-1.5 flex-wrap">
            <span v-for="c in p.colors" :key="c"
              class="text-[10px] px-2 py-0.5 rounded-full bg-[#1A1A1A] text-[#9CA3AF]">{{ c }}</span>
          </div>
        </div>
      </div>

      <div class="bg-[#111] rounded-2xl p-4 border border-[#222]">
        <p class="text-xs text-[#9CA3AF] mb-2">自定义品牌名称（可选）</p>
        <input v-model="customName" type="text"
          placeholder="输入品牌名称…"
          class="w-full h-10 px-3 rounded-xl bg-[#1A1A1A] border border-[#333] text-sm text-white placeholder-[#444] focus:outline-none focus:border-[#00FF88]"/>
      </div>
    </div>

    <div class="fixed bottom-0 left-0 right-0 bg-[#111] border-t border-[#222] px-4 py-3">
      <button @click="save" :disabled="!selected || saving"
        class="w-full py-3.5 rounded-2xl font-bold text-sm active:scale-95 transition-all"
        :style="selected ? 'background:#00FF88;color:#000' : 'background:#1A1A1A;color:#444'">
        {{ saving ? '保存中…' : '确认使用此风格 →' }}
      </button>
    </div>
  </div>
</template>
