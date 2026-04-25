<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { useThemeStore } from '@/store/modules/theme'
import AIChatDrawer from '@/components/AIChatDrawer.vue'
import AppNavBar from '@/components/AppNavBar.vue'
import AppTabBar from '@/components/AppTabBar.vue'

const route  = useRoute()
const router = useRouter()
const theme  = useThemeStore()

// Sidebar navigation items
const NAV_ITEMS = [
  { path: '/',            icon: '🏠', label: '首页'     },
  { path: '/generate',    icon: '✨', label: 'AI生成'   },
  { path: '/inspiration', icon: '🎭', label: '灵感库'   },
  { path: '/template',    icon: '🏪', label: '模板'     },
  { path: '/ai/design',   icon: '🎯', label: 'AI出款'   },
  { path: '/ai/season',   icon: '🌿', label: '整季系列' },
  { path: '/redesign',    icon: '🔧', label: '改款工具' },
  { path: '/leaderboard', icon: '🏆', label: '排行榜'   },
  { path: '/me',          icon: '👤', label: '我的'     },
]

// Responsive breakpoints
const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile    = breakpoints.smaller('md')
const isDesktop   = breakpoints.greaterOrEqual('md')

// AI chat drawer state
const showAIChat  = ref(false)
const isRecording = ref(false)

// Pages that hide the bottom nav / float button
const NO_BOTTOM_NAV = ['admin', 'login', 'landing']
const NO_FLOAT_BTN  = ['login', 'landing', 'ai-chat']

const showBottomNav = computed(() => isMobile.value && !NO_BOTTOM_NAV.some(p => route.path.startsWith('/' + p)))
const showFloatBtn  = computed(() => !NO_FLOAT_BTN.some(p => route.path.startsWith('/' + p)))

// Current page title
const PAGE_TITLES = {
  '/':            'Deepay',
  '/generate':    'AI生成',
  '/template':    '模板',
  '/leaderboard': '排行榜',
  '/me':          '我的',
  '/inspiration': '灵感库',
}
const pageTitle = computed(() => PAGE_TITLES[route.path] || 'Deepay')

// Show back button (not on root pages)
const ROOT_PAGES  = ['/', '/me', '/leaderboard', '/template']
const showBackBtn = computed(() => !ROOT_PAGES.includes(route.path))

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/')
}

function toggleAI() {
  showAIChat.value = !showAIChat.value
}

// Sync theme-color meta tag with theme
watch(() => theme.isDark, (dark) => {
  const meta = document.getElementById('theme-color-meta')
  if (meta) meta.content = dark ? '#0a0a0a' : '#f2f2f7'
}, { immediate: true })
</script>

<template>
  <div
    id="app-root"
    :class="[isMobile ? 'layout-mobile' : 'layout-desktop']"
  >
    <!-- ── Desktop Sidebar ──────────────────────────── -->
    <aside v-if="isDesktop" class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-mark">D</div>
        <span class="logo-name">Deepay</span>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in NAV_ITEMS"
          :key="item.path"
          :to="item.path"
          class="sidebar-item"
          :class="{ active: route.path === item.path || route.path.startsWith(item.path + '/') }"
        >
          <span class="sidebar-item-icon">{{ item.icon }}</span>
          <span class="sidebar-item-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="sidebar-theme-btn" @click="theme.toggle()">
          <span>{{ theme.isDark ? '☀️' : '🌙' }}</span>
          <span>{{ theme.isDark ? '浅色模式' : '深色模式' }}</span>
        </button>
      </div>
    </aside>

    <!-- ── Main content column ──────────────────────── -->
    <div class="main-col">

      <!-- Mobile top navbar -->
      <AppNavBar
        v-if="isMobile"
        :title="pageTitle"
        :show-back="showBackBtn"
        @back="goBack"
        @toggle-theme="theme.toggle()"
        :is-dark="theme.isDark"
      />

      <!-- Page content -->
      <main class="page-content" :class="{ 'has-bottom-nav': showBottomNav }">
        <router-view v-slot="{ Component }">
          <keep-alive :include="['Home', 'Template', 'Leaderboard']">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </main>

      <!-- Mobile bottom tabbar -->
      <AppTabBar
        v-if="showBottomNav"
        :current-path="route.path"
      />

    </div><!-- /main-col -->

    <!-- ── AI Float Button ──────────────────────────── -->
    <button
      v-if="showFloatBtn"
      class="ai-float-btn"
      :class="{ recording: isRecording, 'is-open': showAIChat }"
      :style="{ bottom: showBottomNav ? 'calc(64px + env(safe-area-inset-bottom) + 16px)' : '24px' }"
      @click="toggleAI"
    >
      <span class="ai-float-icon">{{ showAIChat ? '✕' : '✨' }}</span>
      <span v-if="isRecording" class="recording-dot" />
    </button>

    <!-- ── AI Chat Drawer ────────────────────────────── -->
    <AIChatDrawer
      v-model:open="showAIChat"
      :is-mobile="isMobile"
    />

  </div>
</template>

