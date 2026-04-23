<!--
  TemplateGrid.vue — 电商爆款 高转化版
  转化增强：多品展示 + 稀缺 + 强购买CTA
-->
<script setup>
defineProps({ shop: { type: Object, required: true } })
const emit = defineEmits(['buy', 'share'])

function share() {
  const url = window.location.href
  if (navigator.share) navigator.share({ title: 'Deepay Shop', url })
  else navigator.clipboard?.writeText(url).then(() => alert('链接已复制'))
}
</script>

<template>
  <div class="min-h-screen pb-32"
       :style="{ background: shop.theme.bg, color: shop.theme.text }">

    <!-- 顶部 -->
    <header class="flex items-center justify-between px-4 py-4"
            :style="{ borderBottom: `1px solid ${shop.theme.border || '#E5E5E5'}` }">
      <span class="font-bold text-sm tracking-wide">Deepay</span>
      <span class="text-xs font-semibold px-3 py-1 rounded-full"
            :style="{ background: shop.theme.primary + '20', color: shop.theme.primary }">
        🔥 热卖中
      </span>
      <button class="active:opacity-60" @click="share">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
             :style="{ color: shop.theme.subText }">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 1 1 0-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 1 0 5.367-2.684 3 3 0 0 0-5.367 2.684zm0 9.316a3 3 0 1 0 5.368 2.684 3 3 0 0 0-5.368-2.684z"/>
        </svg>
      </button>
    </header>

    <!-- 店铺名 + 社交证明 -->
    <div class="px-4 py-4">
      <h1 class="text-lg font-bold">{{ shop.name }}</h1>
      <div class="flex items-center gap-3 mt-1">
        <span class="text-sm">⭐ 4.8 <span class="text-xs"
              :style="{ color: shop.theme.subText }">(201 条)</span></span>
        <span class="text-xs font-semibold" style="color:#FF6B6B">仅剩少量库存</span>
      </div>
    </div>

    <!-- 商品网格 -->
    <div class="grid grid-cols-2 gap-3 px-4 mb-4">
      <div v-for="(p, i) in shop.products" :key="i"
           class="rounded-xl overflow-hidden cursor-pointer
                  active:scale-[.98] transition-transform duration-100"
           :style="{ background: shop.theme.card, border: `1px solid ${shop.theme.border || '#E5E5E5'}` }"
           @click="emit('buy')">
        <div class="aspect-square overflow-hidden">
          <img v-if="p.img" :src="p.img" :alt="p.title"
               class="w-full h-full object-cover" />
          <div v-else class="w-full h-full"
               :style="{ background: p.gradient || shop.theme.card }" />
        </div>
        <div class="p-3">
          <p class="text-sm font-medium truncate">{{ p.title }}</p>
          <div class="flex items-center justify-between mt-1">
            <p class="font-bold text-sm" :style="{ color: shop.theme.primary }">€{{ p.price }}</p>
            <span class="text-[10px] px-2 py-0.5 rounded-full font-semibold"
                  :style="{ background: shop.theme.primary, color: shop.theme.bg }">买</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 信任标识 -->
    <div class="flex justify-center gap-5 px-4 pb-4">
      <span v-for="tag in ['✔ 安全支付','✔ 7天退换','✔ 极速发货']" :key="tag"
            class="text-xs" :style="{ color: shop.theme.subText }">{{ tag }}</span>
    </div>

    <!-- 固定底部 -->
    <div class="fixed bottom-0 left-0 right-0 z-20 px-4 pt-3
                pb-[calc(.75rem+env(safe-area-inset-bottom))]"
         :style="{ background: shop.theme.bg + 'F5', borderTop: `1px solid ${shop.theme.border || '#E5E5E5'}` }">
      <button class="w-full h-14 rounded-2xl font-bold text-sm mb-2
                     active:scale-95 transition-transform duration-100"
              :style="{ background: shop.theme.primary, color: shop.theme.bg }"
              @click="emit('buy')">
        全部商品 · 立即购买
      </button>
      <button class="w-full h-10 text-sm font-medium active:opacity-70"
              :style="{ color: shop.theme.subText }"
              @click="share">
        📤 分享店铺
      </button>
    </div>

  </div>
</template>
