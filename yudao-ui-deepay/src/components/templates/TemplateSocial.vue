<!--
  TemplateSocial.vue — 社交电商 · 分享赚钱
  设计：每张商品卡片显示佣金率 · 一键生成带 ref 的分享链接
        快速下单弹层 · 佣金统计面板 · 主题色橙金
  使用 social.js API：createShareLink / recordClick / quickOrder
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { shareOrCopy } from '@/utils/user'
import { createShareLink, getMyCommission, recordClick, quickOrder } from '@/api/social'

const props = defineProps({ shop: { type: Object, required: true } })
const emit  = defineEmits(['buy', 'share'])

// 弹层状态
const quickBuyProduct = ref(null)
const copiedId        = ref(null)
const generatingId    = ref(null)   // 正在生成链接的商品

// 统计面板
const statsOpen    = ref(false)
const commission   = ref({ pending: 0, available: 0, total: 0 })
const loadingStats = ref(false)

// 读取佣金统计
async function loadCommission() {
  loadingStats.value = true
  try { commission.value = await getMyCommission() } catch (_) {}
  finally { loadingStats.value = false }
}

// 佣金汇总（含本地估算）
const mockStats = computed(() => {
  const products = props.shop.products || []
  const totalShares   = products.reduce((s, p) => s + (p.shareStats?.total || 0), 0)
  const totalWishlist = products.reduce((s, p) => s + (p.wishlistCount || 0), 0)
  return { totalShares, totalWishlist }
})

// 生成并复制分享链接
async function copyLink(product) {
  generatingId.value = product.id || product.name
  try {
    const shopId = props.shop.shopId || props.shop.id || 'shop'
    const link   = await createShareLink(shopId, product.id || product.name)
    await navigator.clipboard.writeText(link.fullUrl)
    copiedId.value = product.id || product.name
    setTimeout(() => { copiedId.value = null }, 2500)
  } catch {
    alert('复制失败，请手动复制链接')
  } finally {
    generatingId.value = null
  }
}

// 一键分享（系统分享 or 复制）
async function shareNow(product) {
  const shopId = props.shop.shopId || props.shop.id || 'shop'
  const rate   = product.commissionRate || props.shop.commissionRate || 10
  const earn   = (product.price * rate / 100).toFixed(2)
  const title  = `${product.title || product.name} — 每单赚 ¥${earn} 💰`
  try {
    const link = await createShareLink(shopId, product?.id || product?.name)
    shareOrCopy(link.fullUrl, title)
  } catch {
    shareOrCopy(window.location.href, title)
  }
  emit('share')
}

// 打开快速下单
function openQuickBuy(product) {
  quickBuyProduct.value = product
}

// 确认下单
async function confirmBuy() {
  const product = quickBuyProduct.value
  quickBuyProduct.value = null
  if (!product) return
  emit('buy')
  // 同时尝试走 quickOrder API
  try {
    const shopId = props.shop.shopId || props.shop.id || 'shop'
    const order  = await quickOrder({ productId: product.id || product.name, shopId, quantity: 1 })
    if (order?.payUrl) window.location.href = order.payUrl
  } catch (_) { /* 由父组件 buy 事件处理 Jeepay 支付 */ }
}

function closeQuickBuy() { quickBuyProduct.value = null }

// 格式化佣金
function earnLabel(product) {
  const rate = product.commissionRate || props.shop.commissionRate || 10
  return `¥${(product.price * rate / 100).toFixed(0)}`
}

// 统计面板切换
function toggleStats() {
  statsOpen.value = !statsOpen.value
  if (statsOpen.value && !commission.value.total) loadCommission()
}

// 记录落地页点击（从分享链接进入时）
onMounted(() => {
  const pid = new URLSearchParams(window.location.search).get('lnk')
  if (pid) recordClick(pid).catch(() => {})
})
</script>

