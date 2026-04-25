<template>
  <div class="page">
    <!-- New Design Button -->
    <button class="dp-btn new-btn">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
        <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
      </svg>
      新建设计
    </button>

    <!-- Filters Row -->
    <div class="filters-row">
      <div class="filter-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: activeStatus === tab.value }"
          @click="activeStatus = tab.value"
        >{{ tab.label }}</button>
      </div>
      <select class="sort-select" v-model="sortOrder">
        <option value="newest">最新</option>
        <option value="oldest">最早</option>
      </select>
    </div>

    <!-- Loading Skeleton -->
    <div v-if="loading" class="grid-2">
      <div v-for="i in 4" :key="i" class="dp-skeleton" style="height:220px;"></div>
    </div>

    <!-- Grid -->
    <div v-else-if="filteredDesigns.length" class="grid-2">
      <div v-for="design in filteredDesigns" :key="design.id" class="design-card dp-card">
        <div class="design-cover" :style="{ background: design.gradient }">
          <span class="design-emoji">{{ design.emoji }}</span>
          <div class="status-badge" :class="design.status">
            {{ design.status === 'draft' ? '草稿' : '已发布' }}
          </div>
        </div>
        <div class="design-info">
          <span class="design-title">{{ design.title }}</span>
          <span class="design-date">{{ design.date }}</span>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="empty-state">
      <div class="empty-icon">✏️</div>
      <p>还没有设计作品</p>
      <p style="font-size:13px;color:var(--dp-text-muted)">点击"新建设计"开始创作</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const statusTabs = [
  { label: '全部', value: 'all' },
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
]
const activeStatus = ref('all')
const sortOrder = ref('newest')
const loading = ref(true)

const designs = ref([
  { id:1, title:'夏季连衣裙系列',   status:'published', date:'2024-06-01', emoji:'👗', gradient:'linear-gradient(135deg,#2d1b69,#533483)' },
  { id:2, title:'街头风卫衣设计',   status:'draft',     date:'2024-05-28', emoji:'👕', gradient:'linear-gradient(135deg,#1a1a2e,#3a3a5e)' },
  { id:3, title:'高奢晚礼服',       status:'published', date:'2024-05-20', emoji:'👘', gradient:'linear-gradient(135deg,#1b1b1b,#c9a84c44)' },
  { id:4, title:'运动休闲套装',     status:'draft',     date:'2024-05-15', emoji:'🏃', gradient:'linear-gradient(135deg,#0f4c75,#1abc9c44)' },
  { id:5, title:'韩系甜美上衣',     status:'published', date:'2024-05-10', emoji:'🌸', gradient:'linear-gradient(135deg,#3d0c11,#c93a5a44)' },
  { id:6, title:'极简风衬衫',       status:'draft',     date:'2024-05-05', emoji:'👔', gradient:'linear-gradient(135deg,#1a1a1a,#2a2a2a)' },
])

const filteredDesigns = computed(() => {
  let list = activeStatus.value === 'all' ? designs.value : designs.value.filter(d => d.status === activeStatus.value)
  return sortOrder.value === 'newest' ? list : [...list].reverse()
})

onMounted(() => { setTimeout(() => { loading.value = false }, 600) })
</script>

<style scoped>
.page { padding: 16px; max-width: 800px; margin: 0 auto; }
.new-btn { width: 100%; margin-bottom: 16px; padding: 14px; font-size: 15px; }
.filters-row { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.filter-tabs { display: flex; gap: 0; flex: 1; background: var(--dp-chip-bg); border-radius: 10px; padding: 3px; }
.tab-btn {
  flex: 1; padding: 8px; border: none; background: none;
  color: var(--dp-text-sub); font-size: 13px; font-weight: 500;
  border-radius: 8px; cursor: pointer; transition: all 0.15s;
}
.tab-btn.active { background: var(--dp-surface); color: var(--dp-text); font-weight: 600; box-shadow: 0 1px 4px rgba(0,0,0,0.15); }
.sort-select {
  background: var(--dp-chip-bg); border: 1px solid var(--dp-chip-border);
  color: var(--dp-text); font-size: 13px; padding: 8px 12px;
  border-radius: 10px; cursor: pointer; outline: none;
}
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (min-width: 768px) { .grid-2 { grid-template-columns: repeat(3, 1fr); } }
.design-card { padding: 0; overflow: hidden; cursor: pointer; }
.design-cover {
  height: 140px; display: flex; align-items: center; justify-content: center;
  border-radius: 14px 14px 0 0; position: relative;
}
.design-emoji { font-size: 44px; }
.status-badge {
  position: absolute; top: 8px; left: 8px;
  font-size: 11px; font-weight: 700; padding: 3px 10px; border-radius: 999px;
}
.status-badge.draft { background: rgba(100,100,100,0.7); color: #ccc; }
.status-badge.published { background: rgba(26,188,156,0.85); color: #fff; }
.design-info { padding: 10px 12px 12px; }
.design-title { display: block; font-size: 13px; font-weight: 600; color: var(--dp-text); margin-bottom: 3px; }
.design-date { font-size: 11px; color: var(--dp-text-muted); }
.empty-state { text-align: center; padding: 60px 0; color: var(--dp-text-muted); }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
</style>
