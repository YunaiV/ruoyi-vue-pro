<!--
  TemplatePreview.vue — 模拟店铺预览 + 一键开店
  路径：/template/:id

  规则：所有颜色绑定 tpl.theme.*，不写死
-->
<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { templates } from '@/data/templates'
import { createShop } from '@/api/shop'
import { initUserId, buildShareLink, shareOrCopy } from '@/utils/user'

const route  = useRoute()
const router = useRouter()

const tpl = computed(() =>
  templates.find(t => t.id === route.params.id) || null
)

const opening = ref(false)

const MY_USER_ID = initUserId()

async function createShopAndGo() {
  if (!tpl.value || opening.value) return
  opening.value = true
  try {
    const { shopId } = await createShop({
      templateId: tpl.value.id,
      type:       tpl.value.type,
      name:       tpl.value.name,
      theme:      tpl.value.theme,
      gradient:   tpl.value.gradient,
      style:      tpl.value.style,
      products:   tpl.value.products,
    })

    // 开店成功 → 先弹分享，再跳店铺
    const link = buildShareLink(shopId, MY_USER_ID)
    shareOrCopy(link, tpl.value.name || '我的店铺')

    router.push(`/shop/${shopId}`)
  } catch (_) {
    opening.value = false
  }
}

function share() {
  const url = window.location.href
  if (navigator.share) {
    navigator.share({ title: tpl.value?.name, url })
  } else {
    navigator.clipboard?.writeText(url)
      .then(() => alert('链接已复制'))
  }
}
</script>

<template>
  <!-- 整页背景由模板主题控制 -->
  <div
    class="min-h-screen"
    :style="tpl ? { background: tpl.theme.bg, color: tpl.theme.text } : {}"
  >

    <!-- 顶部导航（主题色边框）-->
    <header
      class="sticky top-0 z-10 backdrop-blur-md px-4 py-3
             flex items-center justify-between"
      :style="tpl ? {
        background: tpl.theme.bg + 'E6',
        borderBottom: `1px solid ${tpl.theme.border}`
      } : {}"
    >
      <button
        class="active:opacity-60 transition-opacity"
        :style="tpl ? { color: tpl.theme.subText } : {}"
        @click="router.back()"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <span class="font-semibold text-sm">{{ tpl?.name || '模板预览' }}</span>
      <button
        class="active:opacity-60 transition-opacity"
        :style="tpl ? { color: tpl.theme.subText } : {}"
        @click="share"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
                d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 1 1 0-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 1 0 5.367-2.684 3 3 0 0 0-5.367 2.684zm0 9.316a3 3 0 1 0 5.368 2.684 3 3 0 0 0-5.368-2.684z"/>
        </svg>
      </button>
    </header>

    <!-- 模板不存在 -->
    <div v-if="!tpl"
         class="flex flex-col items-center justify-center min-h-[60vh] gap-4 text-center px-6">
      <p class="text-4xl">🤔</p>
      <p class="text-sm opacity-60">模板不存在</p>
      <button
        class="h-12 px-8 rounded-full font-bold text-sm"
        style="background:#00FF88;color:#000"
        @click="router.push('/template')"
      >
        返回模板列表
      </button>
    </div>

    <!-- 模拟店铺预览 -->
    <template v-else>
      <div class="pb-28">

        <!-- 店铺横幅（主题渐变）-->
        <div
          class="w-full h-56 flex flex-col justify-end p-5"
          :style="{ background: tpl.gradient }"
        >
          <span
            class="text-xs self-start px-2 py-0.5 rounded-full mb-2"
            :style="{ background: tpl.theme.primary + '33', color: tpl.theme.primary }"
          >
            {{ tpl.tag }}
          </span>
          <h1 class="text-2xl font-bold" :style="{ color: tpl.theme.text }">
            {{ tpl.name }}
          </h1>
          <p class="text-sm mt-1" :style="{ color: tpl.theme.subText }">
            {{ tpl.products.length }} 款精选商品
          </p>
        </div>

        <!-- 单品展示（single）-->
        <div v-if="tpl.type === 'single'"
             class="max-w-[480px] mx-auto px-4 pt-5 space-y-4">
          <div
            v-for="(p, i) in tpl.products"
            :key="i"
            class="rounded-2xl overflow-hidden"
            :style="{ background: tpl.theme.card, border: `1px solid ${tpl.theme.border}` }"
          >
            <div class="w-full aspect-square" :style="{ background: p.gradient }" />
            <div class="p-4">
              <p class="font-semibold" :style="{ color: tpl.theme.text }">{{ p.name }}</p>
              <p class="text-lg font-bold mt-1" :style="{ color: tpl.theme.primary }">
                €{{ p.price }}
              </p>
              <button
                class="mt-3 w-full h-12 rounded-full font-bold text-sm"
                :style="{ background: tpl.theme.primary, color: tpl.theme.bg }"
              >
                立即购买
              </button>
            </div>
          </div>
        </div>

        <!-- 商品网格（grid）-->
        <div v-else-if="tpl.type === 'grid'"
             class="max-w-[480px] mx-auto px-4 pt-5">
          <div class="grid grid-cols-2 gap-3">
            <div
              v-for="(p, i) in tpl.products"
              :key="i"
              class="rounded-2xl overflow-hidden"
              :style="{ background: tpl.theme.card, border: `1px solid ${tpl.theme.border}` }"
            >
              <div class="w-full aspect-square" :style="{ background: p.gradient }" />
              <div class="p-3">
                <p class="font-semibold text-sm" :style="{ color: tpl.theme.text }">{{ p.name }}</p>
                <p class="font-bold mt-0.5" :style="{ color: tpl.theme.primary }">€{{ p.price }}</p>
              </div>
            </div>
          </div>
        </div>

      </div><!-- /pb-28 -->

      <!-- 底部固定操作栏（主题色 CTA）-->
      <div
        class="fixed bottom-0 left-0 right-0 z-20 backdrop-blur-md
               px-4 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]"
        :style="{
          background: tpl.theme.bg + 'F2',
          borderTop: `1px solid ${tpl.theme.border}`
        }"
      >
        <p class="text-xs text-center mb-3" :style="{ color: tpl.theme.subText }">
          预览效果 · 开店后获得专属分享链接
        </p>
        <button
          :disabled="opening"
          class="w-full h-12 rounded-full font-bold text-sm
                 flex items-center justify-center gap-2
                 active:scale-95 transition-transform duration-100
                 disabled:opacity-50 disabled:cursor-not-allowed"
          :style="{ background: tpl.theme.primary, color: tpl.theme.bg }"
          @click="createShopAndGo"
        >
          <svg v-if="opening" class="animate-spin h-4 w-4 shrink-0"
               viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor"
                    stroke-opacity=".3" stroke-width="3"/>
            <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                  stroke-width="3" stroke-linecap="round"/>
          </svg>
          {{ opening ? '正在生成店铺…' : '🚀 一键开店' }}
        </button>
      </div>
    </template>

  </div>
</template>
