<!--
  Home.vue — App首页（功能入口，不是宣传页）

  功能：
  ✔ AI输入框（主入口）→ /generate?prompt=...
  ✔ 双入口 CTA：AI生成 / 模板开店
  ✔ 模板横滑预览 → /template/:id
  ✔ 快捷入口：我的店铺 / 全部模板
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { templates } from '@/data/templates'
import { getQuotaInfo } from '@/api/design'
import { initUserId } from '@/utils/user'

const router  = useRouter()
const prompt  = ref('')

const USER_ID      = initUserId()
const remainFree   = ref(3)
const remainPaid   = ref(0)
const totalRemain  = computed(() => remainFree.value + remainPaid.value)

onMounted(async () => {
  try {
    const info = await getQuotaInfo(USER_ID)
    remainFree.value = info?.remainFree ?? 3
    remainPaid.value = info?.remainPaid ?? 0
  } catch (_) {}
})

function goGenerate() {
  const q = prompt.value.trim()
  router.push(q ? `/generate?prompt=${encodeURIComponent(q)}` : '/generate')
}

function goTemplate(id) {
  router.push(`/template/${id}`)
}

// My stores from localStorage
const myShops = computed(() => {
  const result = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key?.startsWith('shop_')) {
      try {
        const data = JSON.parse(localStorage.getItem(key))
        result.push({ id: key.replace('shop_', ''), ...data })
      } catch (_) {}
    }
  }
  return result.slice(0, 3)
})
</script>

