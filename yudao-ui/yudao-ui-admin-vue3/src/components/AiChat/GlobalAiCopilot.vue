<template>
  <!--
    GlobalAiCopilot — 全站 AI Copilot 统一入口。

    使用方式：在根布局（如 BasicLayout、App.vue 或 AppProvider）中引入：
    <GlobalAiCopilot />

    特性：
    - Teleport to body，不受任何页面 overflow/z-index 影响
    - 自动读取当前路由，注入 module/route/entityType/entityId
    - 页面切换后 sessionId 持续（localStorage 持久化）
    - 与各页面内嵌的 AiChatDrawer 共享 sessionId（同 persistKey）
  -->
  <AiChatDrawer
    :module="currentModule"
    :customer-id="undefined"
    :tenant-id="0"
    :persist-key="globalPersistKey"
    :context-provider="getPageContext"
    :greeting="currentGreeting"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'
import type { ChatContext } from '@/api/ai/chat'

const route = useRoute()

/**
 * 从路由路径推断当前模块。
 * 路由规则（按首个匹配路径片段决定）：
 *   /ai/selection  → selection
 *   /ai/design     → design
 *   /ai/product    → product
 *   /ai/inventory  → inventory
 *   /ai/finance    → finance
 *   /ai/trend      → trend
 *   /ai/order      → order
 *   其他           → selection（默认）
 */
const MODULE_MAP: Record<string, string> = {
  selection: 'selection',
  design:    'design',
  product:   'product',
  inventory: 'inventory',
  finance:   'finance',
  trend:     'trend',
  order:     'order',
  sales:     'sales',
  payment:   'finance',
  pay:       'finance',
}

const currentModule = computed<string>(() => {
  const segments = route.path.replace(/^\//, '').split('/')
  for (const seg of segments) {
    const mod = MODULE_MAP[seg.toLowerCase()]
    if (mod) return mod
  }
  return 'selection'
})

/**
 * 全局 Copilot 使用固定 persistKey，保证跨页面 sessionId 不变。
 * 与页面内嵌 AiChatDrawer 区分开（页面内嵌用 `ai_session_{module}` 作 key）。
 */
const globalPersistKey = 'ai_global_session'

/**
 * 根据当前模块动态展示欢迎语。
 */
const GREETINGS: Record<string, string> = {
  selection: '嗨！我是购物顾问 🛍️\n告诉我你想选什么款式，我帮你找到最火的方向！',
  design:    '你好！我是 AI 设计师 🎨\n有任何设计需求，直接告诉我，一起出图！',
  product:   '你好！我是产品经理 📦\n关于商品定价、上架策略，直接问我～',
  inventory: '你好！我是库存专员 🏭\n查库存、预测补货量，随时为你服务！',
  finance:   '你好！我是财务总监 💼\nROI、利润分析、收款问题，直接问我！',
  trend:     '嗨！我是趋势分析师 📈\n想知道什么款最火？直接问我吧～',
  order:     '你好！我是客服专员 🎧\n订单查询、售后服务，我来帮你！',
  sales:     '你好！我是销售顾问 💪\n帮你把客户从感兴趣推进到成交！',
}

const currentGreeting = computed<string>(() =>
  GREETINGS[currentModule.value] ?? '你好！有任何问题，随时告诉我 🤖'
)

/**
 * 上下文提供者：每次发送消息时自动收集当前页面上下文。
 *
 * 注入逻辑：
 * 1. route.path   → route
 * 2. currentModule → module
 * 3. route.params.id 或 route.query.id → entityId
 * 4. route.params.type 或从路径推断   → entityType
 */
function getPageContext(): Partial<ChatContext> {
  const path = route.path
  const params = route.params as Record<string, string>
  const query  = route.query  as Record<string, string>

  // 推断 entityType
  let entityType: string | undefined
  let entityId:   string | undefined

  // 常见路由模式: /order/detail/:id, /product/:chainCode, /finance/payment/:paymentId
  const pathSegments = path.replace(/^\//, '').split('/')
  for (let i = 0; i < pathSegments.length; i++) {
    const seg = pathSegments[i]
    if (['order', 'orders'].includes(seg) && pathSegments[i + 1]) {
      entityType = 'order'
      entityId   = pathSegments[i + 1]
      break
    }
    if (['product', 'products'].includes(seg) && pathSegments[i + 1]) {
      entityType = 'product'
      entityId   = pathSegments[i + 1]
      break
    }
    if (['payment', 'payments', 'pay'].includes(seg) && pathSegments[i + 1]) {
      entityType = 'paymentLink'
      entityId   = pathSegments[i + 1]
      break
    }
    if (['customer', 'customers'].includes(seg) && pathSegments[i + 1]) {
      entityType = 'customer'
      entityId   = pathSegments[i + 1]
      break
    }
    if (['design', 'designs'].includes(seg) && pathSegments[i + 1]) {
      entityType = 'design'
      entityId   = pathSegments[i + 1]
      break
    }
  }

  // 从 route.params 补充
  if (!entityId) {
    entityId = (params.id || params.chainCode || params.orderId ||
                params.paymentId || query.id || query.chainCode) as string | undefined
  }

  return {
    route:      path,
    module:     currentModule.value,
    entityType: entityType,
    entityId:   entityId,
  }
}
</script>
