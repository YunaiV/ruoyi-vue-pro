<!--
  AdminLayout.vue — 管理后台骨架布局（商业级侧边栏）
  ✔ 动态菜单：从 feature store 读取，后台改配置前端立刻变
  ✔ SVG 图标：每个菜单项独立图标
  ✔ 分组折叠：每个 group 可独立展开/收起
  ✔ 路由高亮：当前页面对应菜单项自动高亮
  ✔ 移动端：汉堡按钮 + 遮罩层
  ✔ 侧边栏折叠：可收窄到仅图标模式
-->
<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useFeatureStore } from '@/store/modules/feature'

const router = useRouter()
const route  = useRoute()
const featureStore = useFeatureStore()

// ── 侧边栏状态 ──────────────────────────────────────────────────────
const collapsed   = ref(false)   // 窄图标模式
const mobileOpen  = ref(false)   // 移动端展开

// 分组展开/收起（默认全展开）
const openGroups = ref(new Set(['ai_design', 'material', 'commerce', 'ops']))

function toggleGroup(key) {
  const s = new Set(openGroups.value)
  s.has(key) ? s.delete(key) : s.add(key)
  openGroups.value = s
}

// 关闭移动端侧边栏（路由跳转后）
watch(() => route.path, () => { mobileOpen.value = false })

// ── 菜单数据（固定 P0 运营组 + 动态功能组）───────────────────────────
const GROUP_META = {
  ai_design: { label: 'AI 设计', icon: 'palette' },
  material:  { label: '设计素材', icon: 'image'   },
  commerce:  { label: '商业中心', icon: 'currency' },
  ops:       { label: '系统运营', icon: 'settings' },
}

// P0 运营菜单（固定，不受 feature config 控制）
const OPS_ITEMS = [
  { key: 'product',     name: '商品管理', path: '/admin/product',     icon: 'box'     },
  { key: 'order',       name: '订单管理', path: '/admin/order',       icon: 'list'    },
  { key: 'payment',     name: '支付流水', path: '/admin/payment',     icon: 'credit'  },
  { key: 'wallet',      name: '钱包提现', path: '/admin/wallet',      icon: 'wallet'  },
  { key: 'menu-config', name: '菜单配置', path: '/admin/menu-config', icon: 'sliders' },
]

// 动态功能菜单（来自 feature store，visible=false 的自动隐藏）
const dynamicGroups = computed(() => {
  const groups = {}
  featureStore.visibleMenus.forEach(f => {
    const g = f.menuGroup || 'other'
    if (!groups[g]) groups[g] = []
    groups[g].push(f)
  })
  return groups
})

// 完整菜单结构（动态 + 固定运营组）
const allGroups = computed(() => {
  const result = []
  // 动态分组（ai_design / material / commerce）
  for (const [gKey, items] of Object.entries(dynamicGroups.value)) {
    if (gKey === 'ops') continue  // ops 单独处理
    result.push({
      key:   gKey,
      label: GROUP_META[gKey]?.label ?? gKey,
      icon:  GROUP_META[gKey]?.icon  ?? 'folder',
      items: items.map(f => ({ key: f.key, name: f.name, path: f.path, icon: f.icon ?? '' })),
    })
  }
  // 固定运营组
  result.push({ key: 'ops', label: '系统运营', icon: 'settings', items: OPS_ITEMS })
  return result
})

function isActive(path) { return route.path === path }
function navigate(path) { router.push(path) }
</script>

