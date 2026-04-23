<!--
  Detail.vue — 款式详情页
  路由：/detail?img=<url>&category=外套&style=欧美

  流程：查看大图 → 生成类似款 / 加入小店 / 分享
-->
<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route  = useRoute()
const router = useRouter()

const imgSrc   = computed(() => route.query.img      || '')
const category = computed(() => route.query.category || '外套')
const style    = computed(() => route.query.style    || '欧美')

function generateSimilar() {
  router.push({
    path: '/generate',
    query: { category: category.value, style: style.value },
  })
}

function share() {
  const url = window.location.href
  if (navigator.share) {
    navigator.share({ title: `${category.value} · ${style.value}`, url })
  } else {
    navigator.clipboard?.writeText(url)
    alert('链接已复制！')
  }
}
</script>

<template>
  <div class="min-h-screen bg-bg text-white">

    <!-- 顶部返回 -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md
                   border-b border-border px-4 py-3 flex items-center gap-3">
      <button class="text-muted active:text-white transition-colors"
              @click="router.back()">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <span class="font-semibold text-sm">款式详情</span>
    </header>

    <!-- 主图 -->
    <div class="w-full aspect-square bg-surface2 overflow-hidden">
      <img v-if="imgSrc"
           :src="imgSrc"
           :alt="`${category} ${style}`"
           class="w-full h-full object-cover" />
      <div v-else class="w-full h-full flex items-center justify-center text-muted text-sm">
        暂无图片
      </div>
    </div>

    <!-- 信息 -->
    <div class="px-5 pt-5 pb-32">
      <h2 class="text-lg font-bold mb-1">{{ category }} · {{ style }}</h2>
      <p class="text-muted text-sm leading-relaxed">
        AI基于1688 / TikTok / Shein实时趋势生成的热卖款式
      </p>
    </div>

    <!-- 固定底部操作栏 -->
    <div class="fixed bottom-0 left-0 right-0 z-20
                bg-bg/95 backdrop-blur-md border-t border-border
                px-4 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]
                flex gap-3 max-w-[480px] mx-auto left-0 right-0">

      <!-- 分享 -->
      <button
        class="bg-surface2 text-white border border-border rounded-2xl
               h-12 px-4 flex items-center justify-center gap-1.5
               text-sm font-semibold active:scale-95 transition-transform duration-100 shrink-0"
        @click="share"
      >
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 1 1 0-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 1 0 5.367-2.684 3 3 0 0 0-5.367 2.684zm0 9.316a3 3 0 1 0 5.368 2.684 3 3 0 0 0-5.368-2.684z"/>
        </svg>
        分享
      </button>

      <!-- 生成类似款 -->
      <button
        class="bg-accent text-black font-bold h-12 flex-1 rounded-2xl
               active:scale-95 transition-transform duration-100 text-sm"
        @click="generateSimilar"
      >
        生成类似款 →
      </button>

    </div>

  </div>
</template>
