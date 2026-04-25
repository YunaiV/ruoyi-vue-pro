<template>
  <div class="page">
    <div class="page-header">
      <h2>AI 设计出图</h2>
      <p class="page-subtitle">一句话生成你想要的服装设计图</p>
    </div>

    <!-- 参考图（来自选款板块或 URL 参数） -->
    <div v-if="refImages.length" class="section">
      <div class="section__title">参考款</div>
      <div class="ref-images">
        <img v-for="(url, i) in refImages" :key="i" :src="url" class="ref-img" />
      </div>
    </div>

    <!-- 设计图结果 -->
    <div v-if="designImages.length" class="section">
      <div class="section__title">AI 生成设计图</div>
      <div class="design-grid">
        <div
          v-for="(url, i) in designImages"
          :key="i"
          class="design-card"
          :class="{ 'design-card--selected': selectedDesign === url }"
          @click="selectedDesign = url"
        >
          <img :src="url" :alt="`设计图 ${i + 1}`" />
          <div v-if="selectedDesign === url" class="design-card__badge">已选</div>
        </div>
      </div>
      <button v-if="selectedDesign" class="btn-primary" @click="confirmDesign">
        确认此款，进入生产 →
      </button>
    </div>

    <div v-else class="empty-tip">
      💬 在右下角 AI 助手输入你的设计需求，例如：「帮我设计一款宽松工装外套，蓝色，欧美风」
    </div>

    <!-- AI 对话助手 -->
    <AiChatDrawer
      module="design"
      :customer-id="customerId"
      greeting="告诉我你想设计什么款式，例如：「宽松工装外套，棉麻面料，欧美风」，我来帮你出图！"
      @imageClick="addDesignImage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'

const route     = useRoute()
const router    = useRouter()
const customerId = ref<number | undefined>(undefined)

const refImages    = ref<string[]>([])
const designImages = ref<string[]>([])
const selectedDesign = ref<string>('')

onMounted(() => {
  const refs = route.query.refs as string
  if (refs) {
    refImages.value = refs.split(',').filter(Boolean)
  }
})

const addDesignImage = (url: string) => {
  if (!designImages.value.includes(url)) {
    designImages.value.push(url)
  }
}

const confirmDesign = () => {
  router.push(`/ai/product?design=${encodeURIComponent(selectedDesign.value)}`)
}
</script>

<style scoped>
.page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1f2937; margin: 0 0 4px; }
.page-subtitle  { color: #6b7280; font-size: 14px; margin: 0; }

.section { margin-bottom: 28px; }
.section__title { font-size: 15px; font-weight: 600; color: #374151; margin-bottom: 12px; }

.ref-images { display: flex; gap: 10px; flex-wrap: wrap; }
.ref-img    { width: 80px; height: 100px; object-fit: cover; border-radius: 8px; }

.design-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 200px);
  gap: 16px;
  margin-bottom: 20px;
}
.design-card {
  position: relative;
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}
.design-card:hover           { border-color: #a5b4fc; }
.design-card--selected       { border-color: #6366f1; }
.design-card img             { width: 100%; height: 240px; object-fit: cover; display: block; }
.design-card__badge {
  position: absolute;
  top: 8px; left: 8px;
  background: #6366f1;
  color: #fff;
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 12px;
}

.empty-tip {
  text-align: center;
  color: #9ca3af;
  font-size: 15px;
  padding: 60px 20px;
  line-height: 1.8;
}

.btn-primary {
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 24px;
  padding: 10px 24px;
  font-size: 15px;
  cursor: pointer;
}
.btn-primary:hover { background: #4f46e5; }
</style>
