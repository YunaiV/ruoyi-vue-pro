<!--
  Me.vue — 用户资产中心
  路径：/me

  功能：
  ✔ 总资产面板（totalEarn / balance / frozen / dividendEarn）
  ✔ 邀请数据（inviteCount / inviteEarn）
  ✔ 持有份额（shareAmount）
  ✔ 平台信任背书（累计分红）
  ✔ 提现表单 + 提现记录
  ✔ 我的店铺列表
  ✔ 快捷导航：排行榜 / 邀请 / 新建店铺
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { initUserId, buildShareLink, shareOrCopy } from '@/utils/user'
import { getDashboard, getPlatformStats } from '@/api/user'
import { applyWithdraw, getWithdrawList } from '@/api/withdraw'

const router = useRouter()
const userId = initUserId()

// ── 总资产面板（单接口）─────────────────────────────────────────────
const dash        = ref(null)   // null = loading
const dashError   = ref(false)

async function loadDashboard() {
  try {
    dash.value = await getDashboard()
  } catch (_) {
    dashError.value = true
    dash.value = {
      totalEarn: 0, balance: 0, frozen: 0,
      inviteCount: 0, inviteEarn: 0,
      shareAmount: 0, dividendEarn: 0,
    }
  }
}

// ── 平台累计数据（信任背书）─────────────────────────────────────────
const platformStats = ref(null)

async function loadPlatformStats() {
  try {
    platformStats.value = await getPlatformStats()
  } catch (_) {}
}

// ── 提现 ────────────────────────────────────────────────────────────
const withdrawAmount = ref('')
const withdrawAcct   = ref('')
const withdrawing    = ref(false)
const withdrawError  = ref('')
const withdrawOk     = ref(false)
const withdrawList   = ref([])

async function loadWithdrawList() {
  try { withdrawList.value = await getWithdrawList() ?? [] } catch (_) {}
}

async function submitWithdraw() {
  withdrawError.value = ''
  withdrawOk.value    = false
  const amt = Number(withdrawAmount.value)
  if (!amt || amt < 10) {
    withdrawError.value = '最低提现金额 €10'; return
  }
  if (!withdrawAcct.value.trim()) {
    withdrawError.value = '请填写收款账号'; return
  }
  const avail = dash.value?.balance ?? 0
  if (amt > avail) {
    withdrawError.value = `余额不足（可提现 €${Number(avail).toFixed(2)}）`; return
  }
  withdrawing.value = true
  try {
    await applyWithdraw(amt, withdrawAcct.value.trim())
    withdrawOk.value     = true
    withdrawAmount.value = ''
    withdrawAcct.value   = ''
    await loadDashboard()
    await loadWithdrawList()
  } catch (e) {
    withdrawError.value = e?.response?.data?.msg || '提交失败，请稍后重试'
  } finally {
    withdrawing.value = false
  }
}

// ── 店铺列表（localStorage）─────────────────────────────────────────
function loadShops() {
  const result = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key?.startsWith('shop_')) {
      try {
        const data = JSON.parse(localStorage.getItem(key))
        result.push({ shopId: key.replace('shop_', ''), ...data })
      } catch (_) {}
    }
  }
  return result.sort((a, b) => Number(b.shopId) - Number(a.shopId))
}
const shops = ref([])

function deleteShop(shopId) {
  localStorage.removeItem(`shop_${shopId}`)
  shops.value = loadShops()
}

function shareShop(shopId, shopName) {
  shareOrCopy(buildShareLink(shopId, userId), shopName || '我的店铺')
}

function formatDate(shopId) {
  try {
    return new Date(Number(shopId)).toLocaleDateString('zh-CN', {
      month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
    })
  } catch (_) { return '' }
}

// ── 格式化金额 ───────────────────────────────────────────────────────
const fmt = v => Number(v ?? 0).toFixed(2)

onMounted(() => {
  shops.value = loadShops()
  loadDashboard()
  loadWithdrawList()
  loadPlatformStats()
})
</script>

