<template>
  <div class="gpt-shell" :class="{ 'theme-light': !themeStore.isDark }">

    <!-- ════ LEFT SIDEBAR ════ -->
    <aside class="gpt-sidebar" :class="{ open: mobileSidebarOpen }">

      <!-- Logo row -->
      <div class="sb-head">
        <div class="sb-logo">
          <div class="logo-mark">D</div>
          <span class="logo-text">Deepay</span>
        </div>
        <button class="sb-icon-btn" @click="newChat" title="新聊天">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
        </button>
      </div>

      <!-- Nav Items -->
      <nav class="sb-nav">
        <button class="sb-nav-btn" :class="{ active: currentRoute === '/' }" @click="newChat">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          新聊天
        </button>
        <button class="sb-nav-btn" @click="searchFocus">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          搜索对话
        </button>
        <button class="sb-nav-btn" :class="{ active: currentRoute === '/template-library' }" @click="navTo('/template-library')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          模板库
        </button>
        <button class="sb-nav-btn" :class="{ active: currentRoute === '/image-library' }" @click="navTo('/image-library')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
          图库
        </button>
        <button class="sb-nav-btn" :class="{ active: currentRoute === '/model-library' }" @click="navTo('/model-library')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          模特库
        </button>
        <button class="sb-nav-btn" :class="{ active: currentRoute === '/ai-sales' }" @click="navTo('/ai-sales')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          AI 开店
        </button>
      </nav>

      <div class="sb-divider"></div>

      <!-- Search bar -->
      <div class="sb-search-wrap">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" class="sb-search-icon"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
        <input ref="searchRef" v-model="searchQ" class="sb-search" placeholder="搜索对话..." />
      </div>

      <!-- Recent label -->
      <div class="sb-label">最近</div>

      <!-- Sessions list -->
      <div class="sb-sessions">
        <div
          v-for="s in filteredSessions"
          :key="s.id"
          class="sb-session"
          :class="{ active: chatStore.activeId === s.id }"
          @click="switchSession(s.id)"
        >
          <span class="sb-session-title">{{ s.title }}</span>
          <button class="sb-session-del" @click.stop="chatStore.deleteSession(s.id)" title="删除">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div v-if="!filteredSessions.length" class="sb-empty">暂无对话</div>
      </div>

      <div style="flex:1"></div>

      <!-- Bottom -->
      <div class="sb-bottom">
        <button class="sb-bottom-btn" @click="themeStore.toggle()">
          <svg v-if="themeStore.isDark" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1111.21 3a7 7 0 009.79 9.79z"/></svg>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/></svg>
          {{ themeStore.isDark ? '深色模式' : '浅色模式' }}
        </button>
        <div class="sb-user" @click="navTo('/settings')">
          <div class="sb-avatar">{{ userStore.avatarLetter }}</div>
          <div class="sb-user-info">
            <span class="sb-user-name">{{ userStore.displayName }}</span>
            <span class="sb-user-plan">Free Plan</span>
          </div>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;color:var(--gpt-text-muted)"><path d="M9 18l6-6-6-6"/></svg>
        </div>
      </div>
    </aside>

    <!-- Backdrop -->
    <div v-if="mobileSidebarOpen" class="sb-backdrop" @click="mobileSidebarOpen = false"></div>

    <!-- ════ MAIN CONTENT ════ -->
    <div class="gpt-main">

      <!-- Mobile Top Bar -->
      <header v-if="isMobile" class="mobile-bar">
        <button class="sb-icon-btn" @click="mobileSidebarOpen = true">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <span class="mobile-title">Deepay</span>
        <button class="sb-icon-btn" @click="newChat">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
        </button>
      </header>

      <!-- Router view -->
      <div class="gpt-page">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { useThemeStore, useUserStore, useChatStore } from '@/store/index.js'

const router = useRouter()
const route = useRoute()
const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('lg')

const themeStore = useThemeStore()
const userStore = useUserStore()
const chatStore = useChatStore()

const mobileSidebarOpen = ref(false)
const searchQ = ref('')
const searchRef = ref(null)

const currentRoute = computed(() => route.path)

const filteredSessions = computed(() => {
  const q = searchQ.value.toLowerCase().trim()
  const list = q
    ? chatStore.sessions.filter(s => s.title.toLowerCase().includes(q))
    : chatStore.sessions
  return list.slice(0, 20)
})

function newChat() {
  chatStore.newSession()
  router.push('/')
  mobileSidebarOpen.value = false
}

function switchSession(id) {
  chatStore.activeId = id
  router.push('/')
  mobileSidebarOpen.value = false
}

function navTo(path) {
  router.push(path)
  mobileSidebarOpen.value = false
}

