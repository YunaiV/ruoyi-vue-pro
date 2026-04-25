<template>
  <div class="page">
    <div class="page-header">
      <h2>库存管理</h2>
      <p class="page-subtitle">AI 帮你预测销量、建议补货</p>
    </div>

    <div class="empty-tip">
      💬 在右下角 AI 助手问我：「外套库存还有多少？」或「这款需要补货吗？」
    </div>

    <!-- AI 对话助手 -->
    <AiChatDrawer
      module="inventory"
      :customer-id="customerId"
      greeting="你好！问我关于库存的问题，例如：「外套库存还剩多少？」或「根据最近销量，需要备多少货？」"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'

const customerId = ref<number | undefined>(undefined)
</script>

<style scoped>
.page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1f2937; margin: 0 0 4px; }
.page-subtitle  { color: #6b7280; font-size: 14px; margin: 0; }
.empty-tip {
  text-align: center;
  color: #9ca3af;
  font-size: 15px;
  padding: 60px 20px;
  line-height: 1.8;
}
</style>
