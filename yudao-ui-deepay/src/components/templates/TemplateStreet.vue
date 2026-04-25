<!--
  TemplateStreet.vue — 街头风 · Argon Street Drop Edition
  设计：全宽Featured大图 · Mosaic网格 · 超大装饰文字 · 商品详情弹层 · 红色渐变CTA
-->
<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])

const selectedProduct = ref(null)
const wishlist        = ref(new Set())
const cartCount       = ref(0)

const allProducts = computed(() => props.shop.products || [])
const featured    = computed(() => allProducts.value[0] || null)
const rest        = computed(() => allProducts.value.slice(1))

function openProduct(p)  { selectedProduct.value = p }
function closeProduct()  { selectedProduct.value = null }

function toggleWishlist(name) {
  const s = new Set(wishlist.value)
  s.has(name) ? s.delete(name) : s.add(name)
  wishlist.value = s
}

function share(product) {
  const url   = window.location.href
  const title = product?.title || product?.name || props.shop.name
  if (navigator.share) navigator.share({ title, url })
  else navigator.clipboard?.writeText(url).then(() => alert('链接已复制'))
}

function buyProduct() {
  closeProduct()
  cartCount.value++
  emit('buy')
}
</script>

<template>
  <div class="min-h-screen pb-28 overflow-x-hidden"
       :style="{ background: shop.theme.bg, color: shop.theme.text, fontFamily: 'Poppins,sans-serif' }">

    <!-- ══ HEADER ══ -->
    <header class="flex items-center justify-between px-4 py-4">
      <div>
        <p class="text-[10px] font-bold tracking-[0.4em] uppercase" :style="{ color: shop.theme.primary }">DROP</p>
        <p class="font-black text-sm tracking-widest uppercase">DEEPAY</p>
      </div>
      <div class="flex items-center gap-3">
        <span class="text-[10px] font-bold px-3 py-1.5 rounded-full"
              style="background:linear-gradient(135deg,#FF6B6B,#FF3B3B);color:#fff;box-shadow:0 4px 14px rgba(255,59,59,0.45)">
          ⚡ LIMITED
        </span>
        <button class="relative active:opacity-60" @click="emit('buy')">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
               :style="{ color: shop.theme.subText }">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
          </svg>
          <span v-if="cartCount > 0"
                class="absolute -top-1.5 -right-1.5 w-4 h-4 rounded-full text-[9px] font-bold flex items-center justify-center"
                :style="{ background: shop.theme.primary, color: shop.theme.bg }">{{ cartCount }}</span>
        </button>
        <button class="active:opacity-60" @click="share(null)">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
               :style="{ color: shop.theme.subText }">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
          </svg>
        </button>
      </div>
    </header>

    <!-- Oversized decorative name -->
    <div class="relative px-4 mb-3 overflow-hidden" style="height:80px">
      <p class="absolute font-black leading-none select-none pointer-events-none uppercase"
         style="top:-8px;left:8px;opacity:0.04;font-size:80px;letter-spacing:-0.04em">
        {{ (shop.name||'STREET').toUpperCase() }}
      </p>
      <div class="relative z-10 pt-3">
        <h1 class="font-black text-3xl leading-none tracking-[-0.02em]">
          {{ (shop.name||'STREET DROP').toUpperCase() }}
        </h1>
        <div class="flex items-center gap-3 mt-1.5">
          <span class="text-sm font-black" :style="{ color: shop.theme.primary }">
            FROM €{{ (shop.products||[]).length ? Math.min(...(shop.products||[]).map(p=>p.price||0)).toFixed(2) : '0.00' }}
          </span>
          <span class="text-xs" :style="{ color: shop.theme.subText }">
            ⭐ 4.9 · {{ (shop.products||[]).length }} 款
          </span>
        </div>
      </div>
    </div>

    <!-- ══ PRODUCT MOSAIC ══ -->
    <div class="px-2 space-y-2 mb-4">
      <!-- Featured full-width -->
      <div v-if="featured"
           class="relative w-full overflow-hidden cursor-pointer active:opacity-90 transition-opacity"
           style="height:280px;border-radius:16px"
           :style="{ border:`1px solid ${shop.theme.border}`, boxShadow:'0 20px 40px rgba(0,0,0,0.5)' }"
           @click="openProduct(featured)">
        <img v-if="featured.img" :src="featured.img" :alt="featured.title" class="w-full h-full object-cover"/>
        <div v-else class="w-full h-full" :style="{ background: featured.gradient||shop.theme.card }"/>
        <div class="absolute inset-0" style="background:linear-gradient(to top,rgba(0,0,0,0.78) 0%,transparent 55%)"/>
        <div class="absolute bottom-3 left-3 right-10">
          <p class="text-[10px] font-bold uppercase tracking-wide mb-0.5" :style="{ color: shop.theme.primary }">★ FEATURED DROP</p>
          <p class="font-bold text-base leading-tight line-clamp-1">{{ featured.title||featured.name }}</p>
          <p class="font-black text-xl mt-0.5" :style="{ color: shop.theme.primary }">€{{ featured.price }}</p>
        </div>
        <span v-if="featured.badge"
              class="absolute top-3 left-3 text-[9px] font-black px-2 py-0.5 rounded-sm"
              :style="{ background: shop.theme.primary, color: shop.theme.bg }">{{ featured.badge }}</span>
        <button class="absolute top-3 right-3 w-8 h-8 rounded-full flex items-center justify-center"
                style="background:rgba(0,0,0,0.55);backdrop-filter:blur(8px)"
                @click.stop="toggleWishlist(featured.name)">
          <svg class="h-4 w-4" :fill="wishlist.has(featured.name)?'currentColor':'none'"
               viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
               :style="{ color: wishlist.has(featured.name)?'#FF6B6B':'rgba(255,255,255,0.85)' }">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
          </svg>
        </button>
        <!-- Share on image -->
        <button class="absolute bottom-3 right-3 w-8 h-8 rounded-full flex items-center justify-center"
                style="background:rgba(0,0,0,0.55);backdrop-filter:blur(8px)"
                @click.stop="share(featured)">
          <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
               :style="{ color: 'rgba(255,255,255,0.7)' }">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
          </svg>
        </button>
      </div>

      <!-- 2-col grid for the rest -->
      <div v-if="rest.length" class="grid grid-cols-2 gap-2">
        <div v-for="(p,i) in rest" :key="i"
             class="relative overflow-hidden cursor-pointer active:opacity-90 transition-opacity"
             style="height:160px;border-radius:12px"
             :style="{ border:`1px solid ${shop.theme.border}`, boxShadow:'0 8px 24px rgba(0,0,0,0.4)' }"
             @click="openProduct(p)">
          <img v-if="p.img" :src="p.img" :alt="p.title" class="w-full h-full object-cover"/>
          <div v-else class="w-full h-full" :style="{ background: p.gradient||shop.theme.card }"/>
          <div class="absolute inset-0" style="background:linear-gradient(to top,rgba(0,0,0,0.82) 0%,transparent 55%)"/>
          <div class="absolute bottom-2 left-2 right-2">
            <p class="text-xs font-bold truncate leading-tight">{{ p.title||p.name }}</p>
            <p class="text-sm font-black" :style="{ color: shop.theme.primary }">€{{ p.price }}</p>
          </div>
          <span v-if="p.badge"
                class="absolute top-2 left-2 text-[9px] font-black px-1.5 py-0.5 rounded-sm"
                :style="{ background: shop.theme.primary, color: shop.theme.bg }">{{ p.badge }}</span>
          <button class="absolute top-2 right-2 w-6 h-6 rounded-full flex items-center justify-center"
                  style="background:rgba(0,0,0,0.55)"
                  @click.stop="toggleWishlist(p.name)">
            <svg class="h-3 w-3" :fill="wishlist.has(p.name)?'currentColor':'none'"
                 viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                 :style="{ color: wishlist.has(p.name)?'#FF6B6B':'rgba(255,255,255,0.85)' }">
              <path stroke-linecap="round" stroke-linejoin="round"
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Trust row -->
    <div class="flex gap-2 px-4 pb-4 overflow-x-auto scrollbar-hide">
      <span v-for="tag in ['✔ 安全支付','✔ 极速发货','✔ 退换保障','✔ 正品保证']" :key="tag"
            class="shrink-0 text-[10px] font-semibold px-3 py-1.5 rounded-xl"
            :style="{ background: shop.theme.card, border:`1px solid ${shop.theme.border}`, color: shop.theme.subText }">
        {{ tag }}
      </span>
    </div>

    <!-- ══ FIXED FOOTER ══ -->
    <div class="fixed bottom-0 left-0 right-0 z-20 px-4 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]"
         :style="{ background: shop.theme.bg, borderTop:`2px solid ${shop.theme.primary}` }">
      <div class="flex gap-3">
        <button class="flex-1 h-13 rounded-xl font-black text-sm tracking-widest uppercase active:scale-95 transition-all duration-200 flex items-center justify-center gap-2"
                style="height:52px"
                :style="{ background:`linear-gradient(135deg,${shop.theme.primary},${shop.theme.primary}CC)`,
                          color: shop.theme.bg, boxShadow:`0 8px 24px ${shop.theme.primary}45` }"
                @click="emit('buy')">
          <svg class="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
          </svg>
          SHOP NOW
        </button>
        <button class="w-13 h-13 rounded-xl flex items-center justify-center active:opacity-70 border"
                style="width:52px;height:52px"
                :style="{ borderColor: shop.theme.border, color: shop.theme.subText }"
                @click="share(null)">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- ══ PRODUCT DETAIL SHEET ══ -->
    <Transition enter-active-class="transition-opacity duration-250"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-opacity duration-200"
                leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="selectedProduct" class="fixed inset-0 z-50">
        <div class="absolute inset-0 bg-black/85" style="backdrop-filter:blur(6px)" @click="closeProduct"/>
        <div class="absolute bottom-0 left-0 right-0 rounded-t-3xl overflow-hidden"
             :style="{ background: shop.theme.bg, borderTop:`2px solid ${shop.theme.primary}`, maxHeight:'88vh' }"
             style="animation:slideUpSheet 0.32s cubic-bezier(0.34,1.4,0.64,1) both">
          <div class="flex justify-center pt-3 pb-1">
            <div class="w-10 h-1 rounded-full opacity-40" :style="{ background: shop.theme.subText }"/>
          </div>
          <div class="overflow-y-auto pb-8" style="max-height:calc(88vh - 1.5rem)">
            <!-- Image -->
            <div class="relative mx-4 rounded-xl overflow-hidden mb-4" style="aspect-ratio:1/1;max-height:45vw">
              <img v-if="selectedProduct.img" :src="selectedProduct.img" :alt="selectedProduct.title" class="w-full h-full object-cover"/>
              <div v-else class="w-full h-full" :style="{ background: selectedProduct.gradient||shop.theme.card }"/>
              <span v-if="selectedProduct.badge"
                    class="absolute top-3 left-3 text-[10px] font-black px-2 py-0.5 rounded-sm"
                    :style="{ background: shop.theme.primary, color: shop.theme.bg }">{{ selectedProduct.badge }}</span>
              <button class="absolute bottom-3 right-3 w-9 h-9 rounded-full flex items-center justify-center"
                      style="background:rgba(0,0,0,0.6);backdrop-filter:blur(8px)"
                      @click.stop="share(selectedProduct)">
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" :style="{ color: 'rgba(255,255,255,0.8)' }">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
                </svg>
              </button>
            </div>
            <!-- Info -->
            <div class="px-5 space-y-3">
              <div class="flex items-start justify-between gap-3">
                <h2 class="text-xl font-black leading-tight flex-1">{{ selectedProduct.title||selectedProduct.name }}</h2>
                <div class="text-right shrink-0">
                  <p class="text-2xl font-black" :style="{ color: shop.theme.primary }">€{{ selectedProduct.price }}</p>
                  <p v-if="selectedProduct.originalPrice" class="text-xs line-through" :style="{ color: shop.theme.subText }">€{{ selectedProduct.originalPrice }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <div class="flex">
                  <svg v-for="s in 5" :key="s" class="w-3.5 h-3.5" viewBox="0 0 20 20" fill="currentColor"
                       :style="{ color: s<=Math.round(selectedProduct.rating||5)?shop.theme.primary:shop.theme.border }">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <span class="text-sm font-semibold">{{ selectedProduct.rating||'4.9' }}</span>
                <span class="text-xs" :style="{ color: shop.theme.subText }">({{ selectedProduct.reviews||0 }} 条评价)</span>
              </div>
              <p class="text-sm leading-relaxed" :style="{ color: shop.theme.subText }">
                {{ selectedProduct.desc||'限量街头单品，独特设计，彰显个性。' }}
              </p>
              <div class="flex gap-2 flex-wrap">
                <span v-for="tag in ['✔ 安全支付','✔ 7天保障','✔ 正品保证']" :key="tag"
                      class="text-[10px] px-2.5 py-1 rounded-xl font-medium"
                      :style="{ background: shop.theme.card, border:`1px solid ${shop.theme.border}`, color: shop.theme.subText }">
                  {{ tag }}
                </span>
              </div>
              <div class="flex gap-3 pt-1">
                <button class="flex-1 h-13 rounded-xl font-black text-sm tracking-wider uppercase active:scale-95 transition-all duration-200 flex items-center justify-center gap-2"
                        style="height:52px"
                        :style="{ background:`linear-gradient(135deg,${shop.theme.primary},${shop.theme.primary}CC)`,
                                  color: shop.theme.bg, boxShadow:`0 8px 24px ${shop.theme.primary}45` }"
                        @click="buyProduct()">
                  <svg class="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
                  </svg>
                  BUY NOW
                </button>
                <button class="rounded-xl flex items-center justify-center active:opacity-70 border shrink-0"
                        style="width:52px;height:52px"
                        :style="{ borderColor: shop.theme.border, color: shop.theme.subText }"
                        @click.stop="share(selectedProduct)">
                  <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round"
                          d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>
