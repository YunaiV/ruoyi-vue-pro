<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

defineProps({ currentPath: { type: String, default: '/' } })

const TABS = [
  { path: '/',            icon: '🏠', label: '首页'   },
  { path: '/generate',    icon: '✨', label: 'AI生成' },
  { path: '/template',    icon: '🏪', label: '模板'   },
  { path: '/leaderboard', icon: '🏆', label: '排行'   },
  { path: '/me',          icon: '👤', label: '我的'   },
]

function go(path) { router.push(path) }
</script>

<template>
  <nav class="app-tabbar">
    <button
      v-for="tab in TABS"
      :key="tab.path"
      class="tabbar-item"
      :class="{ active: currentPath === tab.path }"
      @click="go(tab.path)"
    >
      <span class="tabbar-icon">{{ tab.icon }}</span>
      <span class="tabbar-label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<style scoped>
.app-tabbar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 100;
  height: calc(56px + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: var(--c-nav-bg);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-top: 1px solid var(--c-border);
  display: flex; align-items: flex-start; justify-content: space-around;
  padding-top: 6px;
}
.tabbar-item {
  display: flex; flex-direction: column; align-items: center; gap: 3px;
  padding: 4px 12px; border-radius: 12px;
  background: transparent; border: none; cursor: pointer;
  color: var(--c-text-muted); font-size: 10px; font-weight: 600;
  transition: color 0.15s; flex: 1; position: relative;
}
.tabbar-item.active { color: #1abc9c; }
.tabbar-item.active::before {
  content: ''; position: absolute; top: -6px; left: 50%;
  transform: translateX(-50%);
  width: 24px; height: 3px;
  background: #1abc9c; border-radius: 0 0 3px 3px;
}
.tabbar-icon  { font-size: 22px; line-height: 1; }
.tabbar-label { font-size: 10px; letter-spacing: 0.2px; }
</style>
