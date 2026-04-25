<template>
  <div class="gpt-shell" :class="{ 'dark-mode': themeStore.isDark, 'sidebar-collapsed': sidebarCollapsed }">

    <!-- ════ LEFT SIDEBAR ════ -->
    <aside class="gpt-sidebar" :class="{ 'mobile-open': mobileSidebarOpen }">

      <!-- Top: Logo + collapse btn -->
      <div class="sb-top">
        <div class="sb-logo">
          <div class="logo-icon">D</div>
          <span class="logo-name" v-show="!sidebarCollapsed">Deepay</span>
        </div>
        <div class="sb-top-actions" v-show="!sidebarCollapsed">
          <!-- New Chat -->
          <button class="icon-action" title="新对话" @click="newChat">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
              <path d="M12 5v14M5 12h14"/>
            </svg>
          </button>
          <!-- Collapse -->
          <button class="icon-action" title="收起" @click="sidebarCollapsed = true" style="display:none">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 19l-7-7 7-7"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Nav Items -->
      <nav class="sb-nav">
        <button class="sb-nav-item" @click="newChat">
          <svg class="sb-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          <span>新聊天</span>
        </button>
        <button class="sb-nav-item" @click="searchFocus">
          <svg class="sb-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <span>搜索对话</span>
        </button>
        <button class="sb-nav-item" @click="router.push('/template-library')">
          <svg class="sb-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          <span>模板库</span>
        </button>
        <button class="sb-nav-item" @click="router.push('/image-library')">
          <svg class="sb-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>
          <span>图库</span>
        </button>
        <button class="sb-nav-item" @click="router.push('/ai-sales')">
          <svg class="sb-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2 5h12M9 21a1 1 0 100-2 1 1 0 000 2zM20 21a1 1 0 100-2 1 1 0 000 2z"/></svg>
          <span>AI 销售</span>
        </button>
        <button class="sb-nav-item sb-more">
          <svg class="sb-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/></svg>
          <span>更多</span>
        </button>
      </nav>

      <!-- Divider -->
      <div class="sb-divider"></div>

      <!-- Search Bar (compact) -->
      <div class="sb-search" v-show="!sidebarCollapsed">
        <input
          ref="searchInput"
          v-model="searchQuery"
          type="text"
          placeholder="搜索对话..."
          class="sb-search-input"
        />
      </div>

      <!-- Recent Sessions -->
      <div class="sb-section-label" v-show="!sidebarCollapsed">最近</div>
      <div class="sb-recent" v-show="!sidebarCollapsed">
        <div
          v-for="session in filteredSessions"
          :key="session.id"
          class="sb-recent-item"
          :class="{ active: chatStore.activeId === session.id }"
          @click="switchSession(session.id)"
        >
          <span class="sb-recent-text">{{ session.title }}</span>
          <button
            class="sb-delete"
            @click.stop="chatStore.deleteSession(session.id)"
            title="删除"
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div v-if="filteredSessions.length === 0" class="sb-empty">
          暂无对话记录
        </div>
      </div>

      <!-- Spacer -->
      <div style="flex:1"></div>

      <!-- Bottom: User + Theme -->
      <div class="sb-bottom">
        <button class="sb-theme-btn" @click="themeStore.toggle()" title="切换主题">
          <svg v-if="themeStore.isDark" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1111.21 3a7 7 0 009.79 9.79z"/></svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
          <span v-show="!sidebarCollapsed">{{ themeStore.isDark ? '深色' : '浅色' }}</span>
        </button>

        <div class="sb-user" @click="router.push('/settings')">
          <div class="user-avatar">{{ userStore.avatarLetter }}</div>
          <div class="user-info" v-show="!sidebarCollapsed">
            <span class="user-name">{{ userStore.displayName }}</span>
            <span class="user-plan">Free Plan</span>
          </div>
          <svg v-show="!sidebarCollapsed" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </div>
      </div>
    </aside>

    <!-- Mobile sidebar backdrop -->
    <div v-if="mobileSidebarOpen" class="sb-backdrop" @click="mobileSidebarOpen = false"></div>

    <!-- ════ MAIN CONTENT ════ -->
    <div class="gpt-main">

      <!-- Mobile Top Bar -->
      <header class="mobile-topbar" v-if="isMobile">
        <button class="icon-action" @click="mobileSidebarOpen = true">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <span class="mobile-title">Deepay</span>
        <button class="icon-action" @click="newChat">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
        </button>
      </header>

      <!-- Page Content (router-view) -->
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
import { useRouter } from 'vue-router'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { useThemeStore, useUserStore, useChatStore } from '@/store/index.js'

const router = useRouter()
const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('lg')

const themeStore = useThemeStore()
const userStore = useUserStore()
const chatStore = useChatStore()

const sidebarCollapsed = ref(false)
const mobileSidebarOpen = ref(false)
const searchQuery = ref('')
const searchInput = ref(null)

const filteredSessions = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  if (!q) return chatStore.sessions.slice(0, 20)
  return chatStore.sessions.filter(s => s.title.toLowerCase().includes(q)).slice(0, 20)
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

function searchFocus() {
  searchInput.value?.focus()
}

onMounted(() => {
  themeStore.applyTheme()
})
</script>

