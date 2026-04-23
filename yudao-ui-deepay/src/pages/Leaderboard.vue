<!--
  Leaderboard.vue — 收益排行榜
  路径：/leaderboard

  功能：
  ✔ 展示前50名收益用户排行
  ✔ 高亮当前用户行
  ✔ 我的排名卡 + 一键分享（带 ref）
  ✔ 无数据时显示 mock 前10名，增强可信度
  ✔ 每30秒自动刷新
-->
<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getLeaderboard, getMyRank } from '@/api/user'
import { initUserId, buildShareLink } from '@/utils/user'

const router  = useRouter()
const userId  = initUserId()

const list    = ref([])
const my      = ref({ rank: null, totalEarn: 0 })
const loading = ref(true)

// ── Mock fallback (shown when backend returns nothing) ──────────────────────
const MOCK_LIST = [
  { rank: 1,  userId: 'u_mock_0001', nickname: 'User0001', totalEarn: 1280.00 },
  { rank: 2,  userId: 'u_mock_0023', nickname: 'User0023', totalEarn:  960.50 },
  { rank: 3,  userId: 'u_mock_0045', nickname: 'User0045', totalEarn:  741.20 },
  { rank: 4,  userId: 'u_mock_0012', nickname: 'User0012', totalEarn:  530.80 },
  { rank: 5,  userId: 'u_mock_0067', nickname: 'User0067', totalEarn:  412.00 },
  { rank: 6,  userId: 'u_mock_0089', nickname: 'User0089', totalEarn:  318.40 },
  { rank: 7,  userId: 'u_mock_0034', nickname: 'User0034', totalEarn:  256.90 },
  { rank: 8,  userId: 'u_mock_0056', nickname: 'User0056', totalEarn:  198.30 },
  { rank: 9,  userId: 'u_mock_0078', nickname: 'User0078', totalEarn:  155.70 },
  { rank: 10, userId: 'u_mock_0090', nickname: 'User0090', totalEarn:  102.50 },
]

// ── Medal helper ────────────────────────────────────────────────────────────
function medal(rank) {
  if (rank === 1) return '🥇'
  if (rank === 2) return '🥈'
  if (rank === 3) return '🥉'
  return ''
}

// ── Load data ───────────────────────────────────────────────────────────────
async function load() {
  try {
    const [board, me] = await Promise.allSettled([
      getLeaderboard(),
      getMyRank(),
    ])

    const boardData = board.status === 'fulfilled' ? board.value : null
    list.value = Array.isArray(boardData) && boardData.length
      ? boardData
      : MOCK_LIST

    if (me.status === 'fulfilled' && me.value) {
      my.value = {
        rank:      me.value.rank      ?? null,
        totalEarn: me.value.totalEarn ?? 0,
      }
    }
  } catch (_) {
    list.value = MOCK_LIST
  } finally {
    loading.value = false
  }
}

// ── Share ────────────────────────────────────────────────────────────────────
// Find the first shop in localStorage to build a real share link; fall back to /
const myShopId = computed(() => {
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key?.startsWith('shop_')) return key.replace('shop_', '')
  }
  return ''
})

function share() {
  const rankText  = my.value.rank   ? `第${my.value.rank}名` : '排行榜'
  const earnText  = Number(my.value.totalEarn).toFixed(2)
  const shareText = `我在 Deepay 排行榜${rankText}，已赚 €${earnText}，来试试 👇`
  const shareUrl  = myShopId.value
    ? buildShareLink(myShopId.value, userId)
    : `${location.origin}?ref=${userId}`
  const full = `${shareText}\n${shareUrl}`

  if (navigator.share) {
    navigator.share({ title: 'Deepay 收益排行榜', text: shareText, url: shareUrl }).catch(() => {})
  } else {
    navigator.clipboard?.writeText(full)
      .then(()  => alert('已复制分享内容 🎉'))
      .catch(()  => alert(`分享内容：\n${full}`))
  }
}

