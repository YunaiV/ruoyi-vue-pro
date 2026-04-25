<template>
  <div class="page">
    <!-- Profile Card -->
    <div class="dp-card profile-card dp-fade-up">
      <div class="avatar-circle">{{ userStore.avatarLetter }}</div>
      <div class="profile-info">
        <div class="profile-name">{{ userStore.displayName }}</div>
        <div class="profile-email">{{ userStore.profile?.email || 'guest@deepay.ai' }}</div>
      </div>
      <button class="edit-btn dp-btn-ghost">编辑</button>
    </div>

    <!-- Preferences -->
    <div class="section-group dp-fade-up" style="animation-delay:0.05s">
      <p class="dp-section-title">偏好设置</p>
      <div class="dp-card settings-card">
        <div class="settings-row">
          <div class="row-left">
            <span class="row-icon">🎨</span>
            <span class="row-label">主题</span>
          </div>
          <ThemeToggle />
        </div>
        <div class="row-divider"></div>
        <div class="settings-row">
          <div class="row-left">
            <span class="row-icon">🌐</span>
            <span class="row-label">语言</span>
          </div>
          <span class="row-value">简体中文</span>
        </div>
        <div class="row-divider"></div>
        <div class="settings-row">
          <div class="row-left">
            <span class="row-icon">🔔</span>
            <span class="row-label">消息通知</span>
          </div>
          <div class="toggle-switch" :class="{ on: notifications }" @click="notifications = !notifications">
            <div class="toggle-thumb"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- About -->
    <div class="section-group dp-fade-up" style="animation-delay:0.1s">
      <p class="dp-section-title">关于</p>
      <div class="dp-card settings-card">
        <div class="settings-row clickable" v-for="item in aboutItems" :key="item.label" @click="item.action && item.action()">
          <div class="row-left">
            <span class="row-icon">{{ item.icon }}</span>
            <span class="row-label">{{ item.label }}</span>
          </div>
          <span class="row-arrow">›</span>
        </div>
        <div class="row-divider" v-if="aboutItems.indexOf(item) < aboutItems.length - 1" v-for="item in aboutItems" :key="'d' + item.label"></div>
        <div class="row-divider"></div>
        <div class="settings-row">
          <div class="row-left">
            <span class="row-icon">ℹ️</span>
            <span class="row-label">版本</span>
          </div>
          <span class="row-value">v1.0.0</span>
        </div>
      </div>
    </div>

    <!-- Logout -->
    <div class="section-group dp-fade-up" style="animation-delay:0.15s" v-if="userStore.isLoggedIn">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>

    <div style="height: 20px;"></div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { useUserStore } from '@/store/index.js'

const userStore = useUserStore()
const router = useRouter()
const notifications = ref(true)

const aboutItems = [
  { icon: '💬', label: '联系客服',   action: null },
  { icon: '⭐', label: '给我们评分', action: null },
  { icon: '📤', label: '分享应用',   action: null },
  { icon: '��', label: '隐私政策',   action: null },
  { icon: '📋', label: '服务条款',   action: null },
]

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.page { padding: 16px; max-width: 600px; margin: 0 auto; }
.profile-card {
  display: flex; align-items: center; gap: 16px;
  padding: 20px; margin-bottom: 24px;
}
.avatar-circle {
  width: 56px; height: 56px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 800; color: white;
}
.profile-info { flex: 1; }
.profile-name { font-size: 16px; font-weight: 700; color: var(--dp-text-bright); }
.profile-email { font-size: 13px; color: var(--dp-text-sub); margin-top: 2px; }
.edit-btn { padding: 8px 16px; font-size: 13px; border-radius: 8px; }
.section-group { margin-bottom: 24px; }
.settings-card { padding: 0; overflow: hidden; }
.settings-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; min-height: 50px;
}
.settings-row.clickable { cursor: pointer; }
.settings-row.clickable:hover { background: var(--dp-chip-bg); }
.row-left { display: flex; align-items: center; gap: 12px; }
.row-icon { font-size: 18px; width: 24px; text-align: center; }
.row-label { font-size: 14px; font-weight: 500; color: var(--dp-text); }
.row-value { font-size: 13px; color: var(--dp-text-sub); }
.row-arrow { font-size: 18px; color: var(--dp-text-muted); font-weight: 300; }
.row-divider { height: 1px; background: var(--dp-border); margin: 0 16px; }

/* Toggle switch */
.toggle-switch {
  width: 44px; height: 26px; border-radius: 13px;
  background: var(--dp-chip-bg); border: 1px solid var(--dp-chip-border);
  position: relative; cursor: pointer; transition: all 0.2s;
}
.toggle-switch.on { background: #1abc9c; border-color: #1abc9c; }
.toggle-thumb {
  width: 20px; height: 20px; border-radius: 50%; background: white;
  position: absolute; top: 2px; left: 2px; transition: transform 0.2s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.3);
}
.toggle-switch.on .toggle-thumb { transform: translateX(18px); }

.logout-btn {
  width: 100%; padding: 14px;
  background: rgba(239,68,68,0.1); border: 1px solid rgba(239,68,68,0.25);
  border-radius: 14px; color: #ef4444;
  font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}
.logout-btn:hover { background: rgba(239,68,68,0.2); }
</style>
