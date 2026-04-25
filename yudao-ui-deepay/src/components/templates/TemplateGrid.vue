<!--
  TemplateGrid.vue — 电商爆款 · EXCLUSIVE BLACK & WHITE COLLECTION
  设计：3D 翻转卡片（商品正面+故事背面）· 限量/独家/可持续/可定制徽章
        分类筛选 · 商品详情弹层 · 统计栏 · 黑白奢侈展示
  参考：用户提供高级产品展示设计规范
-->
<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])

const selectedProduct = ref(null)
const flippedCards    = ref(new Set())
const wishlist        = ref(new Set())
const cartCount       = ref(0)
const activeFilter    = ref('ALL')
const isMasonry       = ref(false)

const filters = ['ALL', 'LIMITED', 'EXCLUSIVE', 'SALE', 'NEW']

const filteredProducts = computed(() => {
  const all = props.shop.products || []
  if (activeFilter.value === 'ALL')       return all
  if (activeFilter.value === 'LIMITED')   return all.filter(p => p.limited)
  if (activeFilter.value === 'EXCLUSIVE') return all.filter(p => p.exclusive)
  if (activeFilter.value === 'SALE')      return all.filter(p => p.badge === 'SALE')
  if (activeFilter.value === 'NEW')       return all.filter(p => p.badge === 'NEW')
  return all
})

function openProduct(p)  { selectedProduct.value = p }
function closeProduct()  { selectedProduct.value = null }

