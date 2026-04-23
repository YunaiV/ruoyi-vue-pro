<!--
  PayOrderResult.vue — 商品支付结果落地页
  路径：/pay/order/result?result=success|fail&orderId=xxx&shopId=yyy

  Jeepay 完成支付后跳回此页。
  - success → 展示订单号 + 返回店铺 / 首页
  - fail    → 重试入口
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { initUserId, buildShareLink, shareOrCopy } from '@/utils/user'

const route  = useRoute()
const router = useRouter()

const status  = ref('loading')
const orderId = ref('')
const shopId  = ref('')

const MY_USER_ID = initUserId()

onMounted(() => {
  const result = route.query.result || 'success'
  orderId.value = route.query.orderId || ''
  shopId.value  = route.query.shopId  || ''

  // Small delay so users feel the page "checked" the result
  setTimeout(() => {
    status.value = result === 'success' ? 'success' : 'fail'
  }, 800)
})

function goShop() {
  if (shopId.value) {
    router.push(`/shop/${shopId.value}?ref=${MY_USER_ID}`)
  } else {
    router.push('/')
  }
}

function shareShop() {
  if (!shopId.value) return
  shareOrCopy(buildShareLink(shopId.value, MY_USER_ID), '我开的小店')
}
</script>

<template>
  <div class="min-h-screen bg-bg text-white flex flex-col items-center
              justify-center px-6 py-16 max-w-[480px] mx-auto text-center">

    <!-- Loading -->
    <template v-if="status === 'loading'">
      <svg class="animate-spin h-10 w-10 text-accent mb-5"
           viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="10" stroke="currentColor"
                stroke-opacity=".2" stroke-width="3"/>
        <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
              stroke-width="3" stroke-linecap="round"/>
      </svg>
      <p class="text-muted text-sm">确认支付结果中…</p>
    </template>

    <!-- Success -->
    <template v-else-if="status === 'success'">
      <p class="text-6xl mb-5">🎉</p>
      <h2 class="text-xl font-bold mb-2">支付成功！</h2>
      <p class="text-muted text-sm mb-6">订单已确认，感谢购买</p>

      <div v-if="orderId"
           class="w-full bg-surface rounded-2xl p-4 mb-6 text-left text-sm space-y-2">
        <div class="flex justify-between">
          <span class="text-muted">订单号</span>
          <span class="font-mono text-xs text-accent break-all text-right max-w-[60%]">
            {{ orderId }}
          </span>
        </div>
        <div class="flex justify-between border-t border-border pt-2">
          <span class="text-muted">状态</span>
          <span class="text-accent font-semibold">✔ 已支付</span>
        </div>
      </div>

      <!-- 分享赚钱 CTA -->
      <button
        v-if="shopId"
        class="w-full h-12 rounded-full font-bold text-sm mb-3
               active:scale-95 transition-transform duration-100"
        style="background:#00FF88;color:#000"
        @click="shareShop"
      >
        📤 分享店铺赚佣金
      </button>

      <button
        class="w-full h-12 rounded-full font-semibold text-sm border border-border
               text-muted active:scale-95 transition-transform duration-100"
        @click="goShop"
      >
        {{ shopId ? '返回店铺' : '返回首页' }}
      </button>
    </template>

    <!-- Fail -->
    <template v-else>
      <p class="text-6xl mb-5">😕</p>
      <h2 class="text-xl font-bold mb-2">支付未完成</h2>
      <p class="text-muted text-sm mb-8">可以返回店铺重新尝试</p>

      <button
        class="w-full h-12 rounded-full font-bold text-sm mb-3
               active:scale-95 transition-transform duration-100"
        style="background:#00FF88;color:#000"
        @click="goShop"
      >
        返回重试
      </button>
      <button
        class="w-full h-12 text-sm text-muted active:opacity-60"
        @click="router.push('/')"
      >
        回到首页
      </button>
    </template>

  </div>
</template>
