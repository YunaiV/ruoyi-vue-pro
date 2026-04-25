<template>
  <div class="page">
    <!-- Search -->
    <div class="search-wrap">
      <input v-model="search" class="dp-input search-input" placeholder="🔍  搜索模板…" />
    </div>

    <!-- Filter Chips -->
    <div class="filter-row scrollbar-hide">
      <button
        v-for="cat in categories"
        :key="cat"
        class="dp-chip"
        :class="{ active: activeCategory === cat }"
        @click="activeCategory = cat"
      >{{ cat }}</button>
    </div>

    <!-- Loading Skeleton -->
    <div v-if="loading" class="grid-2">
      <div v-for="i in 4" :key="i" class="dp-skeleton" style="height:240px;"></div>
    </div>

    <!-- Grid -->
    <div v-else-if="filteredTemplates.length" class="grid-2">
      <div
        v-for="tpl in filteredTemplates"
        :key="tpl.id"
        class="tpl-card dp-card"
        @click="selectedTpl = tpl"
      >
        <div class="tpl-cover" :style="{ background: tpl.gradient }">
          <span class="tpl-emoji">{{ tpl.emoji }}</span>
        </div>
        <div class="tpl-info">
          <span class="tpl-name">{{ tpl.name }}</span>
          <span class="tpl-meta">{{ tpl.count }}款 · {{ tpl.category }}</span>
          <button class="use-btn dp-btn" @click.stop>使用</button>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="empty-state">
      <div class="empty-icon">��</div>
      <p>未找到相关模板</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const categories = ['全部', '极简', '街头', '高奢', '运动', '韩系']
const activeCategory = ref('全部')
const search = ref('')
const loading = ref(true)
const selectedTpl = ref(null)

const templates = ref([
  { id:1,  name:'极简白色系', category:'极简', count:12, emoji:'🤍', gradient:'linear-gradient(135deg,#1a1a2e,#2a2a3e)' },
  { id:2,  name:'街头潮流',   category:'街头', count:18, emoji:'🔥', gradient:'linear-gradient(135deg,#2d1b69,#533483)' },
  { id:3,  name:'高奢黑金',   category:'高奢', count:8,  emoji:'✨', gradient:'linear-gradient(135deg,#1b1b1b,#c9a84c44)' },
  { id:4,  name:'运动酷感',   category:'运动', count:24, emoji:'⚡', gradient:'linear-gradient(135deg,#0f4c75,#1abc9c44)' },
  { id:5,  name:'韩系甜美',   category:'韩系', count:16, emoji:'🌸', gradient:'linear-gradient(135deg,#3d0c11,#c93a5a44)' },
  { id:6,  name:'极简灰调',   category:'极简', count:10, emoji:'🩶', gradient:'linear-gradient(135deg,#2a2a2a,#3a3a4a)' },
  { id:7,  name:'街头涂鸦',   category:'街头', count:14, emoji:'🎨', gradient:'linear-gradient(135deg,#1a0a2e,#4a1a7a)' },
  { id:8,  name:'运动机能',   category:'运动', count:20, emoji:'🏃', gradient:'linear-gradient(135deg,#0a2a0a,#1a6b3a)' },
])

const filteredTemplates = computed(() => {
  let list = templates.value
  if (activeCategory.value !== '全部') list = list.filter(t => t.category === activeCategory.value)
  if (search.value.trim()) list = list.filter(t => t.name.includes(search.value.trim()))
  return list
})

onMounted(() => { setTimeout(() => { loading.value = false }, 600) })
</script>

<style scoped>
.page { padding: 16px; max-width: 800px; margin: 0 auto; }
.search-wrap { margin-bottom: 12px; }
.search-input { background: var(--dp-surface); }
.filter-row { display: flex; gap: 8px; overflow-x: auto; padding: 4px 2px; margin-bottom: 16px; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (min-width: 768px) { .grid-2 { grid-template-columns: repeat(3, 1fr); } }
.tpl-card { padding: 0; overflow: hidden; cursor: pointer; }
.tpl-cover {
  height: 150px; display: flex; align-items: center; justify-content: center;
  border-radius: 14px 14px 0 0; position: relative;
}
.tpl-emoji { font-size: 44px; }
.tpl-info { padding: 12px; }
.tpl-name { display: block; font-size: 14px; font-weight: 700; color: var(--dp-text-bright); margin-bottom: 3px; }
.tpl-meta { display: block; font-size: 11px; color: var(--dp-text-muted); margin-bottom: 10px; }
.use-btn { width: 100%; padding: 8px; font-size: 13px; }
.empty-state { text-align: center; padding: 60px 0; color: var(--dp-text-muted); }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
</style>