<template>
  <!-- 移动端遮罩 -->
  <div
    v-if="mobileOpen"
    class="mob-overlay"
    @click="mobileOpen = false"
  />

  <div class="shell">

    <!-- ── 侧边栏 ──────────────────────────────────────── -->
    <aside :class="['sidebar', collapsed && 'collapsed', mobileOpen && 'mob-open']">

      <!-- Logo + 折叠按钮 -->
      <div class="logo-row">
        <div class="logo" @click="navigate('/admin')">
          <span class="logo-icon">D</span>
          <span v-show="!collapsed" class="logo-text">Deepay Admin</span>
        </div>
        <button class="collapse-btn" @click="collapsed = !collapsed" title="折叠/展开">
          <svg-icon :name="collapsed ? 'chevron-right' : 'chevron-left'" />
        </button>
      </div>

      <!-- 菜单 -->
      <nav class="nav" :class="collapsed && 'nav-collapsed'">
        <div
          v-for="group in allGroups"
          :key="group.key"
          class="nav-group"
        >
          <!-- 分组标题（可点击折叠） -->
          <button
            v-show="!collapsed"
            class="group-hdr"
            @click="toggleGroup(group.key)"
          >
            <span class="group-label">{{ group.label }}</span>
            <span class="group-arrow" :class="openGroups.has(group.key) && 'open'">›</span>
          </button>

          <!-- 分组项 -->
          <div
            class="group-items"
            :class="(!openGroups.has(group.key) && !collapsed) && 'hidden'"
          >
            <button
              v-for="item in group.items"
              :key="item.key"
              :class="['nav-item', isActive(item.path) && 'active']"
              :title="collapsed ? item.name : ''"
              @click="navigate(item.path)"
            >
              <!-- emoji 图标（feature store 里的 icon 字段） -->
              <span class="item-emoji">{{ item.icon || ICON_MAP[item.key] || '◆' }}</span>
              <span v-show="!collapsed" class="item-label">{{ item.name }}</span>
              <!-- 当前页指示条 -->
              <span v-if="isActive(item.path) && !collapsed" class="active-dot" />
            </button>
          </div>
        </div>
      </nav>

      <!-- 底部：返回用户端 -->
      <button class="back-btn" @click="navigate('/')" :title="collapsed ? '返回用户端' : ''">
        <span class="item-emoji">←</span>
        <span v-show="!collapsed">返回用户端</span>
      </button>
    </aside>

    <!-- ── 主区域 ─────────────────────────────────────── -->
    <div class="main-wrap">

      <!-- 移动端顶栏 -->
      <header class="mob-header">
        <button class="hamburger" @click="mobileOpen = !mobileOpen">
          <span /><span /><span />
        </button>
        <span class="mob-title">Deepay Admin</span>
      </header>

      <!-- 内容区 -->
      <main class="content">
        <router-view />
      </main>
    </div>

  </div>
</template>

<!-- svg-icon shim：直接内联，避免依赖 -->
<script>
// 把图标名映射到 emoji，无需外部图标库
const ICON_MAP = {
  'ai_generate': '🎯', 'ai_season': '🌿', 'ai_redesign': '🔧', 'techpack': '📐',
  'inspiration': '🎭', 'template': '📦', 'shop': '🏪', 'leaderboard': '🏆',
  'invite': '🎁', 'share': '💸',
  'product': '📦', 'order': '📋', 'payment': '💳', 'wallet': '💰', 'menu-config': '⚙️',
}
</script>

<style scoped>
/* ── 基础层 ────────────────────────────────── */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.shell {
  display: flex;
  min-height: 100vh;
  background: #0B0B0B;
  color: #fff;
  font-family: -apple-system, BlinkMacSystemFont, 'Inter', 'Segoe UI', sans-serif;
}

/* ── 侧边栏 ─────────────────────────────────── */
.sidebar {
  width: 230px;
  min-height: 100vh;
  background: #101010;
  border-right: 1px solid #1c1c1c;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.22s ease;
  position: relative;
  z-index: 30;
}
.sidebar.collapsed { width: 60px; }

