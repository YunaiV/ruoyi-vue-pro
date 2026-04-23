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
    <div class="placeholder">
      <div class="icon">📦</div>
      <div class="label">后端已就绪，前端页面即将上线</div>
      <div class="hint">接口：GET /api/product · POST /api/product</div>
    </div>
  </div>
</template>
<style scoped>
.title { font-size: 22px; font-weight: 800; color: #fff; margin: 0 0 6px; }
.sub   { font-size: 13px; color: #666; margin: 0 0 40px; }
.placeholder { text-align: center; padding: 60px 0; border: 1px dashed #222; border-radius: 16px; }
.icon  { font-size: 48px; margin-bottom: 12px; }
.label { font-size: 15px; color: #aaa; font-weight: 600; }
.hint  { font-size: 12px; color: #444; margin-top: 8px; }
</style>
