<template>
  <nav class="tabbar">
    <router-link
      v-for="tab in tabs"
      :key="tab.path"
      :to="tab.path"
      class="tab-item"
      :class="{ active: isActive(tab.path) }"
    >
      <div class="tab-indicator" v-if="isActive(tab.path)"></div>
      <span class="tab-icon">{{ tab.icon }}</span>
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()

const tabs = [
  { path: '/',              icon: '🏠', label: '首页' },
  { path: '/image-library', icon: '🖼️', label: '图库' },
  { path: '/template-library', icon: '📐', label: '模板' },
  { path: '/ai-sales',      icon: '📊', label: 'AI销售' },
  { path: '/settings',      icon: '⚙️', label: '设置' },
]

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  z-index: 300;
  background: var(--dp-nav-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-top: 1px solid var(--dp-border);
  display: flex;
  padding-bottom: env(safe-area-inset-bottom);
}
.tab-item {
  flex: 1;
  display: flex; flex-direction: column; align-items: center;
  padding: 8px 4px 10px;
  text-decoration: none;
  color: var(--dp-text-muted);
  font-size: 10px; font-weight: 500;
  gap: 3px;
  position: relative;
  transition: color 0.15s;
}
.tab-item.active { color: var(--dp-accent); }
.tab-indicator {
  position: absolute;
  top: 0; left: 50%; transform: translateX(-50%);
  width: 24px; height: 2.5px;
  background: var(--dp-accent);
  border-radius: 0 0 4px 4px;
}
.tab-icon { font-size: 20px; line-height: 1; }
.tab-label { line-height: 1; }
</style>