<style>
/* ── CSS design tokens — overridden per theme via [data-theme] ── */
:root, [data-theme="dark"] {
  --c-bg:           #0a0a0a;
  --c-surface:      #141414;
  --c-surface2:     #1e1e1e;
  --c-card:         rgba(255,255,255,0.035);
  --c-card-border:  rgba(255,255,255,0.08);
  --c-text:         #e8e8e8;
  --c-text-bright:  #ffffff;
  --c-text-sub:     #999;
  --c-text-muted:   #555;
  --c-accent:       #1abc9c;
  --c-accent-hover: #16a085;
  --c-accent-bg:    rgba(26,188,156,0.12);
  --c-border:       rgba(255,255,255,0.07);
  --c-border2:      rgba(255,255,255,0.12);
  --c-nav-bg:       rgba(10,10,10,0.95);
  --c-header-bg:    rgba(10,10,10,0.88);
  --c-input-bg:     rgba(20,20,20,0.98);
  --c-chip-bg:      rgba(255,255,255,0.06);
  --c-chip-border:  rgba(255,255,255,0.1);
  --c-shadow:       0 4px 24px rgba(0,0,0,0.5);
  --c-shadow-lg:    0 8px 40px rgba(0,0,0,0.7);
  --c-danger:       #ef4444;
  /* legacy */
  --bg-main:   #0a0a0a;
  --bg-card:   #1a1a1a;
  --accent:    #1abc9c;
  --accent-dk: #16a085;
  --border:    rgba(255,255,255,0.07);
}

[data-theme="light"] {
  --c-bg:           #f2f2f7;
  --c-surface:      #ffffff;
  --c-surface2:     #f8f8f8;
  --c-card:         rgba(255,255,255,0.9);
  --c-card-border:  rgba(0,0,0,0.08);
  --c-text:         #1a1a1a;
  --c-text-bright:  #000000;
  --c-text-sub:     #6e6e73;
  --c-text-muted:   #ababab;
  --c-accent:       #1abc9c;
  --c-accent-hover: #16a085;
  --c-accent-bg:    rgba(26,188,156,0.1);
  --c-border:       rgba(0,0,0,0.08);
  --c-border2:      rgba(0,0,0,0.14);
  --c-nav-bg:       rgba(242,242,247,0.95);
  --c-header-bg:    rgba(242,242,247,0.92);
  --c-input-bg:     rgba(255,255,255,0.98);
  --c-chip-bg:      rgba(0,0,0,0.05);
  --c-chip-border:  rgba(0,0,0,0.1);
  --c-shadow:       0 2px 16px rgba(0,0,0,0.08);
  --c-shadow-lg:    0 8px 32px rgba(0,0,0,0.12);
  --c-danger:       #ef4444;
  /* legacy */
  --bg-main:   #f2f2f7;
  --bg-card:   #ffffff;
  --accent:    #1abc9c;
  --accent-dk: #16a085;
  --border:    rgba(0,0,0,0.08);
}

/* ── App root ──────────────────────────────────────────── */
#app-root {
  min-height: 100vh;
  background: var(--c-bg);
  color: var(--c-text);
  transition: background 0.25s ease, color 0.25s ease;
}

/* ── Desktop layout: sidebar + main ───────────────────── */
.layout-desktop {
  display: flex;
  flex-direction: row;
}

.sidebar {
  width: 240px;
  min-height: 100vh;
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--c-surface);
  border-right: 1px solid var(--c-card-border);
  padding: 24px 12px;
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  margin-bottom: 32px;
}
.logo-mark {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1abc9c, #0d6e5a);
  display: flex; align-items: center; justify-content: center;
  font-weight: 900; font-size: 16px; color: #fff;
  box-shadow: 0 4px 12px rgba(26,188,156,0.4);
  flex-shrink: 0;
}
.logo-name {
  font-weight: 700; font-size: 17px;
  color: var(--c-text-bright);
  letter-spacing: 0.3px;
}

.sidebar-nav { flex: 1; display: flex; flex-direction: column; gap: 4px; }

.sidebar-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px; border-radius: 10px;
  font-size: 14px; font-weight: 500;
  color: var(--c-text-sub);
  text-decoration: none;
  transition: all 0.15s;
}
.sidebar-item:hover {
  background: var(--c-card);
  color: var(--c-text);
}
.sidebar-item.active {
  background: rgba(26,188,156,0.1);
  color: #1abc9c;
  font-weight: 600;
}
.sidebar-item-icon { font-size: 18px; width: 22px; text-align: center; }
.sidebar-item-label { font-size: 14px; }

.sidebar-footer { padding-top: 16px; border-top: 1px solid var(--c-border); }
.sidebar-theme-btn {
  display: flex; align-items: center; gap: 10px;
  width: 100%; padding: 10px 12px; border-radius: 10px;
  background: transparent; border: none; cursor: pointer;
  font-size: 14px; color: var(--c-text-sub);
  transition: all 0.15s;
}
.sidebar-theme-btn:hover { background: var(--c-card); color: var(--c-text); }

/* ── Main content ──────────────────────────────────────── */
.main-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.page-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
.page-content.has-bottom-nav {
  padding-bottom: calc(56px + env(safe-area-inset-bottom));
}

/* ── AI Float Button ───────────────────────────────────── */
.ai-float-btn {
  position: fixed;
  right: 20px;
  width: 52px; height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 20px rgba(26,188,156,0.45);
  z-index: 200;
  transition: all 0.25s cubic-bezier(.34,1.56,.64,1);
}
.ai-float-btn:hover { transform: scale(1.08); box-shadow: 0 6px 28px rgba(26,188,156,0.55); }
.ai-float-btn:active { transform: scale(0.94); }
.ai-float-btn.is-open { background: var(--c-surface2); box-shadow: 0 4px 16px rgba(0,0,0,0.3); }
.ai-float-icon { font-size: 22px; line-height: 1; }

.recording-dot {
  position: absolute; top: -2px; right: -2px;
  width: 12px; height: 12px;
  background: #ef4444; border-radius: 50%;
  border: 2px solid var(--c-bg);
  animation: blink 1s infinite;
}
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }
</style>