// ── Auto-refresh every 30s ──────────────────────────────────────────────────
let timer = null
onMounted(() => {
  load()
  timer = setInterval(load, 30_000)
})
onUnmounted(() => clearInterval(timer))
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
      <span class="font-semibold text-sm flex-1">🏆 收益排行榜</span>
      <!-- 刷新指示：loading dot -->
      <span v-if="loading" class="w-2 h-2 rounded-full bg-accent animate-pulse" />
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-5 pb-28">

      <!-- ── 我的排名卡 ───────────────────────────────────────── -->
      <section class="mb-6">
        <div class="card p-5 rounded-2xl"
             style="background:linear-gradient(135deg,#0f2d1a,#111)">

          <div class="flex items-start justify-between mb-4">
            <div>
              <p class="text-xs text-muted mb-1">我的排名</p>
              <p class="text-3xl font-black text-accent">
                {{ my.rank ? `# ${my.rank}` : '—' }}
              </p>
            </div>
            <div class="text-right">
              <p class="text-xs text-muted mb-1">我的收益</p>
              <p class="text-xl font-bold text-white">
                €{{ Number(my.totalEarn).toFixed(2) }}
              </p>
            </div>
          </div>

          <p class="text-xs text-muted mb-4">
            分享给好友 → 他们购买 → 你的收益上涨 → 排名提升 🚀
          </p>

          <button
            class="w-full h-11 rounded-full font-bold text-sm
                   active:scale-95 transition-transform duration-100"
            style="background:#00FF88;color:#000"
            @click="share"
          >
            {{ my.rank ? `我排第${my.rank}名 · 分享炫耀 🎉` : '分享我的排名' }}
          </button>
        </div>
      </section>

      <!-- ── 邀请好友得 €2 提示条 ───────────────────────────────────── -->
      <section class="mb-6">
        <button
          class="w-full card px-4 py-3 rounded-xl flex items-center gap-3
                 active:scale-[.98] transition-transform duration-100"
          @click="router.push('/invite')"
        >
          <span class="text-xl shrink-0">🎁</span>
          <div class="flex-1 text-left">
            <p class="text-sm font-semibold">邀请好友下单得 €2</p>
            <p class="text-xs text-muted mt-0.5">分享链接 → 好友首次下单 → 自动到账</p>
          </div>
          <svg class="h-4 w-4 text-muted shrink-0" fill="none" viewBox="0 0 24 24"
               stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </section>

      <!-- ── 排行榜列表 ───────────────────────────────────────── -->
      <section>
        <p class="text-[10px] text-muted uppercase tracking-widest mb-3">
          本月收益 TOP {{ list.length }}
        </p>

        <!-- skeleton -->
        <div v-if="loading && !list.length" class="space-y-2">
          <div v-for="n in 8" :key="n"
               class="h-12 bg-surface2 rounded-xl animate-pulse" />
        </div>

        <div v-else class="space-y-1.5">
          <div
            v-for="u in list"
            :key="u.userId"
            class="flex items-center gap-3 px-4 py-3 rounded-xl transition-colors"
            :class="u.userId === userId
              ? 'bg-accent/10 border border-accent/30'
              : 'bg-surface border border-border'"
          >
            <!-- 排名 / 奖章 -->
            <div class="w-8 text-center shrink-0">
              <span v-if="u.rank <= 3" class="text-lg leading-none">
                {{ medal(u.rank) }}
              </span>
              <span v-else class="text-xs text-muted font-mono">
                #{{ u.rank }}
              </span>
            </div>

            <!-- 昵称 -->
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold truncate"
                 :class="u.userId === userId ? 'text-accent' : 'text-white'">
                {{ u.nickname }}
                <span v-if="u.userId === userId"
                      class="ml-1 text-[10px] font-normal text-accent/70">（我）</span>
              </p>
            </div>

            <!-- 收益 -->
            <div class="text-right shrink-0">
              <p class="text-sm font-bold"
                 :class="u.userId === userId ? 'text-accent' : 'text-green-400'">
                €{{ Number(u.totalEarn).toFixed(2) }}
              </p>
            </div>
          </div>
        </div>

        <!-- 当前用户未在榜单中时，底部补充提示 -->
        <div v-if="!loading && my.rank && !list.some(u => u.userId === userId)"
             class="mt-3 px-4 py-3 rounded-xl bg-accent/10 border border-accent/30
                    flex items-center gap-3">
          <div class="w-8 text-center text-xs text-muted font-mono shrink-0">
            #{{ my.rank }}
          </div>
          <p class="flex-1 text-sm font-semibold text-accent">
            我的位置（榜外）
          </p>
          <p class="text-sm font-bold text-accent">
            €{{ Number(my.totalEarn).toFixed(2) }}
          </p>
        </div>
      </section>

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
      <!-- 排行榜 (active) -->
      <button class="flex flex-col items-center gap-0.5 px-3 py-1 text-accent">
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
