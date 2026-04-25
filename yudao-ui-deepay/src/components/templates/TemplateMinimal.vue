<!--
  TemplateMinimal.vue — 极简黑 · Argon Dark Edition
  设计：Glass-morphism 卡片网格、渐变CTA、星评、心愿单、分类筛选
  参考：Argon Dashboard（Creative Tim）+ vue-storefront 卡片设计语言
-->
<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])

const activeCategory = ref(0)
const wishlist       = ref(new Set())
const cartCount      = ref(0)
const lastAdded      = ref(null)

const categories = ['全部', '上衣', '裤装', '外套', '配饰']

const filteredProducts = computed(() => {
  if (activeCategory.value === 0) return props.shop.products || []
  const cat = categories[activeCategory.value]
  return (props.shop.products || []).filter(p => p.category === cat)
})

function toggleWishlist(i) {
  const s = new Set(wishlist.value)
  s.has(i) ? s.delete(i) : s.add(i)
  wishlist.value = s
}

function addToCart(i) {
  cartCount.value++
  lastAdded.value = i
  setTimeout(() => { lastAdded.value = null }, 700)
}

function badgeGradient(badge) {
  return { NEW: 'linear-gradient(135deg,#00FF88,#00D4FF)', HOT: 'linear-gradient(135deg,#FF6B6B,#FF8E53)', SALE: 'linear-gradient(135deg,#8B5CF6,#6D28D9)' }[badge] || null
}

function share() {
  const url = window.location.href
  if (navigator.share) navigator.share({ title: props.shop.name, url })
  else navigator.clipboard?.writeText(url).then(() => alert('链接已复制'))
}
</script>

