<template>
  <div class="page">
    <div class="page-header">
      <h2>选款引导</h2>
      <p class="page-subtitle">AI 帮你找对品类、风格、市场，精准选款</p>
    </div>

    <!-- 画像摘要卡片（选款完成后显示） -->
    <div v-if="profile.category" class="profile-card">
      <div class="profile-card__title">📋 当前选款画像</div>
      <div class="profile-card__tags">
        <span v-if="profile.category"   class="tag">品类：{{ profile.category }}</span>
        <span v-if="profile.style"      class="tag">风格：{{ profile.style }}</span>
        <span v-if="profile.crowd"      class="tag">客群：{{ profile.crowd }}</span>
        <span v-if="profile.market"     class="tag">市场：{{ profile.market }}</span>
        <span v-if="profile.priceLevel" class="tag">价位：{{ profile.priceLevel }}</span>
      </div>
    </div>

    <!-- 推荐图列表（AI 完成后自动填充） -->
    <div v-if="selectionImages.length" class="images-grid">
      <div
        v-for="(url, idx) in selectionImages"
        :key="idx"
        class="image-card"
        :class="{ 'image-card--selected': selectedImages.has(url) }"
        @click="toggleImage(url)"
      >
        <img :src="url" :alt="`推荐款 ${idx + 1}`" />
        <div v-if="selectedImages.has(url)" class="image-card__check">✔</div>
      </div>
    </div>

    <div v-if="selectedImages.size" class="action-bar">
      已选 {{ selectedImages.size }} 款
      <button class="btn-primary" @click="goDesign">进入设计改款 →</button>
    </div>

    <!-- AI 对话助手 -->
    <AiChatDrawer
      module="selection"
      :customer-id="customerId"
      greeting="你好！告诉我你想做什么，我来帮你找对选款方向。例如：「我想做少女运动外套，卖欧美市场」"
      @done="handleDone"
      @imageClick="toggleImage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'

const router      = useRouter()
const customerId  = ref<number | undefined>(undefined)

const selectionImages = ref<string[]>([])
const selectedImages  = ref<Set<string>>(new Set())

interface Profile {
  category?: string
  style?: string
  crowd?: string
  market?: string
  priceLevel?: string
}
const profile = ref<Profile>({})

const toggleImage = (url: string) => {
  if (selectedImages.value.has(url)) {
    selectedImages.value.delete(url)
  } else {
    selectedImages.value.add(url)
  }
  // 触发响应式更新
  selectedImages.value = new Set(selectedImages.value)
}

const handleDone = (_sessionId: string) => {
  // AI 完成选款后可通过 API 刷新推荐图（此处预留扩展点）
}

const goDesign = () => {
  const refs = Array.from(selectedImages.value).join(',')
  router.push(`/ai/design?refs=${refs}`)
}
</script>

<style scoped>
.page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1f2937; margin: 0 0 4px; }
.page-subtitle { color: #6b7280; font-size: 14px; margin: 0; }

.profile-card {
  background: #f5f3ff;
  border: 1px solid #ddd6fe;
  border-radius: 12px;
  padding: 14px 18px;
  margin-bottom: 20px;
}
.profile-card__title { font-weight: 600; color: #5b21b6; margin-bottom: 10px; }
.profile-card__tags  { display: flex; flex-wrap: wrap; gap: 8px; }
.tag {
  background: #ede9fe;
  color: #5b21b6;
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 13px;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 160px);
  gap: 14px;
  margin-bottom: 80px;
}
.image-card {
  position: relative;
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}
.image-card:hover           { border-color: #a5b4fc; }
.image-card--selected       { border-color: #6366f1; }
.image-card img             { width: 100%; height: 200px; object-fit: cover; display: block; }
.image-card__check {
  position: absolute;
  top: 8px; right: 8px;
  background: #6366f1;
  color: #fff;
  width: 24px; height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.action-bar {
  position: fixed;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  background: #1f2937;
  color: #fff;
  padding: 10px 24px;
  border-radius: 30px;
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  z-index: 100;
}
.btn-primary {
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 6px 16px;
  cursor: pointer;
  font-size: 14px;
}
.btn-primary:hover { background: #4f46e5; }
</style>
