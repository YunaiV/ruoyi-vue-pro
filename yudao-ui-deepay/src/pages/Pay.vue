<!--
  Pay.vue — 支付结果页
  路径：/pay/:id
  支付网关回调后跳回此页，读 ?result=success&plan=PACK_M
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
    message.value = '支付成功！快去生成爆款吧 🎉'
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
              justify-center px-6 py-16 max-w-[480px] mx-auto text-center">

    <!-- Loading -->
    <template v-if="status === 'loading'">
      <div class="w-10 h-10 border-2 border-accent border-t-transparent
                  rounded-full animate-spin mb-4"></div>
      <p class="text-muted text-sm">确认支付结果中…</p>
    </template>

    <!-- Success -->
    <template v-else-if="status === 'success'">
      <div class="text-5xl mb-5">🎉</div>
      <h2 class="text-xl font-bold mb-2">{{ message }}</h2>

      <div v-if="quota" class="w-full card p-4 mb-6 text-left space-y-2">
        <div class="flex justify-between text-sm">
          <span class="text-muted">免费剩余</span>
          <strong class="text-accent">{{ quota.remainFree }} 次</strong>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-muted">付费剩余</span>
          <strong class="text-accent">{{ quota.remainPaid }} 次</strong>
        </div>
        <div class="flex justify-between text-sm border-t border-border pt-2">
          <span class="text-muted font-semibold">合计可用</span>
          <strong class="text-accent text-base">{{ quota.totalRemain }} 次</strong>
        </div>
      </div>

      <button class="btn-primary" @click="router.push('/')">
        立即生成爆款设计
      </button>
    </template>

    <!-- Failed -->
    <template v-else>
      <div class="text-5xl mb-5">😕</div>
      <h2 class="text-xl font-bold mb-6">{{ message }}</h2>
      <button class="btn-primary" @click="router.push('/')">返回重试</button>
    </template>

  </div>
</template>
