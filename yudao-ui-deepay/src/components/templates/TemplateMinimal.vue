<!--
  TemplateMinimal.vue — 极简黑（主打款）高转化版
  定位：超大产品图 · 极多留白 · 冷感高端
  转化增强：稀缺感 + 信任标识 + 强CTA
-->
<script setup>
import { ref } from 'vue'

const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])

const imgLoaded  = ref(false)
const buying     = ref(false)

function share() {
  const url = window.location.href
  if (navigator.share) navigator.share({ title: props.shop.products?.[0]?.title || 'Deepay', url })
  else navigator.clipboard?.writeText(url).then(() => alert('链接已复制'))
}

async function buy() {
  if (buying.value) return
  buying.value = true
  try { emit('buy') } finally { buying.value = false }
}

const product = props.shop.products?.[0] || {}
</script>

<template>
  <div class="min-h-screen pb-32"
       :style="{ background: shop.theme.bg, color: shop.theme.text }">

    <!-- 顶部品牌栏 -->
    <header class="flex items-center justify-between px-5 py-4"
            :style="{ borderBottom: `1px solid ${shop.theme.border || '#1A1A1A'}` }">
      <span class="text-xs tracking-[0.2em] font-light uppercase"
            :style="{ color: shop.theme.subText }">Collection</span>
      <span class="font-semibold text-sm tracking-widest uppercase">Deepay</span>
      <button class="active:opacity-60" @click="share">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"
             :style="{ color: shop.theme.subText }">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 1 1 0-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 1 0 5.367-2.684 3 3 0 0 0-5.367 2.684zm0 9.316a3 3 0 1 0 5.368 2.684 3 3 0 0 0-5.368-2.684z"/>
        </svg>
      </button>
    </header>

    <!-- 主图 -->
    <div class="relative w-full aspect-[3/4] overflow-hidden">
      <div v-if="!imgLoaded" class="absolute inset-0 animate-pulse"
           :style="{ background: shop.theme.card || '#111' }" />
      <img v-if="product.img" :src="product.img" :alt="product.title"
           class="w-full h-full object-cover transition-opacity duration-700"
           :class="imgLoaded ? 'opacity-100' : 'opacity-0'"
           @load="imgLoaded = true" />
      <div v-else class="w-full h-full"
           :style="{ background: product.gradient || shop.gradient }" />
    </div>

    <!-- 信息区 -->
    <div class="px-5 pt-7 pb-4 space-y-4">

      <!-- 稀缺感 -->
      <p class="text-xs font-semibold" style="color:#FF6B6B">
        🔥 仅剩 12 件
      </p>

      <div>
        <p class="text-xs tracking-[0.15em] uppercase font-light"
           :style="{ color: shop.theme.subText }">Minimal Series</p>
        <h1 class="text-xl font-medium tracking-wide mt-1">
          {{ product.title || shop.name }}
        </h1>
      </div>

      <!-- 价格 -->
      <p class="text-3xl font-light tracking-wider"
         :style="{ color: shop.theme.primary }">
        €{{ product.price || '—' }}
      </p>

      <!-- 卖点 -->
      <p class="text-sm leading-relaxed" :style="{ color: shop.theme.subText }">
        极简设计，专注品质。高端棉质面料，每一件皆为极致追求。
      </p>

      <!-- 社交证明 -->
      <p class="text-sm font-medium">⭐ 4.8 <span class="font-normal text-xs"
         :style="{ color: shop.theme.subText }">(132 条好评)</span></p>

      <!-- 信任标识 -->
      <div class="flex flex-wrap gap-3 pt-1">
        <span v-for="tag in ['✔ 安全支付','✔ 7天保障','✔ 全球发货']" :key="tag"
              class="text-xs" :style="{ color: shop.theme.subText }">{{ tag }}</span>
      </div>
    </div>

    <!-- 固定底部操作栏 -->
    <div class="fixed bottom-0 left-0 right-0 z-20 backdrop-blur-md
                px-5 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]"
         :style="{ background: shop.theme.bg + 'F0', borderTop: `1px solid ${shop.theme.border || '#1A1A1A'}` }">
      <button class="w-full h-14 rounded-full font-bold text-base mb-2
                     active:scale-95 transition-transform duration-100
                     flex items-center justify-center gap-2
                     disabled:opacity-50"
              :style="{ background: shop.theme.primary, color: shop.theme.bg }"
              :disabled="buying"
              @click="buy">
        <svg v-if="buying" class="animate-spin h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity=".3" stroke-width="3"/>
          <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
        </svg>
        {{ buying ? '处理中…' : '立即购买' }}
      </button>
      <button class="w-full h-11 rounded-full text-sm font-medium border
                     active:scale-95 transition-transform duration-100"
              :style="{ borderColor: shop.theme.border || '#1A1A1A', color: shop.theme.subText }"
              @click="share">
        📤 分享给好友
      </button>
    </div>

  </div>
</template>
