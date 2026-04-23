<!--
  PayResult.vue — 支付结果页（支付网关回调后落地）
  路由：/pay/result?result=success&plan=PACK_M

  支付成功 → 展示配额 → 返回Generate
  支付失败 → 重试入口
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQuotaInfo } from '@/api'
import { initUserId } from '@/utils/user'

const route  = useRoute()
const router = useRouter()

const status  = ref('loading')
const quota   = ref(null)
const message = ref('')

const USER_ID = initUserId()

onMounted(async () => {
  const result = route.query.result || 'success'

  if (result === 'success') {
    status.value  = 'success'
    message.value = '解锁成功！快去生成爆款吧 🎉'
    try {
      quota.value = await getQuotaInfo(USER_ID)
    } catch (_) {}
  } else {
    status.value  = 'failed'
    message.value = '支付未完成，请重试'
  }
})
</script>

<template>
  <div class="min-h-screen bg-bg text-white flex flex-col items-center
              justify-center px-6 text-center">

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
      <h2 class="text-xl font-bold mb-2">{{ message }}</h2>

      <!-- 配额卡片 -->
      <div v-if="quota"
           class="w-full max-w-[320px] bg-surface rounded-2xl p-5 my-5 space-y-3">
        <div class="flex justify-between text-sm">
          <span class="text-muted">免费剩余</span>
          <strong>{{ quota.remainFree }} 次</strong>
        </div>
        <div class="flex justify-between text-sm border-t border-border pt-3">
          <span class="text-muted">付费剩余</span>
          <strong>{{ quota.remainPaid }} 次</strong>
        </div>
        <div class="flex justify-between text-sm border-t border-border pt-3">
          <span class="text-muted">合计可用</span>
          <strong class="text-accent text-base">
            {{ (quota.remainFree ?? 0) + (quota.remainPaid ?? 0) }} 次
          </strong>
        </div>
      </div>

      <button
        class="bg-accent text-black font-bold h-12 w-full max-w-[320px]
               rounded-full active:scale-95 transition-transform duration-100 text-sm"
        @click="router.push('/generate')"
      >
        立即生成爆款设计
      </button>
    </template>

    <!-- Failed -->
    <template v-else>
      <p class="text-6xl mb-5">😕</p>
      <h2 class="text-xl font-bold mb-6">{{ message }}</h2>
      <button
        class="bg-accent text-black font-bold h-12 w-full max-w-[320px]
               rounded-full active:scale-95 transition-transform duration-100 text-sm"
        @click="router.push('/generate')"
      >
        返回重试
      </button>
    </template>

  </div>
</template>
