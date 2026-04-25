<template>
  <button class="theme-toggle" @click="themeStore.toggle()" :title="themeStore.isDark ? '切换到浅色模式' : '切换到深色模式'">
    <span class="toggle-icon">{{ themeStore.isDark ? '☀️' : '🌙' }}</span>
    <span class="toggle-label">{{ themeStore.isDark ? '浅色' : '深色' }}</span>
  </button>
</template>

<script setup>
import { useThemeStore } from '@/store/index.js'
const themeStore = useThemeStore()
</script>

<style scoped>
.theme-toggle {
  display: flex; align-items: center; gap: 8px;
  padding: 9px 14px;
  background: var(--dp-chip-bg);
  border: 1px solid var(--dp-chip-border);
  border-radius: 999px;
  cursor: pointer;
  color: var(--dp-text-sub);
  font-size: 13px; font-weight: 500;
  transition: all 0.2s;
  width: 100%;
  justify-content: center;
}
.theme-toggle:hover {
  background: var(--dp-accent-bg);
  border-color: rgba(26,188,156,0.35);
  color: #1abc9c;
}
.theme-toggle:active { transform: scale(0.97); }
.toggle-icon { font-size: 16px; }
</style>
