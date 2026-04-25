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

const email      = ref('')
const password   = ref('')
const submitting = ref(false)
const errorMsg   = ref('')

async function login() {
  errorMsg.value = ''
  if (!email.value.trim())    { errorMsg.value = '请填写邮箱'; return }
  if (!password.value.trim()) { errorMsg.value = '请填写密码'; return }

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
  <div class="login-page">

    <!-- Background decoration -->
    <div class="login-bg-orb login-bg-orb-1" />
    <div class="login-bg-orb login-bg-orb-2" />

    <!-- Card -->
    <div class="login-card">

      <!-- Logo -->
      <div class="login-logo-wrap">
        <div class="login-logo-mark">D</div>
        <div class="login-logo-glow" />
      </div>

      <h1 class="login-heading">欢迎回来</h1>
      <p class="login-subheading">登录后查看收益、排名和邀请奖励</p>

      <!-- Form -->
      <div class="login-form">
        <div class="login-field">
          <label class="login-label">邮箱</label>
          <input
            v-model="email"
            type="email"
            placeholder="your@email.com"
            autocomplete="email"
            class="login-input"
            @keydown.enter="login"
          />
        </div>

        <div class="login-field">
          <label class="login-label">密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="••••••••"
            autocomplete="current-password"
            class="login-input"
            @keydown.enter="login"
          />
        </div>

        <!-- Error -->
        <div v-if="errorMsg" class="login-error">
          <span>⚠️</span> {{ errorMsg }}
        </div>

        <!-- Login button -->
        <button
          class="login-btn"
          :disabled="submitting"
          @click="login"
        >
          <span v-if="submitting" class="login-spinner" />
          <span>{{ submitting ? '登录中…' : '登录' }}</span>
        </button>

        <!-- Divider -->
        <div class="login-divider">
          <div class="login-divider-line" />
          <span class="login-divider-text">或</span>
          <div class="login-divider-line" />
        </div>

        <!-- Guest button -->
        <button class="login-guest-btn" @click="continueAsGuest">
          先逛逛，不登录
        </button>
      </div>

      <!-- Footer note -->
      <p class="login-note">
        登录后收益、排名和邀请奖励才能正常记录 💰<br />
        每邀请一位好友下单即得 €2 奖励
      </p>

    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--c-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 20px;
  position: relative;
  overflow: hidden;
}

/* Background orbs */
.login-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.login-bg-orb-1 {
  width: 320px; height: 320px;
  background: rgba(26,188,156,0.12);
  top: -80px; right: -80px;
}
.login-bg-orb-2 {
  width: 280px; height: 280px;
  background: rgba(26,188,156,0.07);
  bottom: -60px; left: -60px;
}

/* Card */
.login-card {
  width: 100%; max-width: 420px;
  background: var(--c-surface);
  border: 1px solid var(--c-card-border);
  border-radius: 24px;
  padding: 36px 28px 28px;
  position: relative;
  z-index: 1;
  box-shadow: 0 8px 40px rgba(0,0,0,0.25);
}

/* Logo */
.login-logo-wrap {
  width: 64px; height: 64px;
  margin: 0 auto 20px;
  position: relative;
}
.login-logo-mark {
  width: 64px; height: 64px; border-radius: 18px;
  background: linear-gradient(135deg, #1abc9c, #0d6e5a);
  display: flex; align-items: center; justify-content: center;
  font-weight: 900; font-size: 28px; color: #fff;
  box-shadow: 0 8px 24px rgba(26,188,156,0.45);
  position: relative; z-index: 1;
}
.login-logo-glow {
  position: absolute; inset: -8px;
  background: radial-gradient(circle, rgba(26,188,156,0.25) 0%, transparent 70%);
  border-radius: 50%;
}

.login-heading {
  font-size: 24px; font-weight: 800;
  color: var(--c-text-bright);
  text-align: center; margin: 0 0 6px;
}
.login-subheading {
  font-size: 13px; color: var(--c-text-sub);
  text-align: center; margin: 0 0 24px;
}

/* Form */
.login-form { display: flex; flex-direction: column; gap: 14px; }

.login-field { display: flex; flex-direction: column; gap: 6px; }
.login-label {
  font-size: 12px; font-weight: 600;
  color: var(--c-text-sub); letter-spacing: 0.3px;
}
.login-input {
  height: 48px; border-radius: 14px;
  padding: 0 16px; font-size: 14px;
  background: var(--c-card);
  border: 1px solid var(--c-card-border);
  color: var(--c-text); outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.login-input::placeholder { color: var(--c-text-muted); }
.login-input:focus {
  border-color: rgba(26,188,156,0.5);
  box-shadow: 0 0 0 3px rgba(26,188,156,0.1);
}

/* Error */
.login-error {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 14px; border-radius: 12px;
  background: rgba(239,68,68,0.1);
  border: 1px solid rgba(239,68,68,0.25);
  color: #ef4444; font-size: 13px;
}

/* Login button */
.login-btn {
  height: 50px; border-radius: 25px;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border: none; color: #fff;
  font-size: 15px; font-weight: 700;
  cursor: pointer; display: flex;
  align-items: center; justify-content: center; gap: 8px;
  box-shadow: 0 4px 16px rgba(26,188,156,0.4);
  transition: all 0.2s;
}
.login-btn:hover { box-shadow: 0 6px 22px rgba(26,188,156,0.5); transform: translateY(-1px); }
.login-btn:active { transform: scale(0.97); }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.login-spinner {
  width: 18px; height: 18px; border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Divider */
.login-divider {
  display: flex; align-items: center; gap: 12px;
}
.login-divider-line { flex: 1; height: 1px; background: var(--c-border); }
.login-divider-text { font-size: 12px; color: var(--c-text-muted); }

/* Guest button */
.login-guest-btn {
  height: 48px; border-radius: 24px;
  background: transparent;
  border: 1px solid var(--c-card-border);
  color: var(--c-text-sub); font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.15s;
}
.login-guest-btn:hover {
  border-color: var(--c-text-sub);
  color: var(--c-text);
}
.login-guest-btn:active { transform: scale(0.97); }

/* Note */
.login-note {
  font-size: 12px; color: var(--c-text-muted);
  text-align: center; margin: 20px 0 0;
  line-height: 1.7;
}
</style>
