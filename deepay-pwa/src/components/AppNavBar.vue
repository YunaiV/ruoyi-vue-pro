<template>
  <header class="navbar">
    <div class="navbar-inner">
      <div class="navbar-left">
        <button v-if="canGoBack" class="back-btn" @click="router.back()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M15 18l-6-6 6-6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div v-else class="logo-mark-sm">D</div>
      </div>
      <div class="navbar-center">
        <span class="page-title">{{ route.meta.title || 'Deepay' }}</span>
      </div>
      <div class="navbar-right">
        <button class="theme-btn" @click="themeStore.toggle()" :title="themeStore.isDark ? '浅色' : '深色'">
          {{ themeStore.isDark ? '☀️' : '🌙' }}
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/store/index.js'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const canGoBack = computed(() => route.path !== '/')
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0; left: 0; right: 0;
  z-index: 300;
  padding-top: env(safe-area-inset-top);
  background: var(--dp-header-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--dp-border);
}
.navbar-inner {
  display: flex; align-items: center; justify-content: space-between;
  height: 56px; padding: 0 16px;
}
.navbar-left, .navbar-right {
  width: 64px; display: flex; align-items: center;
}
.navbar-right { justify-content: flex-end; }
.navbar-center { flex: 1; text-align: center; }
.page-title {
  font-size: 16px; font-weight: 700;
  color: var(--dp-text-bright);
  letter-spacing: -0.01em;
}
.back-btn, .theme-btn {
  background: none; border: none;
  color: var(--dp-text); cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s;
  font-size: 18px;
}
.back-btn:hover, .theme-btn:hover { background: var(--dp-chip-bg); }
.logo-mark-sm {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 900; color: white;
}
</style>
