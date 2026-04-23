<!--
  TemplateLuxury.vue — 高端奢感 高转化版
  转化增强：金色定价 + 限量感 + 信任背书 + 细边框CTA
-->
<script setup>
import { ref } from 'vue'

const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])
const imgLoaded = ref(false)
const product   = props.shop.products?.[0] || {}

function share() {
  const url = window.location.href
  if (navigator.share) navigator.share({ title: product.title, url })
  else navigator.clipboard?.writeText(url).then(() => alert('链接已复制'))
}
</script>

<template>
  <div class="min-h-screen pb-32 text-center"
       :style="{ background: shop.theme.bg, color: shop.theme.text }">

    <!-- 品牌头 -->
    <header class="py-8 px-5">
      <p class="tracking-[0.4em] text-xs font-light uppercase"
         :style="{ color: shop.theme.subText }">Exclusive</p>
      <h1 class="tracking-[0.3em] text-base font-light uppercase mt-1">Deepay</h1>
    </header>

    <!-- 稀缺感（金色）-->
    <p class="text-xs tracking-widest font-semibold mb-4"
       :style="{ color: shop.theme.primary }">
      ✦ 全球限量 · 仅余 5 件 ✦
    </p>

    <!-- 主图 -->
    <div class="px-8 mb-8">
      <div class="relative w-full aspect-[3/4] rounded-xl overflow-hidden mx-auto max-w-xs">
        <div v-if="!imgLoaded" class="absolute inset-0 animate-pulse"
             :style="{ background: shop.theme.card || '#111' }" />
        <img v-if="product.img" :src="product.img" :alt="product.title"
             class="w-full h-full object-cover transition-opacity duration-1000"
             :class="imgLoaded ? 'opacity-100' : 'opacity-0'"
             @load="imgLoaded = true" />
        <div v-else class="w-full h-full"
             :style="{ background: product.gradient || shop.gradient }" />
      </div>
    </div>

    <!-- 信息 -->
    <div class="px-8 space-y-3">
      <p class="tracking-[0.15em] text-xs uppercase font-light"
         :style="{ color: shop.theme.subText }">Premium Collection</p>
      <h2 class="text-lg font-light tracking-wide">
        {{ product.title || shop.name }}
      </h2>
      <p class="text-2xl font-light tracking-widest"
         :style="{ color: shop.theme.primary }">
        €{{ product.price || '—' }}
      </p>
      <!-- 评分 -->
      <p class="text-sm">⭐ 4.9 <span class="text-xs font-light"
         :style="{ color: shop.theme.subText }">(56 条好评)</span></p>
      <!-- 卖点 -->
      <p class="text-xs leading-relaxed font-light"
         :style="{ color: shop.theme.subText }">
        精选高端材质，匠心工艺，每一件皆为收藏之作。<br>
        7天无理由退换 · 全球顺丰包邮
      </p>
    </div>

    <!-- 固定底部 -->
    <div class="fixed bottom-0 left-0 right-0 z-20 px-8 pt-4
                pb-[calc(1rem+env(safe-area-inset-bottom))]"
         :style="{ background: shop.theme.bg + 'F5', borderTop: `1px solid ${shop.theme.primary}40` }">
      <button class="w-full h-12 text-xs tracking-[0.2em] uppercase font-light mb-2
                     active:opacity-70 transition-opacity duration-200 border"
              :style="{ borderColor: shop.theme.primary, color: shop.theme.primary, background: 'transparent' }"
              @click="emit('buy')">
        Purchase Now
      </button>
      <button class="w-full h-9 text-xs tracking-widest uppercase font-light active:opacity-60"
              :style="{ color: shop.theme.subText }"
              @click="share">
        Share
      </button>
    </div>

  </div>
</template>
