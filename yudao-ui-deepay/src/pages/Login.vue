<!--
  Login.vue — 登录页
  路径：/login

  功能：
  ✔ 邮箱 + 密码登录（MVP）
  ✔ 登录成功后保存 token
  ✔ 绑定已捕获的推荐人 ref（防止丢失，只绑一次）
  ✔ 跳回登录前页面（支持 ?redirect=）
  ✔ 表单校验 + 错误提示
  ✔ 访客模式入口（继续匿名使用）
-->
<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import http from '@/utils/request'
import { setToken, isLoggedIn } from '@/utils/auth'

const router = useRouter()
const route  = useRoute()

// 如果已经登录了，直接跳走
if (isLoggedIn()) {
  router.replace(route.query.redirect || '/me')
}

const email     = ref('')
const password  = ref('')
const submitting = ref(false)
const errorMsg  = ref('')

async function login() {
  errorMsg.value = ''
  if (!email.value.trim())    { errorMsg.value = '请填写邮箱';  return }
  if (!password.value.trim()) { errorMsg.value = '请填写密码';  return }

  submitting.value = true
  try {
    const data = await http.post('/api/auth/login', {
      email:    email.value.trim(),
      password: password.value,
    })

    const token = data?.token
    if (!token) throw new Error('服务器未返回 token，请联系客服')

    setToken(token)

    // 绑定推荐人（登录后立即触发，只绑一次，后端幂等）
    const ref = localStorage.getItem('deepay_ref')
    if (ref) {
      http.post('/api/user/bindRef', { ref }).catch(() => {})
    }

    // 跳回目标页或默认进个人中心
    router.replace(route.query.redirect || '/me')
  } catch (e) {
    errorMsg.value =
      e?.response?.data?.msg ||
      e?.message ||
      '登录失败，请检查邮箱和密码'
  } finally {
    submitting.value = false
  }
}

// 访客模式：直接进首页，不强制登录
function continueAsGuest() {
  router.replace(route.query.redirect || '/')
}
</script>

<template>
  <div class="min-h-screen bg-bg text-white flex flex-col">

    <!-- 顶部 logo -->
    <header class="px-6 pt-12 pb-8 text-center">
      <div class="w-12 h-12 rounded-2xl bg-accent flex items-center justify-center
                  text-black font-black text-xl mx-auto mb-4">D</div>
      <h1 class="text-2xl font-black">欢迎回来</h1>
      <p class="text-muted text-sm mt-1">登录后查看收益、排名和邀请奖励</p>
    </header>

    <div class="flex-1 px-6 max-w-[480px] w-full mx-auto">

      <!-- 表单 -->
      <div class="space-y-4 mb-6">
        <div>
          <label class="text-xs text-muted block mb-1">邮箱</label>
          <input
            v-model="email"
            type="email"
            placeholder="your@email.com"
            autocomplete="email"
            class="w-full h-12 rounded-xl px-4 text-sm bg-surface
                   border border-border text-white placeholder:text-muted
                   focus:outline-none focus:border-accent transition-colors"
            @keydown.enter="login"
          />
        </div>
        <div>
          <label class="text-xs text-muted block mb-1">密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="••••••••"
            autocomplete="current-password"
            class="w-full h-12 rounded-xl px-4 text-sm bg-surface
                   border border-border text-white placeholder:text-muted
                   focus:outline-none focus:border-accent transition-colors"
            @keydown.enter="login"
          />
        </div>

        <!-- 错误提示 -->
        <p v-if="errorMsg" class="text-xs text-danger">{{ errorMsg }}</p>
      </div>

      <!-- 登录按钮 -->
      <button
        :disabled="submitting"
        class="w-full h-12 rounded-full font-bold text-sm mb-4
               active:scale-95 transition-transform duration-100
               disabled:opacity-50 disabled:cursor-not-allowed"
        style="background:#00FF88;color:#000"
        @click="login"
      >
        {{ submitting ? '登录中…' : '登录' }}
      </button>

      <!-- 分割线 -->
      <div class="flex items-center gap-3 mb-4">
        <div class="flex-1 h-px bg-border" />
        <span class="text-muted text-xs">或</span>
        <div class="flex-1 h-px bg-border" />
      </div>

      <!-- 访客入口 -->
      <button
        class="w-full h-12 rounded-full font-semibold text-sm text-muted
               border border-border active:text-white active:border-white
               transition-colors duration-100"
        @click="continueAsGuest"
      >
        先逛逛，不登录
      </button>

      <!-- 温馨提示 -->
      <p class="text-center text-muted text-xs mt-6 leading-relaxed">
        登录后收益、排名和邀请奖励才能正常记录 💰<br>
        每邀请一位好友下单即得 €2 奖励
      </p>

    </div>

  </div>
</template>
