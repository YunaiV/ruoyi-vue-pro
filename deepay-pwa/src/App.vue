<template>
  <div class="app-shell" :class="{ 'is-desktop': !isMobile }">
    <!-- Desktop Sidebar -->
    <aside v-if="!isMobile" class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-mark">D</div>
        <span class="logo-text">Deepay</span>
      </div>
      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: $route.path === item.path }"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <ThemeToggle />
        <div class="user-mini" v-if="userStore.isLoggedIn">
          <div class="avatar-sm">{{ userStore.avatarLetter }}</div>
          <span class="user-name-sm">{{ userStore.displayName }}</span>
        </div>
      </div>
    </aside>

    <!-- Mobile Top Navbar -->
    <AppNavBar v-if="isMobile" />

    <!-- Main Content -->
    <main class="main-content" :class="{ 'mobile': isMobile }">
      <router-view v-slot="{ Component, route }">
        <transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>

    <!-- Mobile Bottom Tab Bar -->
    <AppTabBar v-if="isMobile" />

    <!-- Floating AI Button (mobile only) -->
    <button
      v-if="isMobile"
      class="fab-ai"
      @click="openChat"
      aria-label="打开AI助手"
    >
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z" fill="white" opacity="0"/>
        <circle cx="12" cy="12" r="10" stroke="white" stroke-width="1.5" opacity="0.3"/>
        <path d="M8 12h8M12 8l4 4-4 4" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <text x="12" y="14" text-anchor="middle" font-size="11" font-weight="700" fill="white" font-family="Arial">AI</text>
      </svg>
    </button>

    <!-- Desktop AI Button -->
    <button
      v-if="!isMobile"
      class="fab-ai-desktop"
      @click="openChat"
      aria-label="打开AI助手"
    >
      <span style="font-size:20px">✨</span>
      <span>AI助手</span>
    </button>

    <!-- Chat Interface -->
    <ChatInterface
      :isOpen="chatStore.isOpen"
      :isMobile="isMobile"
      @close="chatStore.isOpen = false"
    />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import AppNavBar from '@/components/AppNavBar.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import ChatInterface from '@/components/ChatInterface.vue'
import { useThemeStore } from '@/store/index.js'
import { useUserStore } from '@/store/index.js'
import { useChatStore } from '@/store/index.js'

const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('lg')
const themeStore = useThemeStore()
const userStore = useUserStore()
const chatStore = useChatStore()
const route = useRoute()

const navItems = [
  { path: '/',                icon: '🏠', label: '首页' },
  { path: '/image-library',   icon: '🖼️', label: '图库' },
  { path: '/template-library',icon: '📐', label: '模板库' },
  { path: '/model-library',   icon: '👗', label: '模特库' },
  { path: '/design-library',  icon: '✏️', label: '设计库' },
  { path: '/ai-sales',        icon: '📊', label: 'AI销售' },
  { path: '/settings',        icon: '⚙️', label: '设置' },
]

function openChat() {
  if (!chatStore.activeId) chatStore.newSession()
  chatStore.isOpen = true
}

onMounted(() => {
  themeStore.applyTheme()
})
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--dp-bg);
}
.app-shell.is-desktop {
  display: flex;
}

/* Sidebar */
.sidebar {
  width: 240px;
  min-height: 100vh;
  background: var(--dp-surface);
  border-right: 1px solid var(--dp-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0; top: 0; bottom: 0;
  z-index: 100;
  padding: 24px 16px;
}
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px;
  margin-bottom: 32px;
}
.logo-mark {
  width: 36px; height: 36px;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 900; color: white;
}
.logo-text {
  font-size: 18px; font-weight: 800;
  color: var(--dp-text-bright);
  letter-spacing: -0.03em;
}
.sidebar-nav {
  flex: 1;
  display: flex; flex-direction: column; gap: 4px;
}
.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 11px 12px;
  border-radius: 12px;
  color: var(--dp-text-sub);
  font-size: 14px; font-weight: 500;
  text-decoration: none;
  transition: all 0.15s;
}
.nav-item:hover {
  background: var(--dp-chip-bg);
  color: var(--dp-text);
}
.nav-item.active {
  background: var(--dp-accent-bg);
  color: var(--dp-accent);
  font-weight: 600;
}
.nav-icon { font-size: 18px; width: 24px; text-align: center; }
.sidebar-footer {
  display: flex; flex-direction: column; gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--dp-border);
}
.user-mini {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 12px;
}
.avatar-sm {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: white;
}
.user-name-sm {
  font-size: 13px; font-weight: 500;
  color: var(--dp-text);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

/* Main content */
.main-content {
  flex: 1;
  min-height: 100vh;
}
.is-desktop .main-content {
  margin-left: 240px;
}
.main-content.mobile {
  padding-top: 56px;
  padding-bottom: calc(64px + env(safe-area-inset-bottom));
}

/* FAB */
.fab-ai {
  position: fixed;
  right: 20px;
  bottom: calc(80px + env(safe-area-inset-bottom));
  width: 52px; height: 52px;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border: none; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(26,188,156,0.45);
  z-index: 200;
  transition: all 0.2s;
}
.fab-ai:hover { transform: scale(1.08); box-shadow: 0 6px 28px rgba(26,188,156,0.6); }
.fab-ai:active { transform: scale(0.95); }

.fab-ai-desktop {
  position: fixed;
  right: 24px;
  bottom: 32px;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border: none; border-radius: 999px;
  display: flex; align-items: center; gap: 8px;
  padding: 12px 20px;
  color: white; font-size: 14px; font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(26,188,156,0.45);
  z-index: 200;
  transition: all 0.2s;
}
.fab-ai-desktop:hover { transform: translateY(-2px); box-shadow: 0 6px 28px rgba(26,188,156,0.6); }
.fab-ai-desktop:active { transform: scale(0.97); }

/* Page transitions */
.page-enter-active, .page-leave-active { transition: all 0.2s ease; }
.page-enter-from { opacity: 0; transform: translateY(8px); }
.page-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
