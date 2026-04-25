<template>
  <div class="page">
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

    <!-- Loading -->
    <div v-if="loading" class="grid-2">
      <div v-for="i in 6" :key="i" class="dp-skeleton" style="aspect-ratio:3/4;"></div>
    </div>

    <!-- Grid -->
    <div v-else-if="filteredItems.length" class="grid-2">
      <div
        v-for="item in filteredItems"
        :key="item.id"
        class="img-card dp-card"
      >
        <div class="img-cover" :style="{ background: item.gradient }">
          <span class="img-placeholder">{{ item.emoji }}</span>
          <button class="fav-btn" :class="{ active: item.fav }" @click.stop="item.fav = !item.fav" aria-label="收藏">
            <svg width="16" height="16" viewBox="0 0 24 24" :fill="item.fav ? '#1abc9c' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
            </svg>
          </button>
          <div class="img-overlay">
            <span class="img-name">{{ item.name }}</span>
            <span class="img-price">¥{{ item.price }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="empty-state">
      <div class="empty-icon">🔍</div>
      <p>暂无相关图片</p>
    </div>

    <p class="pull-hint">↓ 下拉刷新获取更多</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const categories = ['全部', '外套', '连衣裙', '裤子', '上衣', '运动']
const activeCategory = ref('全部')
const loading = ref(true)

const allItems = ref([
  { id:1,  name:'极简白衬衫',   cat:'上衣',   price:199, fav:false, emoji:'👔', gradient:'linear-gradient(160deg,#1a1a2e,#16213e)' },
  { id:2,  name:'流苏连衣裙',   cat:'连衣裙', price:399, fav:true,  emoji:'👗', gradient:'linear-gradient(160deg,#2d1b69,#533483)' },
  { id:3,  name:'宽腿休闲裤',   cat:'裤子',   price:259, fav:false, emoji:'👖', gradient:'linear-gradient(160deg,#0f3460,#1abc9c44)' },
  { id:4,  name:'皮质短外套',   cat:'外套',   price:699, fav:false, emoji:'🧥', gradient:'linear-gradient(160deg,#1b1b1b,#c9a84c44)' },
  { id:5,  name:'针织连衣裙',   cat:'连衣裙', price:329, fav:true,  emoji:'👗', gradient:'linear-gradient(160deg,#2c1810,#c93a5a44)' },
  { id:6,  name:'运动套装',     cat:'运动',   price:459, fav:false, emoji:'🏃', gradient:'linear-gradient(160deg,#1a3a1a,#2d6a4f)' },
  { id:7,  name:'蕾丝上衣',     cat:'上衣',   price:279, fav:false, emoji:'👚', gradient:'linear-gradient(160deg,#3a1a3a,#8b2fc9)' },
  { id:8,  name:'风衣外套',     cat:'外套',   price:899, fav:true,  emoji:'🧥', gradient:'linear-gradient(160deg,#0a1628,#1a6b8a)' },
])

const filteredItems = computed(() =>
  activeCategory.value === '全部'
    ? allItems.value
    : allItems.value.filter(i => i.cat === activeCategory.value)
)

onMounted(() => { setTimeout(() => { loading.value = false }, 800) })
</script>

<style scoped>
.page { padding: 16px; max-width: 800px; margin: 0 auto; }
.filter-row { display: flex; gap: 8px; overflow-x: auto; padding: 4px 2px; margin-bottom: 16px; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (min-width: 768px) { .grid-2 { grid-template-columns: repeat(3, 1fr); } }
.img-card { padding: 0; overflow: hidden; cursor: pointer; }
.img-cover {
  aspect-ratio: 3/4; position: relative;
  display: flex; align-items: center; justify-content: center;
  overflow: hidden; border-radius: 14px;
}
.img-placeholder { font-size: 48px; opacity: 0.7; }
.fav-btn {
  position: absolute; top: 8px; right: 8px;
  width: 30px; height: 30px; background: rgba(0,0,0,0.45);
  border: none; border-radius: 50%; cursor: pointer; color: #fff;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s; backdrop-filter: blur(4px);
}
.fav-btn.active { color: #1abc9c; }
.img-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  padding: 24px 12px 12px;
  display: flex; justify-content: space-between; align-items: flex-end;
}
.img-name { font-size: 12px; font-weight: 600; color: white; }
.img-price { font-size: 13px; font-weight: 700; color: #1abc9c; }
.empty-state { text-align: center; padding: 60px 0; color: var(--dp-text-muted); }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.pull-hint { text-align: center; font-size: 12px; color: var(--dp-text-muted); padding: 16px 0 4px; }
</style>
