<!--
  Share.vue — 购买份额页
  路径：/share

  功能：
  ✔ 展示当前持有份额（来自 dashboard）
  ✔ 平台分红说明
  ✔ 购买份额入口（跳转支付）
  ✔ 份额权益说明
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboard } from '@/api/user'
import { createPayment } from '@/api/order'
import { initUserId } from '@/utils/user'

const router = useRouter()
const userId = initUserId()

const dash       = ref(null)
const dashError  = ref(false)
const buying     = ref(false)
const buyError   = ref('')

const PLANS = [
  { id: 'SHARE_S', shares: 1,   priceEur: '9.99',  desc: '1 份额', tag: '' },
  { id: 'SHARE_M', shares: 5,   priceEur: '44.99', desc: '5 份额', tag: '⭐ 推荐' },
  { id: 'SHARE_L', shares: 10,  priceEur: '79.99', desc: '10 份额', tag: '💰 最划算' },
]

const selectedPlan = ref('SHARE_M')

async function loadDashboard() {
  try {
    dash.value = await getDashboard()
  } catch (error) {
    dashError.value = true
    console.error('Failed to load dashboard:', error)
    dash.value = { shareAmount: 0, dividendEarn: 0, totalEarn: 0 }
  }
}

async function buyShares() {
  if (buying.value) return
  buyError.value = ''
  buying.value   = true
  try {
    const res = await createPayment(userId, selectedPlan.value)
    if (res?.payUrl) {
      window.location.href = res.payUrl
    } else {
      buyError.value = '支付链接获取失败，请重试'
    }
  } catch (e) {
    buyError.value = e?.response?.data?.msg || '购买失败，请稍后重试'
  } finally {
    buying.value = false
  }
}

const DISCLAIMER = '份额购买后不可退款 · 分红按月结算 · 平台有权更新分红政策'

const fmt = v => Number(v ?? 0).toFixed(2)

onMounted(loadDashboard)
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
      <span class="font-semibold text-sm flex-1">📈 购买份额</span>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-5 pb-28">

      <!-- ── 我的份额总览 ──────────────────────────────────── -->
      <section class="mb-6">
        <div class="card p-5 rounded-2xl"
             style="background:linear-gradient(135deg,#0f2d1a,#111)">
          <div class="grid grid-cols-2 gap-4">
            <div class="text-center">
              <p class="text-[10px] text-muted mb-1 uppercase tracking-widest">持有份额</p>
              <div v-if="dash === null"
                   class="h-9 w-14 bg-surface2 rounded animate-pulse mx-auto" />
              <p v-else class="text-3xl font-black text-white">
                {{ dash.shareAmount ?? 0 }}
              </p>
              <p class="text-xs text-muted mt-0.5">份</p>
            </div>
            <div class="text-center">
              <p class="text-[10px] text-muted mb-1 uppercase tracking-widest">分红收益</p>
              <div v-if="dash === null"
                   class="h-9 w-20 bg-surface2 rounded animate-pulse mx-auto" />
              <p v-else class="text-3xl font-black" style="color:#00FF88">
                €{{ fmt(dash.dividendEarn) }}
              </p>
              <p class="text-xs text-muted mt-0.5">累计到账</p>
            </div>
          </div>
        </div>
      </section>

      <!-- ── 权益说明 ──────────────────────────────────────── -->
      <section class="mb-6">
        <h2 class="font-semibold text-sm mb-3">份额权益</h2>
        <div class="card p-4 rounded-2xl space-y-3">
          <div class="flex items-start gap-3">
            <span class="text-lg shrink-0">💰</span>
            <div>
              <p class="text-sm font-semibold">平台分红</p>
              <p class="text-xs text-muted mt-0.5">
                每月按持有份额比例分配平台净利润，自动到账
              </p>
            </div>
          </div>
          <div class="flex items-start gap-3">
            <span class="text-lg shrink-0">📊</span>
            <div>
              <p class="text-sm font-semibold">透明结算</p>
              <p class="text-xs text-muted mt-0.5">
                每月1日公布分红数据，可在「我的资产」实时查看
              </p>
            </div>
          </div>
          <div class="flex items-start gap-3">
            <span class="text-lg shrink-0">🔒</span>
            <div>
              <p class="text-sm font-semibold">长期持有</p>
              <p class="text-xs text-muted mt-0.5">
                份额永久有效，平台运营期间持续享受分红权益
              </p>
            </div>
          </div>
        </div>
      </section>

      <!-- ── 购买套餐 ────────────────────────────────────── -->
      <section class="mb-6">
        <h2 class="font-semibold text-sm mb-3">购买份额</h2>
        <div class="space-y-3">
          <div
            v-for="plan in PLANS" :key="plan.id"
            class="relative card p-4 rounded-2xl flex items-center gap-3 cursor-pointer
                   border transition-all duration-200"
            :class="selectedPlan === plan.id
              ? 'border-accent bg-accent/5'
              : 'border-border'"
            :style="selectedPlan === plan.id
              ? 'box-shadow: 0 0 0 1px #00FF88' : ''"
            @click="selectedPlan = plan.id"
          >
            <span v-if="plan.tag"
                  class="absolute -top-2.5 right-3 bg-accent text-black
                         text-[10px] font-bold px-2 py-0.5 rounded-full">
              {{ plan.tag }}
            </span>
            <div class="flex-1">
              <p class="font-semibold text-sm">{{ plan.desc }}</p>
              <p class="text-xs text-muted mt-0.5">每份额享受平台月度分红</p>
            </div>
            <p :class="['text-xl font-black',
                        selectedPlan === plan.id ? 'text-accent' : 'text-white']">
              €{{ plan.priceEur }}
            </p>
          </div>
        </div>

        <p v-if="buyError" class="text-xs text-danger mt-3 text-center">{{ buyError }}</p>

        <button
          :disabled="buying"
          class="w-full h-12 rounded-full font-bold text-sm mt-5
                 active:scale-95 transition-transform duration-100
                 disabled:opacity-50 disabled:cursor-not-allowed
                 flex items-center justify-center gap-2"
          style="background:#00FF88;color:#000"
          @click="buyShares"
        >
          <svg v-if="buying" class="animate-spin h-4 w-4 shrink-0"
               viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor"
                    stroke-opacity=".3" stroke-width="3"/>
            <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                  stroke-width="3" stroke-linecap="round"/>
          </svg>
          {{ buying ? '跳转支付中…' : '立即购买' }}
        </button>
      </section>

      <!-- ── 温馨提示 ──────────────────────────────────────── -->
      <p class="text-center text-muted text-xs leading-relaxed">
        {{ DISCLAIMER }}
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
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
                     active:text-white transition-colors"
              @click="router.push('/me')">
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