function toggleFlip(name) {
  const s = new Set(flippedCards.value)
  s.has(name) ? s.delete(name) : s.add(name)
  flippedCards.value = s
}

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
       style="font-family:'Poppins',sans-serif;background:#FAFAFA;color:#111">

    <!-- ══ HEADER ══ -->
    <header class="sticky top-0 z-30 px-5 py-3 flex items-center justify-between"
            style="background:rgba(250,250,250,0.94);backdrop-filter:blur(20px);
                   border-bottom:1px solid rgba(0,0,0,0.07);box-shadow:0 2px 16px rgba(0,0,0,0.04)">
      <div>
        <p style="font-size:9px;letter-spacing:0.4em;color:#999;text-transform:uppercase">Exclusive Collection</p>
        <p style="font-weight:900;font-size:13px;letter-spacing:0.2em;text-transform:uppercase">DEEPAY</p>
      </div>
      <div style="display:flex;align-items:center;gap:12px">
        <!-- Masonry toggle -->
        <button style="width:34px;height:34px;border-radius:8px;display:flex;align-items:center;justify-content:center;
                        background:#f4f4f5;border:none;cursor:pointer"
                @click="isMasonry = !isMasonry">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" style="color:#666">
            <path v-if="!isMasonry" d="M3 3h8v8H3zm10 0h8v8h-8zM3 13h8v8H3zm10 0h8v8h-8z"/>
            <path v-else d="M3 3h8v12H3zm10 0h8v6h-8zM3 17h8v4H3zm10-5h8v12h-8z"/>
          </svg>
        </button>
        <!-- Share -->
        <button style="background:none;border:none;cursor:pointer;padding:0" @click="share(null)">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#888" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
          </svg>
        </button>
        <!-- Cart -->
        <button style="position:relative;background:none;border:none;cursor:pointer;padding:0" @click="emit('buy')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#888" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
          </svg>
          <span v-if="cartCount > 0"
                style="position:absolute;top:-6px;right:-6px;width:16px;height:16px;border-radius:50%;
                       background:#111;color:#fff;font-size:9px;font-weight:700;display:flex;align-items:center;justify-content:center">
            {{ cartCount }}
          </span>
        </button>
      </div>
    </header>

    <!-- ══ SHOWCASE HEADER ══ -->
    <div style="padding:32px 20px 20px;text-align:center;background:linear-gradient(180deg,#111 0%,#1a1a1a 100%)">
      <p style="font-size:9px;letter-spacing:0.5em;color:#888;text-transform:uppercase;margin-bottom:12px">Designer Collection</p>
      <h1 style="font-weight:900;line-height:1.05;color:#fff;margin-bottom:8px">
        <span style="display:block;font-size:clamp(1.4rem,7vw,2rem);letter-spacing:0.08em">EXCLUSIVE</span>
        <span style="display:block;font-size:clamp(1.4rem,7vw,2rem);letter-spacing:0.08em;
                     background:linear-gradient(135deg,#D4AF37,#F5D56D);-webkit-background-clip:text;
                     -webkit-text-fill-color:transparent;background-clip:text">BLACK &amp; WHITE</span>
        <span style="display:block;font-size:clamp(1.4rem,7vw,2rem);letter-spacing:0.08em">COLLECTION</span>
      </h1>
      <p style="font-size:11px;color:#888;margin-top:10px">设计师独家系列 · 限量发售 · 每件单品独立编号</p>
      <!-- Stats row -->
      <div style="display:flex;gap:12px;justify-content:center;margin-top:20px">
        <div v-for="s in [{ v:'4.9★', l:'综合评分' }, { v:(shop.products||[]).length+'件', l:'精选单品' }, { v:'100%', l:'正品保障' }]"
             :key="s.l"
             style="background:rgba(255,255,255,0.08);border:1px solid rgba(255,255,255,0.1);
                    border-radius:12px;padding:10px 14px;text-align:center;min-width:72px">
          <p style="font-weight:900;font-size:13px;background:linear-gradient(135deg,#D4AF37,#F5D56D);
                    -webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text">{{ s.v }}</p>
          <p style="font-size:10px;color:#888;margin-top:2px">{{ s.l }}</p>
        </div>
      </div>
    </div>

    <!-- ══ FILTER CHIPS ══ -->
    <div style="display:flex;gap:8px;padding:16px;overflow-x:auto;scrollbar-width:none">
      <button v-for="f in filters" :key="f"
              style="flex-shrink:0;padding:8px 16px;border-radius:20px;font-size:11px;font-weight:600;
                     letter-spacing:0.05em;cursor:pointer;border:1px solid transparent;transition:all 0.2s"
              :style="activeFilter === f
                ? 'background:#111;color:#fff;box-shadow:0 4px 16px rgba(0,0,0,0.25)'
                : 'background:#f4f4f5;color:#666;border-color:#e5e5e5'"
              @click="activeFilter = f">
        {{ f }}
      </button>
    </div>

    <!-- ══ PRODUCT GRID ══ -->
    <div :style="isMasonry
           ? 'columns:2;column-gap:12px;padding:0 12px 24px'
           : 'display:grid;grid-template-columns:1fr 1fr;gap:12px;padding:0 12px 24px'">

      <div v-for="(p, i) in filteredProducts" :key="p.name"
           :style="isMasonry ? 'break-inside:avoid;margin-bottom:12px' : ''"
           style="perspective:1000px;cursor:pointer">

        <!-- 3D Flip Card Container -->
        <div style="position:relative;transition:transform 0.7s cubic-bezier(0.34,1.2,0.64,1);transform-style:preserve-3d;border-radius:16px"
             :style="{ transform: flippedCards.has(p.name) ? 'rotateY(180deg)' : 'rotateY(0deg)',
                       aspectRatio: isMasonry && i%3===0 ? '3/5' : '3/4' }"
             @click="flippedCards.has(p.name) ? openProduct(p) : null">

          <!-- ── FRONT ── -->
          <div style="position:absolute;inset:0;backface-visibility:hidden;border-radius:16px;overflow:hidden;
                      box-shadow:0 8px 30px rgba(0,0,0,0.12),0 2px 8px rgba(0,0,0,0.06)">
            <!-- Image / gradient -->
            <img v-if="p.img" :src="p.img" :alt="p.title||p.name"
                 style="width:100%;height:100%;object-fit:cover"/>
            <div v-else style="width:100%;height:100%"
                 :style="{ background: p.gradient||'#e5e5e5' }"/>

            <!-- Gradient overlay -->
            <div style="position:absolute;inset:0;pointer-events:none"
                 :style="{ background: p.overlayGradient||'linear-gradient(to top,rgba(0,0,0,0.5) 0%,transparent 60%)' }"/>

            <!-- Badges top-left -->
            <div style="position:absolute;top:10px;left:10px;display:flex;flex-direction:column;gap:4px">
              <span v-if="p.limited"
                    style="font-size:9px;font-weight:700;padding:3px 7px;border-radius:4px;
                           background:#111;color:#D4AF37;letter-spacing:0.05em">
                限量 {{ p.limitedNumber }}/{{ p.totalLimited }}
              </span>
              <span v-if="p.exclusive"
                    style="font-size:9px;font-weight:700;padding:3px 7px;border-radius:4px;
                           background:rgba(0,0,0,0.75);color:#fff">独家</span>
              <span v-if="p.sustainable"
                    style="font-size:9px;font-weight:700;padding:3px 7px;border-radius:4px;
                           background:#064E3B;color:#6EE7B7">可持续</span>
              <span v-if="p.customizable"
                    style="font-size:9px;font-weight:700;padding:3px 7px;border-radius:4px;
                           background:#1E3A8A;color:#93C5FD">可定制</span>
            </div>

            <!-- Sale badge top-right -->
            <span v-if="p.badge === 'SALE'"
                  style="position:absolute;top:10px;right:10px;font-size:9px;font-weight:900;padding:3px 7px;
                         border-radius:4px;background:linear-gradient(135deg,#DC2626,#EF4444);color:#fff">
              SALE
            </span>
            <span v-else-if="p.badge === 'NEW'"
                  style="position:absolute;top:10px;right:10px;font-size:9px;font-weight:900;padding:3px 7px;
                         border-radius:4px;background:linear-gradient(135deg,#2563EB,#7C3AED);color:#fff">
              NEW
            </span>
            <span v-else-if="p.badge === 'HOT'"
                  style="position:absolute;top:10px;right:10px;font-size:9px;font-weight:900;padding:3px 7px;
                         border-radius:4px;background:linear-gradient(135deg,#FF6B6B,#FF8E53);color:#fff">
              HOT
            </span>

            <!-- Bottom info -->
            <div style="position:absolute;bottom:0;left:0;right:0;padding:10px 12px 10px">
              <p style="font-weight:700;font-size:12px;color:#fff;line-height:1.2;
                        white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
                {{ p.title||p.name }}
              </p>
              <div style="display:flex;align-items:center;justify-content:space-between;margin-top:4px">
                <span style="font-weight:900;font-size:14px;color:#fff">€{{ p.price }}</span>
                <div style="display:flex;gap:4px">
                  <!-- Wishlist -->
                  <button style="width:28px;height:28px;border-radius:50%;display:flex;align-items:center;
                                 justify-content:center;background:rgba(0,0,0,0.5);backdrop-filter:blur(8px);
                                 border:none;cursor:pointer"
                          @click.stop="toggleWishlist(p.name)">
                    <svg width="13" height="13" viewBox="0 0 24 24"
                         :fill="wishlist.has(p.name)?'#FF6B6B':'none'"
                         stroke="currentColor" stroke-width="2"
                         :style="{ color: wishlist.has(p.name)?'#FF6B6B':'rgba(255,255,255,0.9)' }">
                      <path stroke-linecap="round" stroke-linejoin="round"
                            d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                    </svg>
                  </button>
                  <!-- Flip to back -->
                  <button style="width:28px;height:28px;border-radius:50%;display:flex;align-items:center;
                                 justify-content:center;background:rgba(255,255,255,0.15);backdrop-filter:blur(8px);
                                 border:1px solid rgba(255,255,255,0.3);cursor:pointer"
                          @click.stop="toggleFlip(p.name)"
                          title="翻转查看故事">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="rgba(255,255,255,0.9)">
                      <path d="M12 5.83L15.17 9l1.41-1.41L12 3 7.41 7.59 8.83 9 12 5.83zm0 12.34L8.83 15l-1.41 1.41L12 21l4.59-4.59L15.17 15 12 18.17z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- ── BACK ── -->
          <div style="position:absolute;inset:0;backface-visibility:hidden;border-radius:16px;overflow:hidden;
                      transform:rotateY(180deg);background:#111;
                      box-shadow:0 8px 30px rgba(0,0,0,0.25);display:flex;flex-direction:column"
               @click.stop="openProduct(p)">
            <!-- Gold header line -->
            <div style="height:3px;background:linear-gradient(90deg,#D4AF37,#F5D56D,#D4AF37)"/>
            <div style="padding:16px 14px;flex:1;display:flex;flex-direction:column;justify-content:space-between">
              <div>
                <p style="font-size:8px;letter-spacing:0.3em;color:#D4AF37;text-transform:uppercase;margin-bottom:6px">
                  Product Story
                </p>
                <h3 style="font-weight:700;font-size:13px;color:#fff;margin-bottom:8px;line-height:1.3">
                  {{ p.title||p.name }}
                </h3>
                <p style="font-size:11px;color:#aaa;line-height:1.6">
                  {{ p.story||p.desc||'暂无商品故事' }}
                </p>
              </div>
              <div>
                <div style="border-top:1px solid rgba(255,255,255,0.1);padding-top:10px;margin-top:10px">
                  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px">
                    <span style="font-size:9px;letter-spacing:0.2em;color:#888;text-transform:uppercase">材质</span>
                    <span style="font-size:11px;color:#D4AF37;font-weight:600">{{ p.material||'精选面料' }}</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
                    <span style="font-size:9px;letter-spacing:0.2em;color:#888;text-transform:uppercase">价格</span>
                    <div style="text-align:right">
                      <span style="font-weight:900;font-size:16px;background:linear-gradient(135deg,#D4AF37,#F5D56D);
                                   -webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text">
                        €{{ p.price }}
                      </span>
                      <span v-if="p.originalPrice"
                            style="font-size:10px;color:#666;text-decoration:line-through;margin-left:4px">
                        €{{ p.originalPrice }}
                      </span>
                    </div>
                  </div>
                  <!-- CTA row -->
                  <div style="display:flex;gap:8px">
                    <button style="flex:1;height:38px;border-radius:10px;font-weight:700;font-size:11px;
                                   letter-spacing:0.05em;border:1px solid #D4AF37;color:#D4AF37;
                                   background:rgba(212,175,55,0.08);cursor:pointer;
                                   transition:all 0.2s;text-transform:uppercase"
                            @click.stop="openProduct(p)">
                      查看详情
                    </button>
                    <button style="width:38px;height:38px;border-radius:10px;display:flex;align-items:center;
                                   justify-content:center;border:1px solid rgba(255,255,255,0.15);
                                   background:none;cursor:pointer"
                            @click.stop="toggleFlip(p.name)">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="rgba(255,255,255,0.6)">
                        <path d="M12 5.83L15.17 9l1.41-1.41L12 3 7.41 7.59 8.83 9 12 5.83zm0 12.34L8.83 15l-1.41 1.41L12 21l4.59-4.59L15.17 15 12 18.17z"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>
        <!-- /3D card -->
      </div>
      <!-- /v-for -->
    </div>
    <!-- /grid -->

    <!-- ══ FIXED FOOTER ══ -->
    <div style="position:fixed;bottom:0;left:0;right:0;z-index:20;padding:12px 16px;
                padding-bottom:calc(12px + env(safe-area-inset-bottom));
                background:rgba(250,250,250,0.95);backdrop-filter:blur(20px);
                border-top:1px solid rgba(0,0,0,0.07);box-shadow:0 -4px 20px rgba(0,0,0,0.08)">
      <div style="display:flex;gap:10px">
        <button style="flex:1;height:50px;border-radius:14px;font-weight:900;font-size:13px;
                       letter-spacing:0.08em;text-transform:uppercase;border:none;cursor:pointer;
                       display:flex;align-items:center;justify-content:center;gap:8px;
                       background:linear-gradient(135deg,#111,#333);color:#fff;
                       box-shadow:0 8px 24px rgba(0,0,0,0.25)"
                @click="emit('buy')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
          </svg>
          SHOP THE COLLECTION
        </button>
        <button style="width:50px;height:50px;border-radius:14px;display:flex;align-items:center;
                       justify-content:center;border:1px solid #e5e5e5;background:#fff;cursor:pointer"
                @click="share(null)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#666" stroke-width="2">
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
      <div v-if="selectedProduct" style="position:fixed;inset:0;z-index:50">
        <div style="position:absolute;inset:0;background:rgba(0,0,0,0.75);backdrop-filter:blur(6px)"
             @click="closeProduct"/>
        <div style="position:absolute;bottom:0;left:0;right:0;border-radius:28px 28px 0 0;overflow:hidden;
                    background:#fff;max-height:88vh;animation:slideUpSheet 0.32s cubic-bezier(0.34,1.4,0.64,1) both">
          <div style="display:flex;justify-content:center;padding:12px 0 4px">
            <div style="width:40px;height:4px;border-radius:4px;background:#ddd"/>
          </div>
          <div style="overflow-y:auto;max-height:calc(88vh - 24px);padding-bottom:32px">
            <!-- Image -->
            <div style="position:relative;margin:0 16px 16px;border-radius:16px;overflow:hidden;aspect-ratio:3/4;max-height:40vh">
              <img v-if="selectedProduct.img" :src="selectedProduct.img"
                   :alt="selectedProduct.title" style="width:100%;height:100%;object-fit:cover"/>
              <div v-else style="width:100%;height:100%"
                   :style="{ background: selectedProduct.gradient||'#e5e5e5' }"/>
              <!-- Badges -->
              <div style="position:absolute;top:10px;left:10px;display:flex;flex-direction:column;gap:4px">
                <span v-if="selectedProduct.limited"
                      style="font-size:9px;font-weight:700;padding:3px 8px;border-radius:4px;
                             background:#111;color:#D4AF37">
                  限量 {{ selectedProduct.limitedNumber }}/{{ selectedProduct.totalLimited }}
                </span>
                <span v-if="selectedProduct.exclusive"
                      style="font-size:9px;font-weight:700;padding:3px 8px;border-radius:4px;
                             background:rgba(0,0,0,0.8);color:#fff">独家</span>
                <span v-if="selectedProduct.sustainable"
                      style="font-size:9px;font-weight:700;padding:3px 8px;border-radius:4px;
                             background:#064E3B;color:#6EE7B7">可持续</span>
                <span v-if="selectedProduct.customizable"
                      style="font-size:9px;font-weight:700;padding:3px 8px;border-radius:4px;
                             background:#1E3A8A;color:#93C5FD">可定制</span>
              </div>
              <!-- Share on image -->
              <button style="position:absolute;bottom:12px;right:12px;width:36px;height:36px;border-radius:50%;
                             display:flex;align-items:center;justify-content:center;
                             background:rgba(0,0,0,0.55);backdrop-filter:blur(8px);border:none;cursor:pointer"
                      @click.stop="share(selectedProduct)">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.85)" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
                </svg>
              </button>
            </div>
            <!-- Details -->
            <div style="padding:0 20px">
              <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:12px;margin-bottom:10px">
                <div>
                  <p style="font-size:10px;letter-spacing:0.2em;color:#aaa;text-transform:uppercase;margin-bottom:4px">
                    Premium Collection
                  </p>
                  <h2 style="font-weight:700;font-size:20px;line-height:1.2">
                    {{ selectedProduct.title||selectedProduct.name }}
                  </h2>
                </div>
                <div style="text-align:right;flex-shrink:0">
                  <p style="font-weight:900;font-size:22px;background:linear-gradient(135deg,#111,#444);
                             -webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text">
                    €{{ selectedProduct.price }}
                  </p>
                  <p v-if="selectedProduct.originalPrice"
                     style="font-size:11px;color:#ccc;text-decoration:line-through">
                    €{{ selectedProduct.originalPrice }}
                  </p>
                </div>
              </div>
              <!-- Stars -->
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:14px">
                <div style="display:flex">
                  <svg v-for="s in 5" :key="s" width="14" height="14" viewBox="0 0 20 20" fill="currentColor"
                       :style="{ color: s<=Math.round(selectedProduct.rating||5)?'#D4AF37':'#e5e5e5' }">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <span style="font-size:13px;font-weight:600">{{ selectedProduct.rating||'4.9' }}</span>
                <span style="font-size:11px;color:#aaa">({{ selectedProduct.reviews||0 }} 条好评)</span>
              </div>
              <!-- Divider -->
              <div style="height:1px;background:linear-gradient(90deg,transparent,#ddd,transparent);margin-bottom:14px"/>
              <!-- Material row -->
              <div v-if="selectedProduct.material"
                   style="display:flex;justify-content:space-between;align-items:center;
                          padding:10px 14px;border-radius:10px;background:#f9f9f9;margin-bottom:10px">
                <span style="font-size:11px;letter-spacing:0.1em;color:#aaa;text-transform:uppercase">材质</span>
                <span style="font-size:12px;font-weight:600;color:#333">{{ selectedProduct.material }}</span>
              </div>
              <!-- Story -->
              <div v-if="selectedProduct.story"
                   style="padding:14px;border-radius:12px;border:1px solid #f0f0f0;margin-bottom:14px">
                <p style="font-size:9px;letter-spacing:0.25em;color:#D4AF37;text-transform:uppercase;margin-bottom:6px">
                  Product Story
                </p>
                <p style="font-size:12px;line-height:1.7;color:#666">{{ selectedProduct.story }}</p>
              </div>
              <!-- Trust row -->
              <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:16px">
                <span v-for="tag in ['✔ 安全支付','✔ 7天保障','✔ 正品溯源','✔ 全球直邮']" :key="tag"
                      style="font-size:10px;padding:6px 10px;border-radius:8px;
                             background:#f4f4f5;color:#666;font-weight:500">
                  {{ tag }}
                </span>
              </div>
              <!-- CTAs -->
              <div style="display:flex;gap:10px">
                <button style="flex:1;height:52px;border-radius:14px;font-weight:900;font-size:13px;
                               letter-spacing:0.06em;text-transform:uppercase;border:none;cursor:pointer;
                               display:flex;align-items:center;justify-content:center;gap:8px;
                               background:linear-gradient(135deg,#111,#333);color:#fff;
                               box-shadow:0 8px 24px rgba(0,0,0,0.2)"
                        @click="buyProduct()">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round"
                          d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
                  </svg>
                  BUY NOW
                </button>
                <button style="width:52px;height:52px;border-radius:14px;display:flex;align-items:center;
                               justify-content:center;border:1px solid #e5e5e5;background:#fff;cursor:pointer"
                        @click.stop="share(selectedProduct)">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#666" stroke-width="2">
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