/* ── Logo 行 ─────────────────────────────────── */
.logo-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 14px 14px;
  border-bottom: 1px solid #1a1a1a;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  overflow: hidden;
  flex: 1;
  min-width: 0;
}
.logo-icon {
  width: 30px; height: 30px;
  background: linear-gradient(135deg, #00FF88, #00cc6a);
  color: #000;
  border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 900; font-size: 15px;
  flex-shrink: 0;
  box-shadow: 0 0 12px #00FF8840;
}
.logo-text {
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
}
.collapse-btn {
  width: 26px; height: 26px;
  display: flex; align-items: center; justify-content: center;
  background: #1a1a1a;
  border: 1px solid #252525;
  border-radius: 7px;
  color: #555;
  cursor: pointer;
  flex-shrink: 0;
  font-size: 12px;
  transition: color 0.15s, background 0.15s;
}
.collapse-btn:hover { color: #fff; background: #222; }

/* ── 菜单导航 ────────────────────────────────── */
.nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px 8px;
  scrollbar-width: thin;
  scrollbar-color: #222 transparent;
}
.nav::-webkit-scrollbar { width: 4px; }
.nav::-webkit-scrollbar-track { background: transparent; }
.nav::-webkit-scrollbar-thumb { background: #222; border-radius: 2px; }

.nav-group { margin-bottom: 4px; }

/* 分组标题 */
.group-hdr {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 5px 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #3a3a3a;
  transition: color 0.15s;
}
.group-hdr:hover { color: #555; }
.group-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.group-arrow {
  font-size: 14px;
  transition: transform 0.2s;
  line-height: 1;
}
.group-arrow.open { transform: rotate(90deg); }

/* 菜单项 */
.group-items { display: flex; flex-direction: column; gap: 1px; }
.group-items.hidden { display: none; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 9px 10px;
  border-radius: 9px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #666;
  font-size: 13px;
  text-align: left;
  position: relative;
  transition: background 0.12s, color 0.12s;
  white-space: nowrap;
  overflow: hidden;
}
.nav-item:hover { background: #181818; color: #ccc; }
.nav-item.active {
  background: #0f2a1a;
  color: #00FF88;
}

.item-emoji { font-size: 15px; flex-shrink: 0; width: 20px; text-align: center; }
.item-label { flex: 1; }

.active-dot {
  width: 5px; height: 5px;
  border-radius: 50%;
  background: #00FF88;
  flex-shrink: 0;
  box-shadow: 0 0 6px #00FF88;
}

/* 收起模式：居中显示 emoji */
.nav-collapsed .nav-item { justify-content: center; padding: 10px 0; }
.nav-collapsed .group-items { gap: 2px; }

/* ── 底部"返回用户端" ─────────────────────────── */
.back-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 8px;
  padding: 9px 10px;
  border-radius: 9px;
  border: 1px solid #1c1c1c;
  background: transparent;
  color: #333;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
  white-space: nowrap;
  overflow: hidden;
}
.back-btn:hover { color: #888; border-color: #333; }

/* ── 主区域 ──────────────────────────────────── */
.main-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}
.content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}

/* ── 移动端 ───────────────────────────────────── */
.mob-header {
  display: none;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #101010;
  border-bottom: 1px solid #1c1c1c;
  position: sticky;
  top: 0;
  z-index: 20;
}
.mob-title { font-size: 14px; font-weight: 700; }
.hamburger {
  display: flex; flex-direction: column; gap: 4px;
  background: none; border: none; cursor: pointer; padding: 4px;
}
.hamburger span {
  display: block; width: 20px; height: 2px;
  background: #fff; border-radius: 1px;
}
.mob-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.7);
  z-index: 25;
  backdrop-filter: blur(2px);
}

@media (max-width: 768px) {
  .mob-header { display: flex; }
  .content    { padding: 20px 16px; }
  .sidebar {
    position: fixed;
    left: -230px;
    top: 0; bottom: 0;
    transition: left 0.25s ease, width 0.22s ease;
    z-index: 30;
  }
  .sidebar.mob-open { left: 0; }
  .sidebar.collapsed { left: -60px; }
  .sidebar.collapsed.mob-open { left: 0; }
}
</style>

