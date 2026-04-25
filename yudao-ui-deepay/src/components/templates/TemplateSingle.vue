<!--
  TemplateSingle.vue — 单品爆款 高转化版（最高转化）
  转化增强：超大图 + 大价格 + 稀缺感 + 强买按钮
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
  <div class="min-h-screen pb-36"
       :style="{ background: shop.theme.bg, color: shop.theme.text }">

    <!-- 顶栏 -->
    <header class="flex items-center justify-between px-5 py-4">
      <span class="text-xs tracking-widest uppercase font-light"
            :style="{ color: shop.theme.subText }">Deepay</span>
      <button class="active:opacity-60" @click="share">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"
             :style="{ color: shop.theme.subText }">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 1 1 0-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 1 0 5.367-2.684 3 3 0 0 0-5.367 2.684zm0 9.316a3 3 0 1 0 5.368 2.684 3 3 0 0 0-5.368-2.684z"/>
        </svg>
      </button>
    </header>

    <!-- 超大主图 -->
    <div class="relative w-full overflow-hidden" style="aspect-ratio:3/4;max-height:70vh">
      <div v-if="!imgLoaded" class="absolute inset-0 animate-pulse"
           :style="{ background: shop.theme.card || '#181818' }" />
      <img v-if="product.img" :src="product.img" :alt="product.title"
           class="w-full h-full object-cover transition-opacity duration-700"
           :class="imgLoaded ? 'opacity-100' : 'opacity-0'"
           @load="imgLoaded = true" />
      <div v-else class="w-full h-full"
           :style="{ background: product.gradient || shop.gradient }" />

      <!-- 底部渐变 -->
      <div class="absolute bottom-0 left-0 right-0 h-40"
           :style="`background:linear-gradient(to top,${shop.theme.bg},transparent)`" />

      <!-- 稀缺标签 -->
      <div class="absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-bold"
           style="background:#FF6B6B;color:#fff">
        仅剩 7 件
      </div>

      <!-- 价格浮层 -->
      <div class="absolute bottom-4 left-5">
        <p class="text-xs tracking-widest uppercase font-light mb-1"
           :style="{ color: shop.theme.subText }">{{ product.title || shop.name }}</p>
        <p class="font-black leading-none" style="font-size:2.8rem"
           :style="{ color: shop.theme.primary }">
          €{{ product.price || '—' }}
        </p>
      </div>
    </div>

    <!-- 卖点区 -->
    <div class="px-5 pt-5 space-y-3">
      <p class="text-sm">⭐ 4.9 <span class="text-xs"
         :style="{ color: shop.theme.subText }">(87 条好评)</span></p>
      <p class="text-sm leading-relaxed" :style="{ color: shop.theme.subText }">
        限量款式，聚焦品质。高端工艺，一件即是品牌。
      </p>
      <div class="flex gap-4 pt-1">
        <span v-for="tag in ['✔ 安全支付','✔ 7天保障','✔ 全球发货']" :key="tag"
              class="text-xs" :style="{ color: shop.theme.subText }">{{ tag }}</span>
      </div>
    </div>

    <!-- 固定底部 -->
    <div class="fixed bottom-0 left-0 right-0 z-20 backdrop-blur-sm px-5 pt-3
                pb-[calc(.75rem+env(safe-area-inset-bottom))]"
         :style="{ background: shop.theme.bg + 'E8', borderTop: `1px solid ${shop.theme.border || '#252525'}` }">
      <button class="w-full h-14 rounded-full font-black text-base tracking-wide mb-2
                     active:scale-95 transition-transform duration-100
                     flex items-center justify-center gap-2"
              :style="{ background: shop.theme.primary, color: shop.theme.bg }"
              @click="emit('buy')">
        BUY NOW
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
          <path stroke-linecap="round" stroke-linejoin="round" d="M5 12h14M12 5l7 7-7 7"/>
        </svg>
      </button>
      <button class="w-full h-10 text-sm font-medium active:opacity-70"
              :style="{ color: shop.theme.subText }"
              @click="share">
        📤 分享给朋友
      </button>
    </div>

  </div>
</template>
