<!--
  TemplateLuxury.vue — 高奢风 · 完整高端电商平台体验
  设计：全屏Hero · 黄金渐变文字 · 品类筛选 · Glass产品卡片 · 商品详情弹层 · 品牌价值区
  参考：Argon Dashboard + Creative Tim 设计语言 + 用户高端奢侈品电商规范
-->
<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])

const selectedProduct = ref(null)
const activeCategory  = ref(0)
const wishlist        = ref(new Set())
const cartCount       = ref(0)

const categories = ['全部', '外套', '礼服', '配饰', '限定']

const filteredProducts = computed(() =>
  activeCategory.value === 0
    ? (props.shop.products || [])
    : (props.shop.products || []).filter(p => p.category === categories[activeCategory.value])
)

function openProduct(p)  { selectedProduct.value = p }
function closeProduct()  { selectedProduct.value = null }

function toggleWishlist(name) {
  const s = new Set(wishlist.value)
  s.has(name) ? s.delete(name) : s.add(name)
  wishlist.value = s
}

function badgeGradient(badge) {
  return { NEW: 'linear-gradient(135deg,#D4AF37,#F5D56D)', HOT: 'linear-gradient(135deg,#FF6B6B,#FF8E53)', SALE: 'linear-gradient(135deg,#8B5CF6,#6D28D9)' }[badge] || null
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

    <!-- ══ STICKY HEADER ══ -->
    <header class="sticky top-0 z-30 flex items-center justify-between px-5 py-3"
            style="backdrop-filter:blur(20px)"
            :style="{ background: shop.theme.bg + 'EE', borderBottom: `1px solid ${shop.theme.primary}25` }">
      <div>
        <p class="text-[9px] tracking-[0.4em] uppercase font-light" :style="{ color: shop.theme.subText }">Exclusive</p>
        <p class="font-semibold text-sm tracking-[0.25em] uppercase">DEEPAY</p>
      </div>
      <div class="flex items-center gap-4">
        <button class="active:opacity-60" @click="share(null)">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"
               :style="{ color: shop.theme.subText }">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
          </svg>
        </button>
        <button class="relative active:opacity-60" @click="emit('buy')">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"
               :style="{ color: shop.theme.subText }">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
          </svg>
          <span v-if="cartCount > 0"
                class="absolute -top-1.5 -right-1.5 w-4 h-4 rounded-full text-[9px] font-bold flex items-center justify-center"
                :style="{ background: shop.theme.primary, color: shop.theme.bg }">{{ cartCount }}</span>
        </button>
      </div>
    </header>

    <!-- ══ HERO ══ -->
    <section class="relative flex flex-col items-center justify-center text-center px-6 py-16 overflow-hidden"
             style="min-height:260px"
             :style="{ background: `linear-gradient(160deg,${shop.theme.bg} 0%,#1c1505 50%,${shop.theme.bg} 100%)` }">
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-72 h-32 rounded-full blur-3xl opacity-20 pointer-events-none"
           :style="{ background: shop.theme.primary }"/>
      <div class="absolute top-5 left-5 w-7 h-7 border-t border-l opacity-50" :style="{ borderColor: shop.theme.primary }"/>
      <div class="absolute top-5 right-5 w-7 h-7 border-t border-r opacity-50" :style="{ borderColor: shop.theme.primary }"/>
      <div class="absolute bottom-5 left-5 w-7 h-7 border-b border-l opacity-50" :style="{ borderColor: shop.theme.primary }"/>
      <div class="absolute bottom-5 right-5 w-7 h-7 border-b border-r opacity-50" :style="{ borderColor: shop.theme.primary }"/>
      <div class="relative z-10">
        <p class="text-[9px] tracking-[0.5em] uppercase font-light mb-3" :style="{ color: shop.theme.subText }">
          Premium · Exclusive · Artisanal
        </p>
        <h1 class="font-light mb-3" style="font-size:clamp(1.8rem,8vw,2.6rem);letter-spacing:0.1em">
          {{ shop.name || 'LUXURY' }}
        </h1>
        <div class="flex items-center gap-3 justify-center mb-3">
          <div class="h-px w-10" :style="{ background: shop.theme.primary }"/>
          <span class="text-[9px] tracking-[0.3em] font-semibold uppercase" :style="{ color: shop.theme.primary }">✦ 高端甄选 ✦</span>
          <div class="h-px w-10" :style="{ background: shop.theme.primary }"/>
        </div>
        <p class="text-[11px] font-light" :style="{ color: shop.theme.subText }">
          {{ shop.products?.length || 0 }} 款限量精品 · 全球顺丰
        </p>
      </div>
    </section>

    <!-- ══ STATS BAR ══ -->
    <div class="grid grid-cols-3 gap-2 px-4 py-4">
      <div v-for="stat in [{ value:'4.9★',label:'综合评分' },{ value:'3K+',label:'全球订单' },{ value:'100%',label:'正品保障' }]"
           :key="stat.label" class="text-center py-3 rounded-2xl"
           :style="{ background:'rgba(17,15,10,0.8)', backdropFilter:'blur(12px)',
                     border:`1px solid ${shop.theme.primary}30`, boxShadow:'0 8px 24px rgba(0,0,0,0.3)' }">
        <p class="font-black text-sm"
           style="background:linear-gradient(135deg,#D4AF37,#F5D56D);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text">
          {{ stat.value }}
        </p>
        <p class="text-[10px] mt-0.5" :style="{ color: shop.theme.subText }">{{ stat.label }}</p>
      </div>
    </div>

    <!-- ══ CATEGORY CHIPS ══ -->
    <div class="flex gap-2 px-4 pb-4 overflow-x-auto scrollbar-hide">
      <button v-for="(cat,i) in categories" :key="cat"
              class="shrink-0 px-4 py-2 rounded-xl text-xs font-medium transition-all duration-200 active:scale-95"
              :style="activeCategory === i
                ? { background:`linear-gradient(135deg,${shop.theme.primary},${shop.theme.primary}99)`, color:shop.theme.bg, boxShadow:`0 4px 16px ${shop.theme.primary}40` }
                : { background:'rgba(17,15,10,0.7)', border:`1px solid ${shop.theme.primary}30`, color:shop.theme.subText }"
              @click="activeCategory = i">
        {{ cat }}
      </button>
    </div>

    <!-- ══ PRODUCT GRID ══ -->
    <div class="grid grid-cols-2 gap-3 px-3 pb-6">
      <div v-for="(p,i) in filteredProducts" :key="i"
           class="rounded-2xl overflow-hidden cursor-pointer transition-all duration-200 active:scale-[.97]"
           style="backdrop-filter:blur(16px)"
           :style="{ background:'rgba(17,15,10,0.75)', border:`1px solid ${shop.theme.primary}20`,
                     boxShadow:'0 20px 27px rgba(0,0,0,0.4)' }"
           @click="openProduct(p)">
        <!-- Image -->
        <div class="relative overflow-hidden" style="aspect-ratio:3/4;border-radius:16px 16px 0 0">
          <img v-if="p.img" :src="p.img" :alt="p.title||p.name" class="w-full h-full object-cover"/>
          <div v-else class="w-full h-full" :style="{ background: p.gradient || shop.theme.card }"/>
          <!-- Gold corner accents -->
          <div class="absolute top-2 left-2 w-4 h-4 border-t border-l pointer-events-none" :style="{ borderColor: shop.theme.primary+'80' }"/>
          <div class="absolute top-2 right-2 w-4 h-4 border-t border-r pointer-events-none" :style="{ borderColor: shop.theme.primary+'80' }"/>
          <!-- Badge -->
          <span v-if="p.badge"
                class="absolute bottom-2 left-2 text-[9px] font-bold px-2 py-0.5 rounded-full text-white"
                :style="{ background: badgeGradient(p.badge)||shop.theme.primary, boxShadow:'0 4px 10px rgba(0,0,0,0.5)' }">
            {{ p.badge }}
          </span>
          <!-- Wishlist -->
          <button class="absolute top-8 right-2 w-7 h-7 rounded-full flex items-center justify-center active:scale-90"
                  style="background:rgba(0,0,0,0.55);backdrop-filter:blur(8px)"
                  @click.stop="toggleWishlist(p.name)">
            <svg class="h-3.5 w-3.5" :fill="wishlist.has(p.name)?'currentColor':'none'"
                 viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                 :style="{ color: wishlist.has(p.name)?'#FF6B6B':'rgba(255,255,255,0.8)' }">
              <path stroke-linecap="round" stroke-linejoin="round"
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
            </svg>
          </button>
        </div>
        <!-- Card body -->
        <div class="p-3 space-y-1.5">
          <p class="text-xs font-semibold line-clamp-1">{{ p.title||p.name }}</p>
          <div class="flex items-center gap-1">
            <div class="flex">
              <svg v-for="s in 5" :key="s" class="w-2.5 h-2.5" viewBox="0 0 20 20" fill="currentColor"
                   :style="{ color: s<=Math.round(p.rating||5)?shop.theme.primary:shop.theme.border }">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
            </div>
            <span class="text-[10px]" :style="{ color: shop.theme.subText }">({{ p.reviews||0 }})</span>
          </div>
          <div class="flex items-baseline gap-1.5">
            <span class="text-sm font-bold"
                  style="background:linear-gradient(135deg,#D4AF37,#F5D56D);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text">
              €{{ p.price }}
            </span>
            <span v-if="p.originalPrice" class="text-[10px] line-through" :style="{ color: shop.theme.subText }">€{{ p.originalPrice }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ══ BRAND VALUES ══ -->
    <div class="px-4 mb-8">
      <div class="h-px w-full mb-6" :style="{ background:`linear-gradient(90deg,transparent,${shop.theme.primary}40,transparent)` }"/>
      <p class="text-center text-[9px] tracking-[0.4em] uppercase mb-5" :style="{ color: shop.theme.subText }">Our Promise</p>
      <div class="grid grid-cols-3 gap-3">
        <div v-for="item in [
               { icon:'✦', title:'独家限量', desc:'全球限量发售' },
               { icon:'◈', title:'匠心工艺', desc:'手工精细制作' },
               { icon:'✈', title:'全球直邮', desc:'顺丰包邮到家' },
             ]" :key="item.title"
             class="text-center py-4 px-2 rounded-2xl"
             :style="{ background:'rgba(17,15,10,0.6)', border:`1px solid ${shop.theme.primary}20` }">
          <p class="text-lg mb-1.5" :style="{ color: shop.theme.primary }">{{ item.icon }}</p>
          <p class="text-[11px] font-semibold mb-0.5">{{ item.title }}</p>
          <p class="text-[10px] leading-tight" :style="{ color: shop.theme.subText }">{{ item.desc }}</p>
        </div>
      </div>
      <div class="h-px w-full mt-6" :style="{ background:`linear-gradient(90deg,transparent,${shop.theme.primary}40,transparent)` }"/>
    </div>

    <!-- ══ FIXED FOOTER ══ -->
    <div class="fixed bottom-0 left-0 right-0 z-20 px-5 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]"
         style="backdrop-filter:blur(24px)"
         :style="{ background: shop.theme.bg+'F2', borderTop:`1px solid ${shop.theme.primary}30` }">
      <div class="flex gap-3">
        <button class="flex-1 h-12 rounded-xl font-semibold text-sm tracking-wider active:scale-95 transition-all duration-200 flex items-center justify-center gap-2 border"
                :style="{ borderColor:shop.theme.primary, color:shop.theme.primary,
                          background:`${shop.theme.primary}0A`, boxShadow:`0 0 20px ${shop.theme.primary}20` }"
                @click="emit('buy')">
          <svg class="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
          </svg>
          PURCHASE
        </button>
        <button class="w-12 h-12 rounded-xl flex items-center justify-center active:opacity-70 border"
                :style="{ borderColor:`${shop.theme.primary}30`, color:shop.theme.subText }"
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
        <div class="absolute inset-0 bg-black/80" style="backdrop-filter:blur(6px)" @click="closeProduct"/>
        <div class="absolute bottom-0 left-0 right-0 rounded-t-3xl overflow-hidden"
             :style="{ background:shop.theme.bg, border:`1px solid ${shop.theme.primary}30`, maxHeight:'88vh' }"
             style="animation:slideUpSheet 0.32s cubic-bezier(0.34,1.4,0.64,1) both">
          <div class="flex justify-center pt-3 pb-1">
            <div class="w-10 h-1 rounded-full opacity-40" :style="{ background: shop.theme.subText }"/>
          </div>
          <div class="overflow-y-auto pb-8" style="max-height:calc(88vh - 1.5rem)">
            <!-- Image -->
            <div class="relative mx-4 rounded-2xl overflow-hidden mb-4" style="aspect-ratio:3/4;max-height:42vh">
              <img v-if="selectedProduct.img" :src="selectedProduct.img" :alt="selectedProduct.title" class="w-full h-full object-cover"/>
              <div v-else class="w-full h-full" :style="{ background: selectedProduct.gradient||shop.theme.card }"/>
              <div class="absolute top-3 left-3 w-5 h-5 border-t border-l pointer-events-none" :style="{ borderColor:shop.theme.primary+'70' }"/>
              <div class="absolute top-3 right-3 w-5 h-5 border-t border-r pointer-events-none" :style="{ borderColor:shop.theme.primary+'70' }"/>
              <button class="absolute bottom-3 right-3 w-9 h-9 rounded-full flex items-center justify-center"
                      style="background:rgba(0,0,0,0.6);backdrop-filter:blur(8px)"
                      @click.stop="share(selectedProduct)">
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" :style="{ color:shop.theme.subText }">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
                </svg>
              </button>
            </div>
            <!-- Info -->
            <div class="px-5 space-y-3">
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="text-[10px] tracking-[0.2em] uppercase font-light mb-1" :style="{ color:shop.theme.subText }">Premium Collection</p>
                  <h2 class="text-xl font-semibold leading-tight">{{ selectedProduct.title||selectedProduct.name }}</h2>
                </div>
                <div class="text-right shrink-0">
                  <p class="text-2xl font-black"
                     style="background:linear-gradient(135deg,#D4AF37,#F5D56D);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text">
                    €{{ selectedProduct.price }}
                  </p>
                  <p v-if="selectedProduct.originalPrice" class="text-xs line-through" :style="{ color:shop.theme.subText }">€{{ selectedProduct.originalPrice }}</p>
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
                <span class="text-xs" :style="{ color:shop.theme.subText }">({{ selectedProduct.reviews||0 }} 条好评)</span>
              </div>
              <div class="h-px" :style="{ background:`${shop.theme.primary}25` }"/>
              <p class="text-sm leading-relaxed" :style="{ color:shop.theme.subText }">
                {{ selectedProduct.desc||'精选高端材质，匠心工艺，每一件皆为收藏之作。' }}
              </p>
              <div class="space-y-2.5">
                <div v-for="item in ['7天无理由退换','顺丰全球包邮','正品溯源保障']" :key="item"
                     class="flex items-center gap-3 text-xs" :style="{ color:shop.theme.subText }">
                  <div class="w-5 h-5 rounded-full flex items-center justify-center shrink-0"
                       :style="{ background:`${shop.theme.primary}25` }">
                    <svg class="w-3 h-3" viewBox="0 0 20 20" fill="currentColor" :style="{ color:shop.theme.primary }">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                  </div>
                  {{ item }}
                </div>
              </div>
              <div class="flex gap-3 pt-2">
                <button class="flex-1 h-13 rounded-xl font-semibold text-sm tracking-wide active:scale-95 transition-all duration-200 flex items-center justify-center gap-2 border"
                        style="height:52px"
                        :style="{ borderColor:shop.theme.primary, color:shop.theme.primary,
                                  background:`${shop.theme.primary}08`, boxShadow:`0 0 20px ${shop.theme.primary}20` }"
                        @click="buyProduct()">
                  <svg class="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
                  </svg>
                  PURCHASE NOW
                </button>
                <button class="rounded-xl flex items-center justify-center active:opacity-70 border shrink-0"
                        style="width:52px;height:52px"
                        :style="{ borderColor:`${shop.theme.primary}40`, color:shop.theme.subText }"
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