<template>
  <div class="min-h-screen pb-28" :style="{ background: shop.theme.bg, color: shop.theme.text }">

    <!-- ── Sticky Header ── -->
    <header class="sticky top-0 z-30 flex items-center justify-between px-4 py-3"
            style="backdrop-filter:blur(20px)"
            :style="{ background: shop.theme.bg + 'EE', borderBottom: `1px solid ${shop.theme.border}` }">
      <span class="font-black text-sm tracking-widest uppercase">DEEPAY</span>
      <div class="flex items-center gap-3">
        <button class="active:opacity-60" @click="share">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
               :style="{ color: shop.theme.subText }">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
          </svg>
        </button>
        <button class="relative active:opacity-60" @click="emit('buy')">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
               :style="{ color: shop.theme.subText }">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
          </svg>
          <span v-if="cartCount > 0"
                class="absolute -top-1.5 -right-1.5 w-4 h-4 rounded-full text-[10px] font-bold flex items-center justify-center"
                :style="{ background: shop.theme.primary, color: shop.theme.bg }">
            {{ cartCount }}
          </span>
        </button>
      </div>
    </header>

    <!-- ── Hero Banner ── -->
    <div class="px-4 pt-5 pb-4">
      <div class="rounded-2xl p-5 relative overflow-hidden"
           :style="{ background: shop.theme.card, border: `1px solid ${shop.theme.border}`,
                     boxShadow: `0 20px 40px rgba(0,0,0,0.4)` }">
        <!-- bg glow -->
        <div class="absolute -top-10 -right-10 w-40 h-40 rounded-full opacity-10 blur-3xl pointer-events-none"
             :style="{ background: shop.theme.primary }"/>
        <div class="relative z-10">
          <p class="text-[10px] tracking-[0.3em] uppercase mb-1" :style="{ color: shop.theme.primary }">
            Minimal Collection
          </p>
          <h1 class="text-xl font-bold">{{ shop.name }}</h1>
          <div class="flex items-center gap-2 mt-1.5">
            <div class="flex">
              <svg v-for="i in 5" :key="i" class="w-3 h-3" viewBox="0 0 20 20" fill="currentColor"
                   :style="{ color: shop.theme.primary }">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
            </div>
            <span class="text-xs" :style="{ color: shop.theme.subText }">4.8 · {{ shop.products?.length || 0 }} 款精选</span>
          </div>
          <div class="flex gap-2 mt-3 flex-wrap">
            <span v-for="tag in ['🔒 安全支付','↩ 7天保障','✈ 全球发货']" :key="tag"
                  class="text-[10px] px-2.5 py-1 rounded-lg"
                  :style="{ background: shop.theme.bg, border: `1px solid ${shop.theme.border}`, color: shop.theme.subText }">
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Category Chips ── -->
    <div class="flex gap-2 px-4 pb-3 overflow-x-auto scrollbar-hide">
      <button v-for="(cat, i) in categories" :key="cat"
              class="shrink-0 px-4 py-2 rounded-xl text-xs font-semibold transition-all duration-200 active:scale-95"
              :style="activeCategory === i
                ? { background: `linear-gradient(135deg,${shop.theme.primary},${shop.theme.primary}99)`,
                    color: shop.theme.bg, boxShadow: `0 4px 16px ${shop.theme.primary}40` }
                : { background: shop.theme.card, border: `1px solid ${shop.theme.border}`, color: shop.theme.subText }"
              @click="activeCategory = i">
        {{ cat }}
      </button>
    </div>

    <!-- ── Product Grid ── -->
    <div class="grid grid-cols-2 gap-3 px-3 pb-6">
      <div v-for="(p, i) in filteredProducts" :key="i"
           class="rounded-2xl overflow-hidden transition-all duration-200 active:scale-[.97]"
           style="backdrop-filter:blur(20px)"
           :style="{ background: 'rgba(17,17,17,0.75)',
                     border: '1px solid rgba(255,255,255,0.06)',
                     boxShadow: '0 20px 27px rgba(0,0,0,0.35)' }">

        <!-- Image -->
        <div class="relative overflow-hidden" style="aspect-ratio:1/1;border-radius:16px 16px 0 0">
          <img v-if="p.img" :src="p.img" :alt="p.title || p.name" class="w-full h-full object-cover"/>
          <div v-else class="w-full h-full" :style="{ background: p.gradient || shop.theme.card }"/>

          <!-- Badge -->
          <span v-if="p.badge"
                class="absolute top-2 left-2 text-[10px] font-bold px-2 py-0.5 rounded-full text-white"
                :style="{ background: badgeGradient(p.badge), boxShadow: '0 4px 12px rgba(0,0,0,0.5)' }">
            {{ p.badge }}
          </span>

          <!-- Wishlist -->
          <button class="absolute top-2 right-2 w-7 h-7 rounded-full flex items-center justify-center active:scale-90 transition-transform"
                  style="background:rgba(0,0,0,0.55);backdrop-filter:blur(8px)"
                  @click.stop="toggleWishlist(i)">
            <svg class="h-3.5 w-3.5 transition-colors duration-200"
                 :fill="wishlist.has(i) ? 'currentColor' : 'none'"
                 viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                 :style="{ color: wishlist.has(i) ? '#FF6B6B' : 'rgba(255,255,255,0.75)' }">
              <path stroke-linecap="round" stroke-linejoin="round"
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="p-3 space-y-1.5">
          <p class="text-xs font-semibold leading-tight line-clamp-1">{{ p.title || p.name }}</p>

          <!-- Stars -->
          <div class="flex items-center gap-1">
            <div class="flex">
              <svg v-for="s in 5" :key="s" class="w-2.5 h-2.5" viewBox="0 0 20 20" fill="currentColor"
                   :style="{ color: s <= Math.round(p.rating || 5) ? shop.theme.primary : shop.theme.border }">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
            </div>
            <span class="text-[10px]" :style="{ color: shop.theme.subText }">({{ p.reviews || 0 }})</span>
          </div>

          <p v-if="p.desc" class="text-[10px] leading-tight line-clamp-1" :style="{ color: shop.theme.subText }">
            {{ p.desc }}
          </p>

          <!-- Price -->
          <div class="flex items-baseline gap-1.5">
            <span class="text-sm font-bold" :style="{ color: shop.theme.primary }">€{{ p.price }}</span>
            <span v-if="p.originalPrice" class="text-[10px] line-through" :style="{ color: shop.theme.subText }">
              €{{ p.originalPrice }}
            </span>
          </div>

          <!-- Add to cart -->
          <button class="w-full h-8 rounded-xl text-[11px] font-bold flex items-center justify-center gap-1.5 active:scale-95 transition-all duration-200"
                  :style="lastAdded === i
                    ? { background: `linear-gradient(135deg,${shop.theme.primary},${shop.theme.primary}BB)`,
                        color: shop.theme.bg, boxShadow: `0 4px 16px ${shop.theme.primary}40` }
                    : { background: `${shop.theme.primary}18`, color: shop.theme.primary,
                        border: `1px solid ${shop.theme.primary}44` }"
                  @click="addToCart(i); emit('buy')">
            <svg class="h-3 w-3 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round"
                    d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
            </svg>
            {{ lastAdded === i ? '✓ 已加入' : '加购' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ── Fixed Bottom Bar ── -->
    <div class="fixed bottom-0 left-0 right-0 z-20 px-4 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]"
         style="backdrop-filter:blur(24px)"
         :style="{ background: shop.theme.bg + 'F0', borderTop: `1px solid ${shop.theme.border}` }">
      <div class="flex items-center gap-3">
        <span v-if="cartCount > 0" class="shrink-0 text-xs" :style="{ color: shop.theme.subText }">
          已选 <strong :style="{ color: shop.theme.primary }">{{ cartCount }}</strong> 件
        </span>
        <button class="flex-1 h-12 rounded-xl font-bold text-sm flex items-center justify-center gap-2
                       active:scale-95 transition-all duration-200"
                :style="{ background: `linear-gradient(135deg,${shop.theme.primary},${shop.theme.primary}BB)`,
                          color: shop.theme.bg, boxShadow: `0 8px 24px ${shop.theme.primary}40` }"
                @click="emit('buy')">
          <svg class="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
          </svg>
          立即结算
        </button>
        <button class="w-12 h-12 rounded-xl flex items-center justify-center active:opacity-70"
                :style="{ background: shop.theme.card, border: `1px solid ${shop.theme.border}` }"
                @click="share">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
               :style="{ color: shop.theme.subText }">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
          </svg>
        </button>
      </div>
    </div>

  </div>
</template>
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
