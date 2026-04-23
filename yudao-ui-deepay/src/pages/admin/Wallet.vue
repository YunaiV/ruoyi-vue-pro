<!--
  admin/Wallet.vue — 钱包提现（P0 占位）
  路径：/admin/wallet
-->
<script setup></script>
<template>
  <div>
    <h1 class="title">💰 钱包提现</h1>
    <p class="sub">用户提现申请审核、余额管理</p>
    <div class="placeholder">
      <div class="icon">💰</div>
      <div class="label">钱包提现管理即将上线</div>
      <div class="hint">接口：GET /api/wallet · POST /api/wallet/approve</div>
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