function searchFocus() {
  searchRef.value?.focus()
}

onMounted(() => {
  themeStore.applyTheme()
})
</script>

<style scoped>
/* ─── Shell ──────────────────────────────────── */
.gpt-shell {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: var(--gpt-main);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", system-ui, sans-serif;
  color: var(--gpt-text);
}

/* ─── Sidebar ────────────────────────────────── */
.gpt-sidebar {
  width: 260px;
  min-width: 260px;
  height: 100vh;
  background: var(--gpt-sidebar);
  display: flex;
  flex-direction: column;
  padding: 12px 8px;
  gap: 2px;
  overflow: hidden;
  flex-shrink: 0;
  border-right: 1px solid var(--gpt-border);
}

.sb-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px 10px;
}

.sb-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-mark {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 900;
  color: white;
  flex-shrink: 0;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  color: var(--gpt-text);
  letter-spacing: -0.02em;
}

.sb-icon-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
  flex-shrink: 0;
}
.sb-icon-btn:hover {
  background: var(--gpt-sidebar-hover);
  color: var(--gpt-text);
}

/* Nav */
.sb-nav {
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding-bottom: 4px;
}

.sb-nav-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub);
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  width: 100%;
}
.sb-nav-btn:hover {
  background: var(--gpt-sidebar-hover);
  color: var(--gpt-text);
}
.sb-nav-btn.active {
  background: var(--gpt-sidebar-active);
  color: var(--gpt-text);
}

/* Divider */
.sb-divider {
  height: 1px;
  background: var(--gpt-border);
  margin: 4px 0 8px;
}

/* Search */
.sb-search-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--gpt-sidebar-hover);
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  padding: 7px 10px;
  margin: 0 2px 8px;
}
.sb-search-icon {
  color: var(--gpt-text-muted);
  flex-shrink: 0;
}
.sb-search {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-size: 13px;
  color: var(--gpt-text);
  min-width: 0;
}
.sb-search::placeholder {
  color: var(--gpt-text-muted);
}

/* Label */
.sb-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--gpt-text-muted);
  padding: 2px 12px 6px;
}

/* Sessions */
.sb-sessions {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1px;
  scrollbar-width: none;
}
.sb-sessions::-webkit-scrollbar { display: none; }

.sb-session {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  gap: 6px;
}
.sb-session:hover { background: var(--gpt-sidebar-hover); }
.sb-session.active { background: var(--gpt-sidebar-active); }
.sb-session:hover .sb-session-del { opacity: 1; }

.sb-session-title {
  flex: 1;
  font-size: 13px;
  color: var(--gpt-text-sub);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sb-session.active .sb-session-title { color: var(--gpt-text); }

.sb-session-del {
  opacity: 0;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: var(--gpt-text-muted);
  cursor: pointer;
  border-radius: 4px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.15s, color 0.15s;
}
.sb-session-del:hover { color: #ef4444; }

.sb-empty {
  font-size: 12px;
  color: var(--gpt-text-muted);
  text-align: center;
  padding: 20px 0;
}

/* Bottom */
.sb-bottom {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding-top: 8px;
  border-top: 1px solid var(--gpt-border);
}

.sb-bottom-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  width: 100%;
  text-align: left;
}
.sb-bottom-btn:hover {
  background: var(--gpt-sidebar-hover);
  color: var(--gpt-text);
}

.sb-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.sb-user:hover { background: var(--gpt-sidebar-hover); }

.sb-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: white;
}

.sb-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sb-user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--gpt-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sb-user-plan {
  font-size: 11px;
  color: var(--gpt-text-muted);
}

/* Backdrop */
.sb-backdrop {
  position: fixed;
  inset: 0;
  z-index: 98;
  background: rgba(0, 0, 0, 0.5);
}

/* ─── Main ───────────────────────────────────── */
.gpt-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--gpt-main);
}

.mobile-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: var(--gpt-main);
  border-bottom: 1px solid var(--gpt-border);
  flex-shrink: 0;
}
.mobile-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--gpt-text);
}

.gpt-page {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--gpt-border) transparent;
}

/* Mobile: sidebar as drawer */
@media (max-width: 1023px) {
  .gpt-sidebar {
    position: fixed;
    left: -280px;
    top: 0;
    bottom: 0;
    z-index: 99;
    transition: left 0.25s ease;
    box-shadow: 4px 0 24px rgba(0, 0, 0, 0.4);
  }
  .gpt-sidebar.open { left: 0; }
  .gpt-main { width: 100%; }
}

/* Transitions */
.fade-enter-active,
.fade-leave-active { transition: opacity 0.18s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
