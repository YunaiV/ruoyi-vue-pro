<template>
  <div class="page">
    <div class="page-header">
      <h2>趋势分析</h2>
      <p class="page-subtitle">AI 帮你发现热门品类和风格趋势</p>
    </div>

    <!-- 趋势图展示 -->
    <div v-if="trendImages.length" class="trend-grid">
      <div v-for="(url, i) in trendImages" :key="i" class="trend-card">
        <img :src="url" :alt="`趋势款 ${i + 1}`" />
        <div class="trend-card__label">趋势款 #{{ i + 1 }}</div>
      </div>
    </div>

    <div v-else class="empty-tip">
      💬 在右下角 AI 助手问我：「最近什么款最火？」或「欧美市场流行什么风格？」
    </div>

    <!-- AI 对话助手 -->
    <AiChatDrawer
      module="trend"
      :customer-id="customerId"
      greeting="你好！问我趋势相关的问题，例如：「最近外套市场什么风格最火？」或「给我看看少女风的热门款」"
      @imageClick="addTrend"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'

const customerId  = ref<number | undefined>(undefined)
const trendImages = ref<string[]>([])

const addTrend = (url: string) => {
  if (!trendImages.value.includes(url)) {
    trendImages.value.push(url)
  }
}
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
.trend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
}
.trend-card {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.trend-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}
.trend-card__label {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
}
</style>
