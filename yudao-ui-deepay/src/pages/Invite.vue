<!--
  Invite.vue — 邀请好友赚钱
  路径：/invite

  功能：
  ✔ 展示已邀请人数 + 累计奖励
  ✔ 最近邀请列表
  ✔ 一键复制 / 分享带 ref 邀请链接
  ✔ 无数据时展示说明，引导用户分享
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getInvites } from '@/api/invite'
import { initUserId, buildShareLink } from '@/utils/user'

const router = useRouter()
const userId = initUserId()

const count     = ref(0)
const bonusEarn = ref(0)
const list      = ref([])
const loading   = ref(true)

// First shop in localStorage → used for share link; fall back to origin root
const myShopId = computed(() => {
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key?.startsWith('shop_')) return key.replace('shop_', '')
  }
  return ''
})

const inviteLink = computed(() =>
  myShopId.value
    ? buildShareLink(myShopId.value, userId)
    : `${location.origin}?ref=${userId}`,
)

async function load() {
  try {
    const data  = await getInvites()
    count.value     = data?.count     ?? 0
    bonusEarn.value = data?.bonusEarn ?? 0
    list.value      = data?.list      ?? []
  } catch (_) {
    // keep defaults — don't crash the page
  } finally {
    loading.value = false
  }
}

function share() {
  const text = `我在 Deepay 开了个店，你来注册用我的链接，下单后我们俩各得 €2 奖励 🎁\n${inviteLink.value}`

  if (navigator.share) {
    navigator.share({
      title: '邀请你加入 Deepay',
      text:  `Deepay — AI开店赚钱，我的邀请链接 👇`,
      url:   inviteLink.value,
    }).catch(() => {})
  } else {
    navigator.clipboard?.writeText(text)
      .then(()  => alert('邀请链接已复制 🎉'))
      .catch(()  => alert(`邀请链接：\n${inviteLink.value}`))
  }
}

onMounted(load)
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
      <span class="font-semibold text-sm flex-1">🎁 邀请好友赚钱</span>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-5 pb-24">

      <!-- ── 邀请奖励说明横幅 ──────────────────────────────────── -->
      <section class="mb-6">
        <div class="rounded-2xl p-5 text-center"
             style="background:linear-gradient(135deg,#0f2d1a,#111)">
          <p class="text-4xl mb-3">🎁</p>
          <p class="text-xl font-black text-white mb-1">每邀请1位好友</p>
          <p class="text-3xl font-black" style="color:#00FF88">得 €2 奖励</p>
          <p class="text-xs text-muted mt-3">
            好友首次下单即触发 · 奖励自动到账 · 无上限
          </p>
        </div>
      </section>

      <!-- ── 我的邀请数据 ──────────────────────────────────────── -->
      <section class="mb-6">
        <div class="card p-5 rounded-2xl">
          <div class="grid grid-cols-2 gap-4 mb-5">
            <div class="text-center">
              <div v-if="loading"
                   class="h-9 w-16 bg-surface2 rounded animate-pulse mx-auto mb-1" />
              <p v-else class="text-3xl font-black text-white">{{ count }}</p>
              <p class="text-xs text-muted">已邀请人数</p>
            </div>
            <div class="text-center">
              <div v-if="loading"
                   class="h-9 w-20 bg-surface2 rounded animate-pulse mx-auto mb-1" />
              <p v-else class="text-3xl font-black" style="color:#00FF88">
                €{{ Number(bonusEarn).toFixed(2) }}
              </p>
              <p class="text-xs text-muted">邀请奖励</p>
            </div>
          </div>

          <!-- 邀请链接展示 -->
          <div class="bg-surface2 rounded-xl p-3 mb-4">
            <p class="text-[10px] text-muted mb-1 uppercase tracking-widest">我的专属邀请链接</p>
            <p class="text-xs text-white/80 font-mono truncate">{{ inviteLink }}</p>
          </div>

          <!-- 分享按钮 -->
          <button
            class="w-full h-11 rounded-full font-bold text-sm
                   active:scale-95 transition-transform duration-100"
            style="background:#00FF88;color:#000"
            @click="share"
          >
            复制邀请链接 · 分享给好友
          </button>
        </div>
      </section>

      <!-- ── 邀请规则 ──────────────────────────────────────────── -->
      <section class="mb-6">
        <h2 class="font-semibold text-sm mb-3">规则说明</h2>
        <div class="card p-4 rounded-2xl space-y-3">
          <div class="flex items-start gap-3">
            <span class="text-lg shrink-0">1️⃣</span>
            <div>
              <p class="text-sm font-semibold">分享你的专属链接</p>
              <p class="text-xs text-muted mt-0.5">把链接发给朋友，他们点击后进入你的店铺</p>
            </div>
          </div>
          <div class="flex items-start gap-3">
            <span class="text-lg shrink-0">2️⃣</span>
            <div>
              <p class="text-sm font-semibold">好友完成首次下单</p>
              <p class="text-xs text-muted mt-0.5">好友在平台完成第一笔购买即触发奖励</p>
            </div>
          </div>
          <div class="flex items-start gap-3">
            <span class="text-lg shrink-0">3️⃣</span>
            <div>
              <p class="text-sm font-semibold">你自动获得 €2 奖励</p>
              <p class="text-xs text-muted mt-0.5">奖励自动入账，可随时提现，无上限</p>
            </div>
          </div>
        </div>
      </section>

      <!-- ── 邀请明细 ──────────────────────────────────────────── -->
      <section v-if="list.length">
        <h2 class="font-semibold text-sm mb-3">邀请记录</h2>
        <div class="card rounded-2xl divide-y divide-border">
          <div
            v-for="(item, i) in list"
            :key="item.userId || i"
            class="flex items-center justify-between px-4 py-3"
          >
            <div>
              <p class="text-sm font-mono text-white/70 truncate max-w-[180px]">
                {{ item.userId }}
              </p>
              <p class="text-[10px] text-muted mt-0.5">首次下单已触发</p>
            </div>
            <p class="text-sm font-bold" style="color:#00FF88">
              +€{{ Number(item.amount).toFixed(2) }}
            </p>
          </div>
        </div>
      </section>

      <!-- 空态 -->
      <div v-else-if="!loading" class="text-center py-8">
        <p class="text-3xl mb-3">👥</p>
        <p class="text-muted text-sm">还没有邀请记录</p>
        <p class="text-muted text-xs mt-1">分享链接给好友，他们下单你就得 €2 💰</p>
      </div>

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
