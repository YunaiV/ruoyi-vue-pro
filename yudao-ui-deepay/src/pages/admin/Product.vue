<!--
  admin/Product.vue — 商品管理（P0 占位）
  路径：/admin/product
-->
<script setup>
import { ref } from 'vue'
const msg = ref('商品管理模块建设中，敬请期待')
</script>
<template>
  <div>
    <h1 class="title">📦 商品管理</h1>
    <p class="sub">{{ msg }}</p>
    <div class="placeholder-panel">
      <span class="placeholder-icon">📦</span>
      <div class="placeholder-label">后端已就绪，前端页面即将上线</div>
      <div class="placeholder-hint">GET /api/product · POST /api/product</div>
    </div>
  </div>
</template>
<style scoped>
.title { font-size: 22px; font-weight: 800; color: #ffffff; margin: 0 0 6px; }
.sub   { font-size: 13px; color: #a0a0a0; margin: 0 0 40px; }
</style>
