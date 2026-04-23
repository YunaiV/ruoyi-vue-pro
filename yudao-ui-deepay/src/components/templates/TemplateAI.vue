<!--
  TemplateAI.vue — AI生成款（做成商品后的默认展示）
  定位：AI设计图直出，强调独特性
  风格：暗底 · 荧光绿 · 科技感
-->
<script setup>
import { ref } from 'vue'

const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])

const imgLoaded = ref(false)

function share() {
  const url = window.location.href
  if (navigator.share) navigator.share({ title: props.shop.name || 'AI设计款', url })
  else navigator.clipboard?.writeText(url).then(() => alert('链接已复制'))
}
</script>

<template>
  <div
    class="min-h-screen pb-28"
    :style="{ background: shop.theme?.bg || '#0B0B0B', color: shop.theme?.text || '#fff' }"
  >

    <!-- 顶栏 -->
    <header class="flex items-center justify-between px-5 py-4">
      <div class="flex items-center gap-2">
        <span class="w-5 h-5 rounded-md flex items-center justify-center text-[10px] font-black"
              :style="{ background: shop.theme?.primary || '#00FF88', color: shop.theme?.bg || '#0B0B0B' }">
          AI
        </span>
        <span class="text-xs tracking-widest uppercase font-semibold">Deepay</span>
      </div>
      <button class="active:opacity-60" @click="share">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="1.5"
             :style="{ color: shop.theme?.subText || '#9CA3AF' }">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 1 1 0-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 1 0 5.367-2.684 3 3 0 0 0-5.367 2.684zm0 9.316a3 3 0 1 0 5.368 2.684 3 3 0 0 0-5.368-2.684z"/>
        </svg>
      </button>
    </header>

    <!-- AI 生成图 -->
    <div class="relative w-full aspect-square overflow-hidden">
      <div v-if="!imgLoaded"
           class="absolute inset-0 animate-pulse"
           :style="{ background: shop.theme?.card || '#111' }" />
      <img
        v-if="shop.image"
        :src="shop.image"
        :alt="shop.name"
        class="w-full h-full object-cover transition-opacity duration-700"
        :class="imgLoaded ? 'opacity-100' : 'opacity-0'"
        @load="imgLoaded = true"
      />
      <div v-else
           class="w-full h-full flex items-center justify-center"
           :style="{ background: shop.theme?.card || '#111' }">
        <span class="text-6xl opacity-20">✨</span>
      </div>

      <!-- AI 角标 -->
      <div class="absolute top-3 right-3 px-2 py-1 rounded-full text-[10px] font-bold"
           :style="{ background: shop.theme?.primary || '#00FF88', color: shop.theme?.bg || '#0B0B0B' }">
        AI Generated
      </div>
    </div>

    <!-- 信息区 -->
    <div class="px-5 pt-6 space-y-3">
      <div>
        <p class="text-xs tracking-widest uppercase font-light"
           :style="{ color: shop.theme?.subText || '#9CA3AF' }">
          {{ shop.category }} · {{ shop.style }}
        </p>
        <h1 class="text-xl font-bold mt-1">{{ shop.name || 'AI设计款' }}</h1>
      </div>
      <p class="text-3xl font-black"
         :style="{ color: shop.theme?.primary || '#00FF88' }">
        €{{ shop.products?.[0]?.price || '29.99' }}
      </p>
      <p class="text-sm leading-relaxed"
         :style="{ color: shop.theme?.subText || '#9CA3AF' }">
        由 AI 根据实时流行趋势生成的独家设计，全球限量。
      </p>
    </div>

    <!-- 固定底部 -->
    <div
      class="fixed bottom-0 left-0 right-0 z-20 backdrop-blur-md
             px-5 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]"
      :style="{
        background: (shop.theme?.bg || '#0B0B0B') + 'F0',
        borderTop: `1px solid ${shop.theme?.border || '#1A1A1A'}`
      }"
    >
      <div class="flex gap-3">
        <button
          class="h-12 px-5 rounded-full text-sm font-medium border
                 active:scale-95 transition-transform duration-100 shrink-0"
          :style="{
            borderColor: shop.theme?.border || '#1A1A1A',
            color: shop.theme?.subText || '#9CA3AF'
          }"
          @click="share"
        >
          分享
        </button>
        <button
          class="flex-1 h-12 rounded-full font-black text-sm
                 active:scale-95 transition-transform duration-100"
          :style="{ background: shop.theme?.primary || '#00FF88', color: shop.theme?.bg || '#0B0B0B' }"
          @click="emit('buy')"
        >
          立即购买
        </button>
      </div>
    </div>

  </div>
</template>