<template>
  <div class="min-h-screen bg-bg text-white">

    <!-- ── 顶部导航 ──────────────────────────────────── -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md
                   border-b border-border px-4 py-3
                   flex items-center justify-between">
      <div class="flex items-center gap-2">
        <span class="w-7 h-7 rounded-lg bg-accent flex items-center justify-center
                     text-black font-black text-sm select-none">D</span>
        <span class="font-semibold text-sm tracking-wide">Deepay</span>
      </div>
      <span
        :class="[
          'text-xs font-semibold px-3 py-1.5 rounded-full',
          totalRemain <= 0 ? 'badge-empty' :
          totalRemain <= 1 ? 'badge-low'   : 'badge-ok'
        ]"
      >
        {{ totalRemain <= 0 ? '次数已用完' : `剩余 ${totalRemain} 次` }}
      </span>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pb-24">

      <!-- ── Hero：AI输入区 ─────────────────────────── -->
      <section class="pt-8 pb-6">
        <h1 class="text-2xl font-bold mb-1">开始设计</h1>
        <p class="text-muted text-sm mb-5">输入描述 AI 即刻生成，或选模板直接开店</p>

        <!-- 输入框 -->
        <div class="relative mb-3">
          <input
            v-model="prompt"
            type="text"
            placeholder="描述你想要的款式，例如：黑色街头卫衣"
            class="w-full bg-surface border border-border rounded-2xl
                   px-4 py-3.5 pr-12 text-sm text-white placeholder-muted
                   focus:outline-none focus:border-accent transition-colors"
            @keydown.enter="goGenerate"
          />
          <!-- 发送图标 -->
          <button
            class="absolute right-3 top-1/2 -translate-y-1/2
                   w-8 h-8 rounded-full bg-accent text-black
                   flex items-center justify-center active:scale-90
                   transition-transform duration-100"
            @click="goGenerate"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24"
                 stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round"
                    d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </button>
        </div>

        <!-- 双入口 CTA -->
        <div class="flex gap-3">
          <button class="btn-primary flex-1 text-sm font-bold" @click="goGenerate">
            ✨ AI生成
          </button>
          <button
            class="btn-ghost flex-1 text-sm font-semibold"
            @click="router.push('/template')"
          >
            🏪 模板开店
          </button>
        </div>
      </section>

      <!-- ── 模板横滑预览 ──────────────────────────── -->
      <section class="mb-8">
        <div class="flex items-center justify-between mb-3">
          <h2 class="font-semibold text-sm">快速开店模板</h2>
          <button
            class="text-accent text-xs font-semibold active:opacity-70"
            @click="router.push('/template')"
          >
            查看全部
          </button>
        </div>

        <div class="flex gap-3 overflow-x-auto scrollbar-hide pb-1">
          <div
            v-for="tpl in templates"
            :key="tpl.id"
            class="card shrink-0 w-[130px] cursor-pointer active:scale-95
                   transition-transform duration-100"
            @click="goTemplate(tpl.id)"
          >
            <!-- 模板封面 -->
            <div
              class="h-24 w-full rounded-t-2xl flex items-end p-2"
              :style="{ background: tpl.gradient }"
            >
              <span class="text-[10px] bg-black/40 backdrop-blur-sm
                           px-2 py-0.5 rounded-full text-white/80">
                {{ tpl.tag }}
              </span>
            </div>
            <div class="p-2.5">
              <p class="text-sm font-semibold">{{ tpl.name }}</p>
              <p class="text-[11px] text-muted mt-0.5">
                {{ tpl.products.length }} 款商品
              </p>
            </div>
          </div>
        </div>
      </section>

      <!-- ── AI 设计系统 ───────────────────────────── -->
      <section class="mb-8">
        <h2 class="font-semibold text-sm mb-3">AI 设计系统</h2>
        <div class="grid grid-cols-2 gap-3">

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100 hover:border-accent"
            style="border:1px solid #222"
            @click="router.push('/ai/design')"
          >
            <span class="text-2xl mb-2 block">🎯</span>
            <p class="font-semibold text-sm">AI出款</p>
            <p class="text-muted text-xs mt-0.5">可控出款，系列生成</p>
          </button>

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100 hover:border-accent"
            style="border:1px solid #222"
            @click="router.push('/inspiration')"
          >
            <span class="text-2xl mb-2 block">🎭</span>
            <p class="font-semibold text-sm">灵感库</p>
            <p class="text-xs mt-0.5" style="color:#A855F7">精选秀场 + 品牌图</p>
          </button>

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100 hover:border-accent"
            style="border:1px solid #222"
            @click="router.push('/ai/season')"
          >
            <span class="text-2xl mb-2 block">🌿</span>
            <p class="font-semibold text-sm">整季系列</p>
            <p class="text-muted text-xs mt-0.5">一键生成 A/B/C</p>
          </button>

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100 hover:border-accent"
            style="border:1px solid #222"
            @click="router.push('/redesign')"
          >
            <span class="text-2xl mb-2 block">🔧</span>
            <p class="font-semibold text-sm">改款工具</p>
            <p class="text-muted text-xs mt-0.5">参考图 → 新款</p>
          </button>

        </div>
      </section>

      <!-- ── 快捷入口 ─────────────────────────────── -->
      <section class="mb-8">
        <h2 class="font-semibold text-sm mb-3">快捷入口</h2>
        <div class="grid grid-cols-2 gap-3">

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/me')"
          >
            <span class="text-2xl mb-2 block">🏪</span>
            <p class="font-semibold text-sm">我的店铺</p>
            <p class="text-muted text-xs mt-0.5">
              {{ myShops.length > 0 ? `${myShops.length} 家店铺` : '还没有店铺' }}
            </p>
          </button>

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/generate')"
          >
            <span class="text-2xl mb-2 block">🎨</span>
            <p class="font-semibold text-sm">AI设计</p>
            <p class="text-muted text-xs mt-0.5">每日 {{ remainFree }} 次免费</p>
          </button>

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/redesign')"
          >
            <span class="text-2xl mb-2 block">✏️</span>
            <p class="font-semibold text-sm">AI改款</p>
            <p class="text-muted text-xs mt-0.5">上传参考图改款</p>
          </button>

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/inspiration')"
          >
            <span class="text-2xl mb-2 block">🎭</span>
            <p class="font-semibold text-sm">灵感库</p>
            <p class="text-muted text-xs mt-0.5" style="color:#A855F7">秀场 + 品牌图</p>
          </button>

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/leaderboard')"
          >
            <span class="text-2xl mb-2 block">🏆</span>
            <p class="font-semibold text-sm">收益排行榜</p>
            <p class="text-muted text-xs mt-0.5">看看你排第几名</p>
          </button>

          <button
            class="card p-4 text-left active:scale-95 transition-transform duration-100"
            @click="router.push('/invite')"
          >
            <span class="text-2xl mb-2 block">🎁</span>
            <p class="font-semibold text-sm">邀请好友</p>
            <p class="text-xs mt-0.5" style="color:#00FF88">每邀请一人得 €2</p>
          </button>

        </div>
      </section>

      <!-- ── 我的店铺（有数据才显示）──────────────── -->
      <section v-if="myShops.length">
        <div class="flex items-center justify-between mb-3">
          <h2 class="font-semibold text-sm">最近店铺</h2>
          <button
            class="text-accent text-xs font-semibold active:opacity-70"
            @click="router.push('/me')"
          >
            全部
          </button>
        </div>

        <div class="space-y-2">
          <div
            v-for="shop in myShops"
            :key="shop.id"
            class="card flex items-center gap-3 p-3 cursor-pointer
                   active:scale-[.98] transition-transform duration-100"
            @click="router.push(`/shop/${shop.id}`)"
          >
            <div
              class="w-12 h-12 rounded-xl shrink-0"
              :style="{ background: shop.gradient || '#1A1A1A' }"
            />
            <div class="min-w-0 flex-1">
              <p class="font-semibold text-sm truncate">{{ shop.name || 'AI设计款' }}</p>
              <p class="text-muted text-xs mt-0.5 truncate">
                /shop/{{ shop.id }}
              </p>
            </div>
            <svg class="h-4 w-4 text-muted shrink-0" fill="none"
                 viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/>
            </svg>
          </div>
        </div>
      </section>

    </div><!-- /max-w -->

    <!-- ── 底部导航栏 ─────────────────────────────── -->
    <nav class="fixed bottom-0 left-0 right-0 z-20
                bg-bg/95 backdrop-blur-md border-t border-border
                flex items-center justify-around
                px-2 pt-2 pb-[calc(.5rem+env(safe-area-inset-bottom))]">
      <button
        class="flex flex-col items-center gap-0.5 px-3 py-1
               text-accent"
        @click="router.push('/')"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M3 12l9-9 9 9M5 10v9a1 1 0 001 1h4v-5h4v5h4a1 1 0 001-1v-9"/>
        </svg>
        <span class="text-[10px] font-semibold">首页</span>
      </button>

      <button
        class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
               active:text-white transition-colors"
        @click="router.push('/generate')"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M12 4v16m8-8H4"/>
        </svg>
        <span class="text-[10px] font-semibold">AI生成</span>
      </button>

      <button
        class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
               active:text-white transition-colors"
        @click="router.push('/template')"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"/>
        </svg>
        <span class="text-[10px] font-semibold">模板</span>
      </button>

      <button
        class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
               active:text-white transition-colors"
        @click="router.push('/leaderboard')"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
        </svg>
        <span class="text-[10px] font-semibold">排行</span>
      </button>

      <button
        class="flex flex-col items-center gap-0.5 px-3 py-1 text-muted
               active:text-white transition-colors"
        @click="router.push('/me')"
      >
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
