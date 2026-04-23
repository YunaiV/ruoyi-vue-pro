<!--
  TemplateStreet.vue — 街头前卫 高转化版
  转化增强：大字冲击 + 红色稀缺感 + 强CTA
-->
<script setup>
const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])

function share() {
  const url = window.location.href
  if (navigator.share) navigator.share({ title: 'Street Drop', url })
  else navigator.clipboard?.writeText(url).then(() => alert('链接已复制'))
}

const minPrice = props.shop.products?.length
  ? Math.min(...props.shop.products.map(p => p.price || 0))
  : 0
</script>

<template>
  <div class="min-h-screen pb-32"
       :style="{ background: shop.theme.bg, color: shop.theme.text }">

    <!-- 顶栏 -->
    <header class="flex items-center justify-between px-4 py-4">
      <span class="text-xs tracking-[0.25em] uppercase font-bold"
            :style="{ color: shop.theme.primary }">DROP</span>
      <span class="font-black text-sm tracking-widest uppercase">DEEPAY</span>
      <button class="active:opacity-60" @click="share">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
             :style="{ color: shop.theme.subText }">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 1 1 0-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 1 0 5.367-2.684 3 3 0 0 0-5.367 2.684zm0 9.316a3 3 0 1 0 5.368 2.684 3 3 0 0 0-5.368-2.684z"/>
        </svg>
      </button>
    </header>

    <!-- 商品图网格 -->
    <div class="grid grid-cols-2 gap-1 px-1 mb-4">
      <div v-for="(p, i) in shop.products.slice(0, 4)" :key="i"
           class="aspect-square overflow-hidden"
           :class="{ 'col-span-2': shop.products.length === 1 }">
        <img v-if="p.img" :src="p.img" :alt="p.title"
             class="w-full h-full object-cover" />
        <div v-else class="w-full h-full"
             :style="{ background: p.gradient || shop.theme.card }" />
      </div>
    </div>

    <!-- 内容区 -->
    <div class="px-4 pb-4 space-y-4">

      <!-- 稀缺感 -->
      <p class="text-xs font-bold tracking-widest" :style="{ color: shop.theme.primary }">
        ⚡ LIMITED DROP — 仅剩 8 件
      </p>

      <!-- 大标题 -->
      <h1 class="font-black leading-none"
          style="font-size: clamp(2rem,10vw,3rem);letter-spacing:-0.02em">
        {{ shop.name?.toUpperCase() || 'STREET DROP' }}
      </h1>

      <!-- 价格 + 评分 -->
      <div class="flex items-center gap-4">
        <span class="font-black text-2xl" :style="{ color: shop.theme.primary }">
          FROM €{{ minPrice }}
        </span>
        <span class="text-sm">⭐ 4.9 <span class="text-xs"
              :style="{ color: shop.theme.subText }">(98 条)</span></span>
      </div>

      <!-- 商品列表 -->
      <div class="space-y-0">
        <div v-for="(p, i) in shop.products" :key="i"
             class="flex items-center justify-between py-3"
             :style="{ borderBottom: `1px solid ${shop.theme.border || '#222'}` }">
          <span class="font-bold text-sm uppercase tracking-wide">{{ p.title }}</span>
          <span class="font-black" :style="{ color: shop.theme.primary }">€{{ p.price }}</span>
        </div>
      </div>

      <!-- 信任标识 -->
      <div class="flex gap-4">
        <span v-for="tag in ['✔ 安全支付','✔ 极速发货','✔ 退换保障']" :key="tag"
              class="text-xs" :style="{ color: shop.theme.subText }">{{ tag }}</span>
      </div>
    </div>

    <!-- 固定底部 -->
    <div class="fixed bottom-0 left-0 right-0 z-20 px-4 pt-3
                pb-[calc(.75rem+env(safe-area-inset-bottom))]"
         :style="{ background: shop.theme.bg, borderTop: `2px solid ${shop.theme.primary}` }">
      <button class="w-full h-14 font-black text-base tracking-widest uppercase
                     active:scale-95 transition-transform duration-100 mb-2"
              :style="{ background: shop.theme.primary, color: shop.theme.bg }"
              @click="emit('buy')">
        SHOP NOW
      </button>
      <button class="w-full h-10 text-sm font-semibold active:opacity-70"
              :style="{ color: shop.theme.subText }"
              @click="share">
        📤 分享给朋友
      </button>
    </div>

  </div>
</template>
