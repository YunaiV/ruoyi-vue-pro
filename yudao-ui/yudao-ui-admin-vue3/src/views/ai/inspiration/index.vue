<template>
  <div class="page">
    <h2>灵感库</h2>

    <div class="grid">
      <div
        v-for="item in list"
        :key="item.id"
        class="card"
        @click="toggle(item)"
      >
        <img :src="item.url" />
        <div class="info">
          <div class="title">{{ item.title }}</div>
          <div class="score">趋势分 {{ item.score }}</div>
        </div>

        <div v-if="item.selected" class="selected">✔</div>
      </div>
    </div>

    <div class="footer" v-if="selectedList.length">
      已选 {{ selectedList.length }} 张
      <button @click="goRedesign">用这些改款 →</button>
    </div>

    <!-- AI 助手：可用自然语言直接选款 -->
    <AiChatDrawer
      module="selection"
      greeting="你好！我是灵感库 AI 助手。直接告诉我你想做什么款，比如：「我要做欧美极简外套」"
      @done="handleAiDone"
      @imageClick="handleAiImageClick"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'

const router = useRouter()

const list = ref([
  { id: 1, url: 'https://picsum.photos/300/400?1', title: '极简西装', score: 88 },
  { id: 2, url: 'https://picsum.photos/300/400?2', title: '廓形大衣', score: 92 },
  { id: 3, url: 'https://picsum.photos/300/400?3', title: '通勤衬衫', score: 85 },
  { id: 4, url: 'https://picsum.photos/300/400?4', title: '高级连衣裙', score: 95 },
  { id: 5, url: 'https://picsum.photos/300/400?5', title: '设计感外套', score: 90 }
])

const toggle = (item) => {
  item.selected = !item.selected
}

const selectedList = computed(() => list.value.filter(i => i.selected))

const goRedesign = () => {
  const urls = selectedList.value.map(i => i.url).join(',')
  router.push(`/ai/redesign?refs=${urls}`)
}

// AI 选款完成后，将推荐图加入灵感库
const handleAiDone = (sessionId) => {
  console.log('AI 选款完成 sessionId=', sessionId)
}

// AI 推荐图片被点击 → 直接加入已选
const handleAiImageClick = (url) => {
  const exists = list.value.find(i => i.url === url)
  if (!exists) {
    list.value.push({
      id: Date.now(),
      url,
      title: 'AI 推荐款',
      score: 90,
      selected: true
    })
  }
}
</script>

<style scoped>
.page {
  padding: 20px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 200px);
  gap: 20px;
}

.card {
  position: relative;
  cursor: pointer;
}

.card img {
  width: 100%;
  border-radius: 10px;
}

.info {
  margin-top: 8px;
}

.title {
  font-weight: bold;
}

.score {
  color: #888;
  font-size: 12px;
}

.selected {
  position: absolute;
  top: 8px;
  right: 8px;
  background: green;
  color: #fff;
  padding: 4px 6px;
  border-radius: 6px;
}

.footer {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #000;
  color: #fff;
  padding: 10px 20px;
  border-radius: 20px;
}
</style>
