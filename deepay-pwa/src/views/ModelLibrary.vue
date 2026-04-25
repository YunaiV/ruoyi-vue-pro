<template>
  <div class="page">
    <!-- New Model Button -->
    <button class="dp-btn new-btn">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
      </svg>
      新建模特
    </button>

    <!-- Filter Tabs -->
    <div class="filter-tabs">
      <button
        v-for="tab in tabs"
        :key="tab"
        class="tab-btn"
        :class="{ active: activeTab === tab }"
        @click="activeTab = tab"
      >{{ tab }}</button>
    </div>

    <!-- Loading Skeleton -->
    <div v-if="loading" class="grid-2">
      <div v-for="i in 4" :key="i" class="dp-skeleton" style="aspect-ratio:3/4;"></div>
    </div>

    <!-- Grid -->
    <div v-else-if="filteredModels.length" class="grid-2">
      <ModelLibraryCard
        v-for="(model, i) in filteredModels"
        :key="model.id"
        :name="model.name"
        :tag="model.tag"
        :index="i"
        :isFavorite="model.fav"
        @toggle-favorite="(v) => model.fav = v"
      />
    </div>

    <!-- Empty -->
    <div v-else class="empty-state">
      <div class="empty-icon">👗</div>
      <p>还没有模特，点击上方按钮添加</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import ModelLibraryCard from '@/components/ModelLibraryCard.vue'

const tabs = ['全部', '收藏', '最近']
const activeTab = ref('全部')
const loading = ref(true)

const models = ref([
  { id:1, name:'亚洲时尚模特 A', tag:'热门', fav:true },
  { id:2, name:'欧美高冷模特 B', tag:'新品', fav:false },
  { id:3, name:'街头潮流模特 C', tag:'',     fav:false },
  { id:4, name:'高奢优雅模特 D', tag:'热门', fav:true },
  { id:5, name:'清新甜美模特 E', tag:'新品', fav:false },
  { id:6, name:'运动活力模特 F', tag:'',     fav:false },
])

const filteredModels = computed(() => {
  if (activeTab.value === '收藏') return models.value.filter(m => m.fav)
  if (activeTab.value === '最近') return models.value.slice(0, 4)
  return models.value
})

onMounted(() => { setTimeout(() => { loading.value = false }, 700) })
</script>

<style scoped>
.page { padding: 16px; max-width: 800px; margin: 0 auto; }
.new-btn { width: 100%; margin-bottom: 16px; padding: 14px; font-size: 15px; }
.filter-tabs { display: flex; gap: 0; margin-bottom: 16px; background: var(--dp-chip-bg); border-radius: 10px; padding: 3px; }
.tab-btn {
  flex: 1; padding: 8px; border: none; background: none;
  color: var(--dp-text-sub); font-size: 13px; font-weight: 500;
  border-radius: 8px; cursor: pointer; transition: all 0.15s;
}
.tab-btn.active { background: var(--dp-surface); color: var(--dp-text); box-shadow: 0 1px 4px rgba(0,0,0,0.15); font-weight: 600; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (min-width: 768px) { .grid-2 { grid-template-columns: repeat(3, 1fr); } }
.empty-state { text-align: center; padding: 60px 0; color: var(--dp-text-muted); }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
</style>