<style scoped>
/* ─── Shell ─────────────────────────────────── */
.gpt-shell {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: var(--gpt-main, #212121);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", system-ui, sans-serif;
}

/* ─── Sidebar ────────────────────────────────── */
.gpt-sidebar {
  width: 260px;
  min-width: 260px;
  height: 100vh;
  background: var(--gpt-sidebar, #171717);
  display: flex;
  flex-direction: column;
  padding: 12px 8px;
  gap: 2px;
  overflow: hidden;
  flex-shrink: 0;
  border-right: 1px solid var(--gpt-border);
  transition: width 0.2s ease;
}

.sidebar-collapsed .gpt-sidebar {
  width: 64px;
  min-width: 64px;
}

/* Top */
.sb-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 8px 4px;
  margin-bottom: 4px;
}
.sb-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-icon {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 900; color: white;
  flex-shrink: 0;
}
.logo-name {
  font-size: 17px; font-weight: 700;
  color: var(--gpt-text, #ececec);
  letter-spacing: -0.02em;
}
.sb-top-actions {
  display: flex; gap: 4px;
}
.icon-action {
  width: 34px; height: 34px;
  border: none; background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub, #8e8ea0);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.icon-action:hover {
  background: var(--gpt-sidebar-hover, #212121);
  color: var(--gpt-text, #ececec);
}

/* Nav */
.sb-nav {
  display: flex; flex-direction: column; gap: 1px;
  padding: 0 0 4px;
}
.sb-nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px;
  border: none; background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub, #8e8ea0);
  font-size: 14px; font-weight: 500;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s;
  white-space: nowrap;
}
.sb-nav-item:hover {
  background: var(--gpt-sidebar-hover, #212121);
  color: var(--gpt-text, #ececec);
}
.sb-nav-item.sb-more { color: var(--gpt-text-muted); }
.sb-icon { flex-shrink: 0; }

/* Divider */
.sb-divider {
  height: 1px;
  background: var(--gpt-border);
  margin: 6px 0;
}

/* Search */
.sb-search { padding: 4px 4px 8px; }
.sb-search-input {
  width: 100%;
  background: var(--gpt-sidebar-hover, #212121);
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
  color: var(--gpt-text, #ececec);
  outline: none;
  transition: border-color 0.15s;
}
.sb-search-input::placeholder { color: var(--gpt-text-muted, #555); }
.sb-search-input:focus { border-color: #10a37f; }

/* Section label */
.sb-section-label {
  font-size: 11px; font-weight: 600;
  text-transform: uppercase; letter-spacing: 0.06em;
  color: var(--gpt-text-muted, #555);
  padding: 4px 12px 6px;
}

/* Recent items */
.sb-recent {
  flex: 1;
  overflow-y: auto;
  display: flex; flex-direction: column; gap: 1px;
  scrollbar-width: none;
}
.sb-recent::-webkit-scrollbar { display: none; }

.sb-recent-item {
  display: flex; align-items: center;
  padding: 9px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  gap: 8px;
}
.sb-recent-item:hover {
  background: var(--gpt-sidebar-hover, #212121);
}
.sb-recent-item.active {
  background: var(--gpt-sidebar-active, #2a2a2a);
}
.sb-recent-item:hover .sb-delete { opacity: 1; }
.sb-recent-text {
  flex: 1;
  font-size: 13.5px;
  color: var(--gpt-text-sub, #8e8ea0);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.sb-recent-item.active .sb-recent-text { color: var(--gpt-text, #ececec); }
.sb-delete {
  opacity: 0; flex-shrink: 0;
  width: 20px; height: 20px;
  border: none; background: transparent;
  color: var(--gpt-text-muted); cursor: pointer;
  border-radius: 4px; padding: 0;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.sb-delete:hover { color: var(--dp-danger); }
.sb-empty {
  font-size: 12px; color: var(--gpt-text-muted);
  text-align: center; padding: 20px 0;
}

/* Bottom */
.sb-bottom {
  display: flex; flex-direction: column; gap: 4px;
  padding-top: 8px;
  border-top: 1px solid var(--gpt-border);
}
.sb-theme-btn {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 12px;
  border: none; background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub, #8e8ea0);
  font-size: 13px; cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.sb-theme-btn:hover {
  background: var(--gpt-sidebar-hover);
  color: var(--gpt-text);
}
.sb-user {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}
.sb-user:hover { background: var(--gpt-sidebar-hover); }
.user-avatar {
  width: 32px; height: 32px; flex-shrink: 0;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: white;
}
.user-info {
  flex: 1; display: flex; flex-direction: column; overflow: hidden;
}
.user-name {
  font-size: 13px; font-weight: 600;
  color: var(--gpt-text, #ececec);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.user-plan {
  font-size: 11px; color: var(--gpt-text-muted);
}

/* Backdrop */
.sb-backdrop {
  position: fixed; inset: 0; z-index: 98;
  background: rgba(0,0,0,0.5);
}

/* ─── Main ───────────────────────────────────── */
.gpt-main {
  flex: 1;
  display: flex; flex-direction: column;
  overflow: hidden;
  background: var(--gpt-main, #212121);
}

.mobile-topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 16px;
  background: var(--gpt-main);
  border-bottom: 1px solid var(--gpt-border);
  flex-shrink: 0;
}
.mobile-title {
  font-size: 16px; font-weight: 700;
  color: var(--gpt-text);
}

.gpt-page {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}

/* Mobile sidebar slide-in */
@media (max-width: 1023px) {
  .gpt-sidebar {
    position: fixed;
    left: -100%; top: 0; bottom: 0;
    z-index: 99;
    transition: left 0.25s ease;
  }
  .gpt-sidebar.mobile-open {
    left: 0;
  }
  .gpt-main {
    width: 100%;
  }
}

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.18s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