<template>
  <div class="min-h-screen pb-36"
       :style="{ background: shop.theme.bg, color: shop.theme.text }">

    <!-- ── 顶栏 ────────────────────────────────────────────────── -->
    <header class="sticky top-0 z-20 px-4 py-3 flex items-center justify-between backdrop-blur-md"
            :style="{ background: shop.theme.bg + 'EE', borderBottom: `1px solid ${shop.theme.border}` }">
      <div>
        <p class="text-xs font-semibold tracking-widest uppercase"
           :style="{ color: shop.theme.primary }">SHARE & EARN</p>
        <p class="text-[10px]" :style="{ color: shop.theme.subText }">分享每单赚佣金</p>
      </div>
      <!-- 统计按钮 -->
      <button class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold transition-all active:scale-95"
              :style="{ background: shop.theme.primary + '22', color: shop.theme.primary, border: `1px solid ${shop.theme.primary}55` }"
              @click="toggleStats">
        <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z"/>
        </svg>
        我的佣金
      </button>
    </header>

    <!-- ── 统计面板（展开/收起）────────────────────────────────── -->
    <transition name="slide-down">
      <div v-if="statsOpen"
           class="mx-4 mt-3 rounded-2xl p-4"
           :style="{ background: shop.theme.card, border: `1px solid ${shop.theme.border}` }">
        <!-- 实际佣金数据行 -->
        <div class="grid grid-cols-3 gap-3 mb-4">
          <div class="text-center">
            <p class="text-xl font-black" :style="{ color: shop.theme.primary }">
              {{ loadingStats ? '…' : mockStats.totalShares.toLocaleString() }}
            </p>
            <p class="text-[10px] mt-0.5" :style="{ color: shop.theme.subText }">累计分享</p>
          </div>
          <div class="text-center border-x" :style="{ borderColor: shop.theme.border }">
            <p class="text-xl font-black" :style="{ color: shop.theme.primary }">
              ¥{{ loadingStats ? '…' : commission.available.toFixed(0) }}
            </p>
            <p class="text-[10px] mt-0.5" :style="{ color: shop.theme.subText }">可提现</p>
          </div>
          <div class="text-center">
            <p class="text-xl font-black" :style="{ color: shop.theme.primary }">
              ¥{{ loadingStats ? '…' : commission.total.toFixed(0) }}
            </p>
            <p class="text-[10px] mt-0.5" :style="{ color: shop.theme.subText }">累计收益</p>
          </div>
        </div>
        <!-- 待结算 -->
        <div v-if="commission.pending > 0"
             class="flex items-center justify-between text-xs p-2 rounded-xl mb-3"
             :style="{ background: shop.theme.primary + '18', color: shop.theme.primary }">
          <span>⏳ 待结算</span>
          <span class="font-bold">¥{{ commission.pending.toFixed(0) }}</span>
        </div>
        <!-- 佣金说明 -->
        <div class="text-xs leading-relaxed"
             :style="{ color: shop.theme.subText, borderTop: `1px solid ${shop.theme.border}`, paddingTop: '0.75rem' }">
          💡 通过你的专属链接每成交一单，平台自动到账佣金，30天内可提现。
        </div>
      </div>
    </transition>

    <!-- ── Hero 横幅 ─────────────────────────────────────────── -->
    <div class="mx-4 mt-4 rounded-2xl overflow-hidden relative"
         :style="{ background: shop.gradient || shop.theme.card }">
      <div class="px-5 py-6">
        <p class="text-xs uppercase tracking-widest font-semibold mb-1"
           :style="{ color: shop.theme.primary }">{{ shop.name || '分享赚钱专场' }}</p>
        <h2 class="text-2xl font-black leading-tight">分享每单<br>
          <span :style="{ color: shop.theme.primary }">最高赚 {{ shop.commissionRate || 15 }}%</span>
        </h2>
        <p class="text-xs mt-2" :style="{ color: shop.theme.subText }">点击商品下方「生成分享链接」即可开始赚钱</p>
      </div>
      <!-- 装饰圆 -->
      <div class="absolute -right-8 -top-8 w-32 h-32 rounded-full opacity-20"
           :style="{ background: shop.theme.primary }"></div>
      <div class="absolute -right-4 -bottom-4 w-20 h-20 rounded-full opacity-10"
           :style="{ background: shop.theme.primary }"></div>
    </div>

    <!-- ── 商品列表 ────────────────────────────────────────────── -->
    <div class="px-4 mt-5 space-y-4">
      <div
        v-for="(product, i) in shop.products"
        :key="i"
        class="rounded-2xl overflow-hidden"
        :style="{ background: shop.theme.card, border: `1px solid ${shop.theme.border}` }">

        <!-- 商品图 -->
        <div class="relative w-full" style="aspect-ratio:4/3">
          <img v-if="product.img" :src="product.img" :alt="product.title || product.name"
               class="w-full h-full object-cover" />
          <div v-else class="w-full h-full" :style="{ background: product.gradient || shop.gradient }"></div>

          <!-- 徽章 -->
          <div class="absolute top-3 left-3 flex gap-1.5">
            <span v-if="product.badge"
                  class="px-2 py-0.5 rounded-full text-[10px] font-bold"
                  :style="{ background: shop.theme.primary, color: shop.theme.bg }">
              {{ product.badge }}
            </span>
          </div>

          <!-- 佣金标签（右上角，最显眼）-->
          <div class="absolute top-3 right-3 flex flex-col items-end gap-1">
            <div class="flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-black shadow-lg"
                 style="background:linear-gradient(135deg,#FF6B35,#F7C948);color:#000">
              💰 {{ earnLabel(product) }}/单
            </div>
            <div class="text-[10px] px-2 py-0.5 rounded-full font-medium"
                 :style="{ background: shop.theme.bg + 'CC', color: shop.theme.subText }">
              佣金 {{ product.commissionRate || shop.commissionRate || 10 }}%
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="p-4">
          <div class="flex items-start justify-between mb-1">
            <div class="flex-1 min-w-0">
              <p class="font-semibold text-sm leading-tight truncate">{{ product.title || product.name }}</p>
              <p v-if="product.desc" class="text-xs mt-0.5 line-clamp-1" :style="{ color: shop.theme.subText }">{{ product.desc }}</p>
            </div>
            <div class="text-right ml-3 shrink-0">
              <p class="font-black text-lg leading-none" :style="{ color: shop.theme.primary }">
                ¥{{ product.price }}
              </p>
              <p v-if="product.originalPrice" class="text-xs line-through opacity-50 mt-0.5">¥{{ product.originalPrice }}</p>
            </div>
          </div>

          <!-- 评分 & 分享数 -->
          <div class="flex items-center gap-3 mt-2 mb-3">
            <div v-if="product.rating" class="flex items-center gap-1">
              <span class="text-yellow-400 text-xs">★</span>
              <span class="text-xs font-medium">{{ product.rating }}</span>
              <span class="text-[10px]" :style="{ color: shop.theme.subText }">({{ product.reviews || 0 }})</span>
            </div>
            <div v-if="product.shareStats" class="flex items-center gap-1 text-[10px]" :style="{ color: shop.theme.subText }">
              <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
              </svg>
              {{ product.shareStats.total || 0 }} 次分享
            </div>
          </div>

          <!-- 操作行 -->
          <div class="flex gap-2">
            <!-- 生成分享链接 -->
            <button
              class="flex-1 h-10 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 active:scale-95 transition-transform"
              :style="copiedId === (product.id || product.name)
                ? { background: '#22c55e', color: '#fff' }
                : { background: shop.theme.primary, color: shop.theme.bg }"
              @click="copyLink(product)">
              <svg v-if="copiedId !== (product.id || product.name)" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
              </svg>
              <svg v-else class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
              </svg>
              {{ copiedId === (product.id || product.name) ? '已复制链接 ✓' : '生成分享链接' }}
            </button>

            <!-- 分享按钮 -->
            <button
              class="w-10 h-10 rounded-xl flex items-center justify-center active:scale-95 transition-transform"
              :style="{ background: shop.theme.primary + '22', color: shop.theme.primary, border: `1px solid ${shop.theme.primary}44` }"
              @click="shareNow(product)">
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
              </svg>
            </button>

            <!-- 快速购买 -->
            <button
              class="w-10 h-10 rounded-xl flex items-center justify-center active:scale-95 transition-transform"
              :style="{ background: shop.theme.card, color: shop.theme.text, border: `1px solid ${shop.theme.border}` }"
              @click="openQuickBuy(product)">
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ── 快速下单弹层 ─────────────────────────────────────────── -->
    <transition name="fade-overlay">
      <div v-if="quickBuyProduct"
           class="fixed inset-0 z-50 flex items-end"
           style="background:rgba(0,0,0,.7)"
           @click.self="closeQuickBuy">
        <div class="w-full rounded-t-3xl p-6 pb-[calc(1.5rem+env(safe-area-inset-bottom))]"
             :style="{ background: shop.theme.card, border: `1px solid ${shop.theme.border}` }">

          <!-- 拖动条 -->
          <div class="w-10 h-1 rounded-full mx-auto mb-5 opacity-30"
               :style="{ background: shop.theme.text }"></div>

          <!-- 商品简介 -->
          <div class="flex gap-4 mb-5">
            <div class="w-20 h-20 rounded-xl overflow-hidden shrink-0">
              <img v-if="quickBuyProduct.img" :src="quickBuyProduct.img"
                   class="w-full h-full object-cover" :alt="quickBuyProduct.title || quickBuyProduct.name" />
              <div v-else class="w-full h-full" :style="{ background: quickBuyProduct.gradient }"></div>
            </div>
            <div class="flex-1 min-w-0">
              <p class="font-semibold leading-tight">{{ quickBuyProduct.title || quickBuyProduct.name }}</p>
              <p v-if="quickBuyProduct.desc" class="text-xs mt-1 line-clamp-2" :style="{ color: shop.theme.subText }">{{ quickBuyProduct.desc }}</p>
              <p class="font-black text-xl mt-1.5" :style="{ color: shop.theme.primary }">¥{{ quickBuyProduct.price }}</p>
            </div>
          </div>

          <!-- 佣金提示 -->
          <div class="flex items-center gap-2 p-3 rounded-xl mb-5 text-sm font-semibold"
               style="background:linear-gradient(135deg,rgba(255,107,53,.15),rgba(247,201,72,.15));border:1px solid rgba(247,201,72,.3)">
            💰 购买后分享给朋友，每单赚 <span class="text-yellow-400 font-black">{{ earnLabel(quickBuyProduct) }}</span> 佣金
          </div>

          <button
            class="w-full h-14 rounded-full font-black text-base flex items-center justify-center gap-2 active:scale-95 transition-transform"
            :style="{ background: shop.theme.primary, color: shop.theme.bg }"
            @click="confirmBuy">
            立即购买 ¥{{ quickBuyProduct.price }}
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
              <path stroke-linecap="round" stroke-linejoin="round" d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </button>
          <button class="w-full h-10 mt-2 text-sm active:opacity-60"
                  :style="{ color: shop.theme.subText }"
                  @click="closeQuickBuy">取消</button>
        </div>
      </div>
    </transition>

    <!-- ── 底部固定 CTA ─────────────────────────────────────────── -->
    <div class="fixed bottom-0 left-0 right-0 z-20 backdrop-blur-md px-4 pt-3
                pb-[calc(.75rem+env(safe-area-inset-bottom))]"
         :style="{ background: shop.theme.bg + 'EE', borderTop: `1px solid ${shop.theme.border}` }">
      <div class="flex gap-2 mb-1">
        <!-- 一键分享全店 -->
        <button
          class="flex-1 h-12 rounded-full font-bold text-sm flex items-center justify-center gap-2 active:scale-95 transition-transform"
          :style="{ background: shop.theme.primary, color: shop.theme.bg }"
          @click="shareNow(shop.products[0])">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
          </svg>
          📤 一键分享赚钱
        </button>
        <!-- 统计 -->
        <button
          class="w-12 h-12 rounded-full flex items-center justify-center active:scale-95 transition-transform"
          :style="{ background: shop.theme.card, color: shop.theme.primary, border: `1px solid ${shop.theme.border}` }"
          @click="statsOpen = !statsOpen">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z"/>
          </svg>
        </button>
      </div>
    </div>

  </div>
</template>

<style scoped>
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all .25s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.fade-overlay-enter-active,
.fade-overlay-leave-active {
  transition: opacity .25s ease;
}
.fade-overlay-enter-from,
.fade-overlay-leave-to {
  opacity: 0;
}
.fade-overlay-enter-active > *,
.fade-overlay-leave-active > * {
  transition: transform .3s cubic-bezier(.4,0,.2,1);
}
.fade-overlay-enter-from > *,
.fade-overlay-leave-to > * {
  transform: translateY(100%);
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
