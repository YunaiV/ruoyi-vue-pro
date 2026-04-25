<template>
  <div class="page">
    <div class="page-header">
      <h2>财务 / ROI 分析</h2>
      <p class="page-subtitle">AI 帮你分析利润、ROI、均价，驱动定价决策</p>
    </div>

    <!-- KPI 卡片 -->
    <div v-if="kpi.roi != null" class="kpi-row">
      <div class="kpi-card" :class="roiClass">
        <div class="kpi-card__label">ROI</div>
        <div class="kpi-card__value">{{ (kpi.roi * 100).toFixed(1) }}%</div>
        <div class="kpi-card__trend">{{ roiTrend }}</div>
      </div>
      <div v-if="kpi.profit != null" class="kpi-card kpi-card--blue">
        <div class="kpi-card__label">单件利润</div>
        <div class="kpi-card__value">¥{{ kpi.profit.toFixed(2) }}</div>
        <div class="kpi-card__trend">售价 - 成本</div>
      </div>
      <div v-if="kpi.avgPrice != null" class="kpi-card kpi-card--purple">
        <div class="kpi-card__label">同品类均价</div>
        <div class="kpi-card__value">¥{{ kpi.avgPrice.toFixed(2) }}</div>
        <div class="kpi-card__trend">历史成交均值</div>
      </div>
      <div v-if="kpi.costPrice != null" class="kpi-card kpi-card--gray">
        <div class="kpi-card__label">生产成本</div>
        <div class="kpi-card__value">¥{{ kpi.costPrice.toFixed(2) }}</div>
      </div>
    </div>

    <div v-else class="empty-tip">
      💬 在右下角 AI 助手问我：「这款外套的 ROI 是多少？」或「外套品类均价是多少？」
    </div>

    <!-- AI 对话助手 -->
    <AiChatDrawer
      module="finance"
      :customer-id="customerId"
      greeting="你好！问我关于财务的任何问题，例如：「这款外套的 ROI 是多少？」或「如何提高利润？」"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'

const customerId = ref<number | undefined>(undefined)

interface KpiData {
  roi?: number
  profit?: number
  avgPrice?: number
  costPrice?: number
}
const kpi = ref<KpiData>({})

const roiClass = computed(() => {
  if (kpi.value.roi == null) return ''
  if (kpi.value.roi >= 0.4) return 'kpi-card--green'
  if (kpi.value.roi >= 0.2) return 'kpi-card--yellow'
  return 'kpi-card--red'
})

const roiTrend = computed(() => {
  if (kpi.value.roi == null) return ''
  if (kpi.value.roi >= 0.4) return '🟢 优秀'
  if (kpi.value.roi >= 0.2) return '🟡 良好'
  return '🔴 偏低，建议优化成本或提价'
})
</script>

<style scoped>
.page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1f2937; margin: 0 0 4px; }
.page-subtitle  { color: #6b7280; font-size: 14px; margin: 0; }

.kpi-row { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 28px; }
.kpi-card {
  flex: 1; min-width: 150px;
  background: #fff; border-radius: 14px; padding: 20px 22px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  border-left: 4px solid #d1d5db;
}
.kpi-card--green  { border-left-color: #22c55e; }
.kpi-card--yellow { border-left-color: #f59e0b; }
.kpi-card--red    { border-left-color: #ef4444; }
.kpi-card--blue   { border-left-color: #3b82f6; }
.kpi-card--purple { border-left-color: #8b5cf6; }
.kpi-card--gray   { border-left-color: #9ca3af; }

.kpi-card__label {
  font-size: 12px; color: #6b7280; font-weight: 500;
  text-transform: uppercase; margin-bottom: 6px; letter-spacing: 0.05em;
}
.kpi-card__value { font-size: 28px; font-weight: 800; color: #111827; line-height: 1; margin-bottom: 6px; }
.kpi-card__trend { font-size: 12px; color: #6b7280; }

.empty-tip {
  text-align: center; color: #9ca3af;
  font-size: 15px; padding: 60px 20px; line-height: 1.8;
}
</style>
