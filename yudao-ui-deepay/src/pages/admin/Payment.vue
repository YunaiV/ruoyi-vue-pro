<!--
  admin/Payment.vue — 支付流水（P0 占位）
  路径：/admin/payment
-->
<script setup></script>
<template>
  <div>
    <h1 class="title">💳 支付流水</h1>
    <p class="sub">Jeepay / Stripe 支付记录与对账</p>
    <div class="placeholder-panel">
      <span class="placeholder-icon">💳</span>
      <div class="placeholder-label">支付流水即将上线</div>
      <div class="placeholder-hint">GET /api/payment · GET /api/payment/stats</div>
    </div>
  </div>
</template>
<style scoped>
.title { font-size: 22px; font-weight: 800; color: #ffffff; margin: 0 0 6px; }
.sub   { font-size: 13px; color: #a0a0a0; margin: 0 0 40px; }
</style>
