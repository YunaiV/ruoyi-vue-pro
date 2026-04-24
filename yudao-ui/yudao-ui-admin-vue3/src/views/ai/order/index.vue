<template>
  <div class="page">
    <div class="page-header">
      <h2>订单管理</h2>
      <p class="page-subtitle">AI 帮你查询订单状态、发货进度、对账信息</p>
    </div>

    <!-- 订单列表 -->
    <div v-if="orders.length" class="order-list">
      <div v-for="o in orders" :key="o.id" class="order-card">
        <div class="order-card__status-dot" :class="`order-dot--${o.status.toLowerCase()}`" />
        <div class="order-card__body">
          <div class="order-card__id">订单 #{{ o.id }}</div>
          <div class="order-card__meta">{{ o.chainCode }} · {{ o.createdAt }}</div>
        </div>
        <div class="order-card__right">
          <div class="order-card__amount">¥{{ o.amount }}</div>
          <div class="order-card__status-label" :class="`order-label--${o.status.toLowerCase()}`">
            {{ STATUS_LABELS[o.status] ?? o.status }}
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-tip">
      💬 在右下角 AI 助手问我：「查一下我的最新订单」或「链码 XXXXX 的订单状态是什么？」
    </div>

    <!-- AI 对话助手 -->
    <AiChatDrawer
      module="order"
      :customer-id="customerId"
      greeting="你好！问我关于订单的任何问题，例如：「查一下最新订单状态」或「链码 XXXXX 的订单对账情况」"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'

const customerId = ref<number | undefined>(undefined)

const STATUS_LABELS: Record<string, string> = {
  PAID:      '✅ 已支付',
  PENDING:   '⏳ 待支付',
  CANCELLED: '❌ 已取消',
  REFUNDED:  '↩️ 已退款',
}

interface Order {
  id: string | number
  chainCode: string
  amount: string
  status: string
  createdAt: string
}
const orders = ref<Order[]>([])
</script>

<style scoped>
.page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1f2937; margin: 0 0 4px; }
.page-subtitle  { color: #6b7280; font-size: 14px; margin: 0; }

.order-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 80px; }
.order-card {
  display: flex; align-items: center; gap: 12px;
  background: #fff; border-radius: 12px; padding: 14px 18px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}
.order-card__status-dot {
  width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0;
}
.order-dot--paid      { background: #22c55e; }
.order-dot--pending   { background: #f59e0b; animation: blink 1s step-end infinite; }
.order-dot--cancelled { background: #9ca3af; }
.order-dot--refunded  { background: #3b82f6; }
@keyframes blink { 50% { opacity: 0; } }

.order-card__body { flex: 1; min-width: 0; }
.order-card__id   { font-weight: 600; color: #111827; font-size: 14px; }
.order-card__meta { color: #9ca3af; font-size: 12px; margin-top: 2px; }

.order-card__right { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.order-card__amount { font-size: 16px; font-weight: 700; color: #6366f1; }
.order-card__status-label { font-size: 12px; }

.order-label--paid      { color: #16a34a; }
.order-label--pending   { color: #d97706; }
.order-label--cancelled { color: #9ca3af; }
.order-label--refunded  { color: #2563eb; }

.empty-tip {
  text-align: center; color: #9ca3af;
  font-size: 15px; padding: 60px 20px; line-height: 1.8;
}
</style>
