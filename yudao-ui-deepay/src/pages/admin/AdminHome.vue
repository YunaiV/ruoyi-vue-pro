<!--
  admin/AdminHome.vue — 管理后台首页仪表盘
  路径：/admin
-->
<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const quickLinks = [
  { label: '🎭 灵感库',     path: '/admin/inspiration', desc: '管理灵感图片，选款改款' },
  { label: '⚙️ 菜单配置',  path: '/admin/menu-config',  desc: '控制用户端功能入口' },
  { label: '📦 商品管理',  path: '/admin/product',       desc: '上架/下架商品' },
  { label: '📋 订单管理',  path: '/admin/order',         desc: '查看所有订单' },
  { label: '💳 支付流水',  path: '/admin/payment',       desc: '支付记录与对账' },
  { label: '💰 钱包提现',  path: '/admin/wallet',        desc: '用户提现审核' },
]
</script>

<template>
  <div>
    <h1 class="title">Deepay 管理后台</h1>
    <p class="sub">选择下方模块开始管理</p>
    <div class="grid">
      <button
        v-for="link in quickLinks"
        :key="link.path"
        class="card"
        @click="router.push(link.path)"
      >
        <div class="card-label">{{ link.label }}</div>
        <div class="card-desc">{{ link.desc }}</div>
      </button>
    </div>
  </div>
</template>

<style scoped>
.title { font-size: 24px; font-weight: 800; color: #fff; margin: 0 0 6px; }
.sub   { font-size: 14px; color: #555; margin: 0 0 28px; }
.grid  { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
.card {
  background: #111;
  border: 1px solid #1e1e1e;
  border-radius: 14px;
  padding: 20px 18px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s, transform 0.1s;
}
.card:hover { border-color: #00FF8844; transform: translateY(-2px); }
.card-label { font-size: 16px; font-weight: 700; color: #fff; margin-bottom: 6px; }
.card-desc  { font-size: 12px; color: #555; }
</style>
