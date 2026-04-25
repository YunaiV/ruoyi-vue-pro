<!--
  admin/AdminHome.vue — 管理后台首页仪表盘
  路径：/admin
-->
<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const stats = [
  { label: '商品数',  value: '128',     delta: '+12',  up: true,  icon: '📦' },
  { label: '订单量',  value: '3,842',   delta: '+8%',  up: true,  icon: '📋' },
  { label: '支付流水', value: '€24,910', delta: '+15%', up: true,  icon: '💳' },
  { label: '活跃用户', value: '1,204',   delta: '-3%',  up: false, icon: '👥' },
]

const quickLinks = [
  { label: '🎭 灵感库',    path: '/admin/inspiration', desc: '管理灵感图片，选款改款' },
  { label: '⚙️ 菜单配置', path: '/admin/menu-config',  desc: '控制用户端功能入口' },
  { label: '📦 商品管理', path: '/admin/product',       desc: '上架/下架商品' },
  { label: '📋 订单管理', path: '/admin/order',         desc: '查看所有订单' },
  { label: '💳 支付流水', path: '/admin/payment',       desc: '支付记录与对账' },
  { label: '💰 钱包提现', path: '/admin/wallet',        desc: '用户提现审核' },
]
</script>

<template>
  <div>
    <!-- Header with accent underline -->
    <div class="dash-header">
      <div>
        <h1 class="dash-title">Deepay 管理后台</h1>
        <p class="dash-sub">欢迎回来，选择模块开始管理</p>
      </div>
      <div class="pulse-wrap">
        <span class="pulse-dot-inner" />
        <span class="pulse-ring" />
        <span class="pulse-label">系统正常</span>
      </div>
    </div>

    <!-- Stat cards -->
    <div class="stats-grid">
      <div
        v-for="(s, i) in stats" :key="s.label"
        class="stat-card"
        :style="{ animationDelay: `${i * 80}ms` }"
      >
        <div class="stat-icon">{{ s.icon }}</div>
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-bottom">
          <span class="stat-label">{{ s.label }}</span>
          <span :class="['stat-delta', s.up ? 'up' : 'down']">
            {{ s.up ? '▲' : '▼' }} {{ s.delta }}
          </span>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="section-title">快速入口</div>
    <div class="grid">
      <button
        v-for="link in quickLinks"
        :key="link.path"
        class="qcard"
        @click="router.push(link.path)"
      >
        <div class="qcard-label">{{ link.label }}</div>
        <div class="qcard-desc">{{ link.desc }}</div>
      </button>
    </div>
  </div>
</template>

<style scoped>
.dash-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
  border-bottom: 2px solid #1abc9c;
  padding-bottom: 16px;
}
.dash-title { font-size: 24px; font-weight: 800; color: #ffffff; margin: 0 0 4px; }
.dash-sub   { font-size: 14px; color: #a0a0a0; margin: 0; }

.pulse-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
}
.pulse-dot-inner {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #1abc9c;
  display: block;
  position: relative;
  z-index: 1;
}
.pulse-ring {
  position: absolute;
  left: 0; top: 0;
  width: 8px; height: 8px;
  border-radius: 50%;
  border: 2px solid #1abc9c;
  animation: ping 1.5s ease-out infinite;
}
.pulse-label { font-size: 12px; color: #1abc9c; font-weight: 600; padding-left: 4px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.stat-card {
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
  border-radius: 14px;
  padding: 20px 18px;
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
  animation: fade-up 0.5s ease both;
}
.stat-card:hover {
  border-color: #1abc9c;
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0,0,0,0.5), 0 0 0 1px rgba(26,188,156,0.1);
}
.stat-icon  { font-size: 24px; margin-bottom: 10px; }
.stat-value { font-size: 26px; font-weight: 800; color: #ffffff; letter-spacing: -0.02em; margin-bottom: 8px; }
.stat-bottom { display: flex; align-items: center; justify-content: space-between; }
.stat-label { font-size: 12px; color: #a0a0a0; font-weight: 500; }
.stat-delta {
  font-size: 11px; font-weight: 600;
  padding: 2px 6px; border-radius: 6px;
  display: inline-flex; align-items: center; gap: 2px;
}
.stat-delta.up   { background: rgba(26,188,156,0.12); color: #1abc9c; }
.stat-delta.down { background: rgba(239,68,68,0.12);  color: #ef4444; }

.section-title {
  font-size: 11px;
  font-weight: 700;
  color: #555555;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 12px;
}

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
.qcard {
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
  border-radius: 14px;
  padding: 20px 18px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s, transform 0.1s, box-shadow 0.15s;
  animation: fade-up 0.5s ease both;
}
.qcard:hover {
  border-color: #1abc9c;
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0,0,0,0.5), 0 0 0 1px rgba(26,188,156,0.1);
}
.qcard-label { font-size: 16px; font-weight: 700; color: #e0e0e0; margin-bottom: 6px; }
.qcard-desc  { font-size: 12px; color: #a0a0a0; }

@keyframes fade-up {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes ping {
  0%   { opacity: 0.8; transform: scale(1); }
  100% { opacity: 0;   transform: scale(2.5); }
}
</style>
