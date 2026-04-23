<!--
  AdminLayout.vue — 管理后台骨架布局
  所有 /admin/* 页面共享此布局（侧边栏 + 主内容区）
-->
<script setup>
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route  = useRoute()

const nav = [
  {
    group: '🎨 AI设计',
    items: [
      { label: '出款生成',   path: '/admin/ai/generate' },
      { label: '整季系列',   path: '/admin/ai/season' },
      { label: '改款设计',   path: '/admin/ai/redesign' },
      { label: '技术包',     path: '/admin/ai/techpack' },
    ],
  },
  {
    group: '🧠 设计素材',
    items: [
      { label: '灵感库',    path: '/admin/inspiration' },
      { label: '模板库',    path: '/admin/template' },
    ],
  },
  {
    group: '💰 商业中心',
    items: [
      { label: '店铺',      path: '/admin/shop' },
      { label: '排行榜',    path: '/admin/leaderboard' },
      { label: '邀请',      path: '/admin/invite' },
      { label: '收益贡献',  path: '/admin/share' },
    ],
  },
  {
    group: '⚙️ 系统运营',
    items: [
      { label: '商品管理',  path: '/admin/product' },
      { label: '订单管理',  path: '/admin/order' },
      { label: '支付流水',  path: '/admin/payment' },
      { label: '钱包提现',  path: '/admin/wallet' },
      { label: '菜单配置',  path: '/admin/menu-config' },
    ],
  },
]

function isActive(path) {
  return route.path === path
}
</script>

<template>
  <div class="admin-shell">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo" @click="router.push('/admin')">
        <span class="logo-icon">D</span>
        <span class="logo-text">Deepay Admin</span>
      </div>

      <nav class="nav">
        <div v-for="section in nav" :key="section.group" class="nav-section">
          <div class="nav-group-label">{{ section.group }}</div>
          <button
            v-for="item in section.items"
            :key="item.path"
            :class="['nav-item', isActive(item.path) && 'active']"
            @click="router.push(item.path)"
          >
            {{ item.label }}
          </button>
        </div>
      </nav>

      <!-- 返回用户端 -->
      <button class="back-btn" @click="router.push('/')">
        ← 返回用户端
      </button>
    </aside>

    <!-- 主内容 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: #0B0B0B;
  color: #fff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

/* ── 侧边栏 ── */
.sidebar {
  width: 220px;
  min-height: 100vh;
  background: #111;
  border-right: 1px solid #222;
  display: flex;
  flex-direction: column;
  padding: 0 0 20px;
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid #222;
  margin-bottom: 8px;
}
.logo-icon {
  width: 28px;
  height: 28px;
  background: #00FF88;
  color: #000;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 14px;
  flex-shrink: 0;
}
.logo-text {
  font-size: 13px;
  font-weight: 700;
  color: #fff;
}

.nav { flex: 1; overflow-y: auto; padding: 0 8px; }
.nav-section { margin-bottom: 16px; }
.nav-group-label {
  font-size: 10px;
  font-weight: 700;
  color: #555;
  padding: 6px 8px 4px;
  letter-spacing: 0.05em;
}
.nav-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #aaa;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.nav-item:hover { background: #1a1a1a; color: #fff; }
.nav-item.active { background: #00FF8818; color: #00FF88; font-weight: 600; }

.back-btn {
  margin: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #333;
  background: transparent;
  color: #555;
  font-size: 12px;
  cursor: pointer;
  text-align: left;
  transition: color 0.15s, border-color 0.15s;
}
.back-btn:hover { color: #fff; border-color: #555; }

/* ── 主内容区 ── */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}
</style>