<template>
  <div class="min-h-screen bg-bg text-white">

    <!-- 顶部导航 -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md
                   border-b border-border px-4 py-3
                   flex items-center gap-3">
      <button class="text-muted active:text-white transition-colors"
              @click="router.back()">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <span class="font-semibold text-sm flex-1">我的资产</span>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-5 pb-28">

      <!-- ── 平台信任背书 ──────────────────────────────────────────── -->
      <div v-if="platformStats"
           class="mb-5 text-center text-xs text-muted">
        平台已累计向用户分红
        <span class="text-accent font-bold">
          €{{ fmt(platformStats.totalDividend) }}
        </span>
        · {{ platformStats.totalUsers ?? 0 }} 人参与
      </div>

      <!-- ── 总收益英雄卡 ──────────────────────────────────────────── -->
      <section class="mb-4">
        <div class="card p-5 rounded-2xl"
             style="background:linear-gradient(135deg,#0f2d1a,#111)">

          <div class="flex items-start justify-between mb-4">
            <div>
              <p class="text-xs text-muted mb-1">总收益</p>
              <!-- loading skeleton -->
              <div v-if="dash === null"
                   class="h-10 w-32 bg-surface2 rounded animate-pulse" />
              <p v-else class="text-4xl font-black" style="color:#00FF88">
                €{{ fmt(dash.totalEarn) }}
              </p>
            </div>
            <span class="text-xs px-2 py-1 rounded-full font-semibold mt-1"
                  style="background:#00FF8822;color:#00FF88">
              10% 佣金
            </span>
          </div>

          <!-- 三格资产行 -->
          <div class="grid grid-cols-3 gap-2">
            <div class="bg-bg/50 rounded-xl p-3 text-center">
              <p class="text-[10px] text-muted mb-1">可提现</p>
              <div v-if="dash === null"
                   class="h-5 w-12 bg-surface2 rounded animate-pulse mx-auto" />
              <p v-else class="text-sm font-bold text-white">
                €{{ fmt(dash.balance) }}
              </p>
            </div>
            <div class="bg-bg/50 rounded-xl p-3 text-center">
              <p class="text-[10px] text-muted mb-1">提现中</p>
              <div v-if="dash === null"
                   class="h-5 w-12 bg-surface2 rounded animate-pulse mx-auto" />
              <p v-else class="text-sm font-bold text-yellow-400">
                €{{ fmt(dash.frozen) }}
              </p>
            </div>
            <div class="bg-bg/50 rounded-xl p-3 text-center">
              <p class="text-[10px] text-muted mb-1">分红</p>
              <div v-if="dash === null"
                   class="h-5 w-12 bg-surface2 rounded animate-pulse mx-auto" />
              <p v-else class="text-sm font-bold" style="color:#00FF88">
                €{{ fmt(dash.dividendEarn) }}
              </p>
            </div>
          </div>

        </div>
      </section>

      <!-- ── 邀请 + 份额 ────────────────────────────────────────────── -->
      <section class="mb-4">
        <div class="grid grid-cols-2 gap-3">

          <!-- 邀请 -->
          <button
            class="card p-4 rounded-2xl text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/invite')"
          >
            <p class="text-[10px] text-muted mb-2 uppercase tracking-widest">邀请奖励</p>
            <div v-if="dash === null"
                 class="h-7 w-16 bg-surface2 rounded animate-pulse mb-1" />
            <template v-else>
              <p class="text-xl font-black" style="color:#00FF88">
                €{{ fmt(dash.inviteEarn) }}
              </p>
              <p class="text-xs text-muted mt-1">已邀请 {{ dash.inviteCount }} 人</p>
            </template>
            <p class="text-[10px] mt-2" style="color:#00FF8888">每人 €2 →</p>
          </button>

          <!-- 份额 -->
          <button
            class="card p-4 rounded-2xl text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/share')"
          >
            <p class="text-[10px] text-muted mb-2 uppercase tracking-widest">持有份额</p>
            <div v-if="dash === null"
                 class="h-7 w-16 bg-surface2 rounded animate-pulse mb-1" />
            <template v-else>
              <p class="text-xl font-black text-white">
                {{ dash.shareAmount ?? 0 }}
              </p>
              <p class="text-xs text-muted mt-1">份额 · 享分红</p>
            </template>
            <p class="text-[10px] mt-2 text-muted">购买份额 →</p>
          </button>

        </div>
      </section>

      <!-- ── 快捷操作 ───────────────────────────────────────────────── -->
      <section class="mb-6">
        <div class="space-y-2">
          <button
            class="card w-full flex items-center gap-3 px-4 py-3 rounded-xl
                   active:scale-[.98] transition-transform duration-100"
            @click="router.push('/leaderboard')"
          >
            <span class="text-lg">🏆</span>
            <span class="flex-1 text-sm font-semibold text-left">收益排行榜</span>
            <svg class="h-4 w-4 text-muted" fill="none" viewBox="0 0 24 24"
                 stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/>
            </svg>
          </button>
          <button
            class="card w-full flex items-center gap-3 px-4 py-3 rounded-xl
                   active:scale-[.98] transition-transform duration-100"
            @click="router.push('/invite')"
          >
            <span class="text-lg">🎁</span>
            <div class="flex-1 text-left">
              <p class="text-sm font-semibold">邀请好友</p>
            </div>
            <span class="text-xs font-bold" style="color:#00FF88">每人 +€2</span>
            <svg class="h-4 w-4 text-muted" fill="none" viewBox="0 0 24 24"
                 stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/>
            </svg>
          </button>
        </div>
      </section>

      <!-- ── 提现 ──────────────────────────────────────────────────── -->
      <section class="mb-6">
        <h2 class="font-semibold text-sm mb-3">提现</h2>
        <div class="card p-5 rounded-2xl space-y-4">

          <!-- 余额行 -->
          <div class="flex items-center justify-between">
            <div>
              <p class="text-xs text-muted mb-1">可提现余额</p>
              <div v-if="dash === null"
                   class="h-7 w-20 bg-surface2 rounded animate-pulse" />
              <p v-else class="text-2xl font-black text-white">
                €{{ fmt(dash.balance) }}
              </p>
            </div>
            <div v-if="dash && dash.frozen > 0" class="text-right">
              <p class="text-xs text-muted mb-1">提现中</p>
              <p class="text-sm text-yellow-400 font-semibold">€{{ fmt(dash.frozen) }}</p>
            </div>
          </div>

          <!-- 提现表单 -->
          <div class="space-y-3">
            <div>
              <label class="text-xs text-muted block mb-1">提现金额（最低 €10）</label>
              <input
                v-model="withdrawAmount"
                type="number" min="10" step="1" placeholder="0.00"
                class="w-full h-11 rounded-xl px-3 text-sm bg-surface2
                       border border-border text-white placeholder:text-muted
                       focus:outline-none focus:border-accent"
              />
            </div>
            <div>
              <label class="text-xs text-muted block mb-1">收款账号（PayPal / 银行）</label>
              <input
                v-model="withdrawAcct"
                type="text" placeholder="paypal:your@email.com"
                class="w-full h-11 rounded-xl px-3 text-sm bg-surface2
                       border border-border text-white placeholder:text-muted
                       focus:outline-none focus:border-accent"
              />
            </div>
            <p v-if="withdrawError" class="text-xs text-danger">{{ withdrawError }}</p>
            <p v-if="withdrawOk"    class="text-xs text-accent">✔ 提现申请已提交，1-3 个工作日处理</p>
            <button
              :disabled="withdrawing"
              class="w-full h-11 rounded-full font-bold text-sm
                     active:scale-95 transition-transform duration-100
                     disabled:opacity-50 disabled:cursor-not-allowed"
              style="background:#00FF88;color:#000"
              @click="submitWithdraw"
            >
              {{ withdrawing ? '提交中…' : '申请提现' }}
            </button>
          </div>

          <!-- 提现记录 -->
          <div v-if="withdrawList.length" class="border-t border-border pt-4 space-y-2">
            <p class="text-[10px] text-muted uppercase tracking-widest mb-2">提现记录</p>
            <div
              v-for="w in withdrawList.slice(0, 5)" :key="w.id"
              class="flex items-center justify-between text-sm"
            >
              <div>
                <p class="text-xs font-mono text-muted truncate max-w-[160px]">{{ w.account }}</p>
                <p class="text-[10px] text-muted/60">
                  {{ w.createdAt ? new Date(w.createdAt).toLocaleDateString('zh-CN') : '' }}
                </p>
              </div>
              <div class="text-right">
                <p class="font-semibold text-xs">€{{ fmt(w.amount) }}</p>
                <p class="text-[10px]"
                   :style="{
                     color: w.status === 'success' ? '#00FF88'
                          : w.status === 'reject'  ? '#FF6B6B'
                          : '#F59E0B'
                   }">
                  {{ w.status === 'success' ? '已到账'
                   : w.status === 'reject'  ? '已拒绝'
                   : '处理中' }}
                </p>
              </div>
            </div>
          </div>

        </div>
      </section>

      <!-- ── 我的店铺 ──────────────────────────────────────────────── -->
      <section class="mb-8">
        <div class="flex items-center justify-between mb-3">
          <h2 class="font-semibold text-sm">我的店铺</h2>
          <span class="text-muted text-xs">{{ shops.length }} 家</span>
        </div>

        <div v-if="!shops.length" class="card p-8 text-center">
          <p class="text-3xl mb-3">🏪</p>
          <p class="text-muted text-sm mb-4">还没有店铺</p>
          <div class="flex gap-2 justify-center">
            <button class="btn-primary max-w-[140px] text-sm"
                    @click="router.push('/template')">选模板开店</button>
            <button class="btn-ghost max-w-[140px] text-sm"
                    @click="router.push('/generate')">AI生成</button>
          </div>
        </div>

        <div v-else class="space-y-3">
          <div v-for="shop in shops" :key="shop.shopId" class="card overflow-hidden">
            <div
              class="flex items-center gap-3 p-3 cursor-pointer active:opacity-80 transition-opacity"
              :style="{ background: shop.gradient || '#1A1A1A' }"
              @click="router.push(`/shop/${shop.shopId}`)"
            >
              <div class="flex-1 min-w-0">
                <p class="font-semibold text-sm truncate"
                   :style="{ color: shop.theme?.text || '#fff' }">
                  {{ shop.name || 'AI设计款' }}
                </p>
                <p class="text-xs mt-0.5 truncate"
                   :style="{ color: shop.theme?.subText || '#9CA3AF' }">
                  {{ formatDate(shop.shopId) }}
                </p>
              </div>
              <svg class="h-4 w-4 shrink-0 opacity-60" fill="none"
                   viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                   :style="{ color: shop.theme?.text || '#fff' }">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/>
              </svg>
            </div>
            <div class="flex items-center gap-2 px-3 py-2.5 border-t border-border">
              <span class="text-muted text-xs flex-1 truncate">/shop/{{ shop.shopId }}</span>
              <button
                class="text-xs font-semibold px-3 py-1.5 rounded-full
                       active:scale-95 transition-transform duration-100"
                style="background:#00FF8820;color:#00FF88;border:1px solid #00FF8840"
                @click="shareShop(shop.shopId, shop.name)"
              >分享赚钱</button>
              <button
                class="text-xs font-semibold px-3 py-1.5 rounded-full
                       bg-danger/10 text-danger border border-danger/20
                       active:scale-95 transition-transform duration-100"
                @click="deleteShop(shop.shopId)"
              >删除</button>
            </div>
          </div>
        </div>
      </section>

      <!-- ── 新建入口 ──────────────────────────────────────────────── -->
      <section class="mb-8">
        <h2 class="font-semibold text-sm mb-3">新建</h2>
        <div class="grid grid-cols-2 gap-3">
          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/template')"
          >
            <span class="text-2xl mb-2 block">📋</span>
            <p class="font-semibold text-sm">模板开店</p>
            <p class="text-muted text-xs mt-0.5">选模板一键生成</p>
          </button>
          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/generate')"
          >
            <span class="text-2xl mb-2 block">✨</span>
            <p class="font-semibold text-sm">AI生成</p>
            <p class="text-muted text-xs mt-0.5">设计图做成商品</p>
          </button>
        </div>
      </section>

      <!-- 用户 ID（调试用）-->
      <p class="text-center text-muted text-[10px] font-mono break-all">
        ID: {{ userId }}
      </p>

    </div><!-- /max-w -->

    <!-- 底部导航 -->
    <nav class="fixed bottom-0 left-0 right-0 z-20
                bg-bg/95 backdrop-blur-md border-t border-border
                flex items-center justify-around
                px-2 pt-2 pb-[calc(.5rem+env(safe-area-inset-bottom))]">
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M3 12l9-9 9 9M5 10v9a1 1 0 001 1h4v-5h4v5h4a1 1 0 001-1v-9"/>
        </svg>
        <span class="text-[10px] font-semibold">首页</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/generate')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/>
        </svg>
        <span class="text-[10px] font-semibold">AI生成</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/template')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"/>
        </svg>
        <span class="text-[10px] font-semibold">模板</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/leaderboard')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
        </svg>
        <span class="text-[10px] font-semibold">排行</span>
      </button>
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-accent">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
        </svg>
        <span class="text-[10px] font-semibold">我的</span>
      </button>
    </nav>

  </div>
</template>
