<!--
  admin/Inspiration.vue — 灵感库管理页
  路径：/admin/inspiration
  ✔ 5张图 + 卡片
  ✔ 点击选中（多选，最多5张）
  ✔ 底部浮条 → 用选中图跳改款
  ✔ 本地数据，不依赖后端，100% 可显示
-->
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const list = ref([
  { id: 1, url: 'https://picsum.photos/300/400?random=1', title: '极简西装',   score: 88 },
  { id: 2, url: 'https://picsum.photos/300/400?random=2', title: '廓形大衣',   score: 92 },
  { id: 3, url: 'https://picsum.photos/300/400?random=3', title: '通勤衬衫',   score: 85 },
  { id: 4, url: 'https://picsum.photos/300/400?random=4', title: '高级连衣裙', score: 95 },
  { id: 5, url: 'https://picsum.photos/300/400?random=5', title: '设计感外套', score: 90 },
  { id: 6, url: 'https://picsum.photos/300/400?random=6', title: '街头卫衣',   score: 82 },
  { id: 7, url: 'https://picsum.photos/300/400?random=7', title: '针织开衫',   score: 79 },
  { id: 8, url: 'https://picsum.photos/300/400?random=8', title: '高腰阔腿裤', score: 87 },
])

function toggle(item) {
  const sel = selectedIds.value
  if (sel.has(item.id)) {
    sel.delete(item.id)
  } else {
    if (sel.size >= 5) return  // 最多5张
    sel.add(item.id)
  }
  selectedIds.value = new Set(sel)
}

const selectedIds = ref(new Set())
const isSelected  = (item) => selectedIds.value.has(item.id)
const selectedList = computed(() => list.value.filter(i => selectedIds.value.has(i.id)))

function goRedesign() {
  const urls = selectedList.value.map(i => encodeURIComponent(i.url)).join(',')
  router.push(`/redesign?refs=${urls}`)
}

function goAiGenerate() {
  const urls = selectedList.value.map(i => encodeURIComponent(i.url)).join(',')
  router.push(`/ai/design?refs=${urls}`)
}

function clearSelection() {
  selectedIds.value = new Set()
}
</script>

<template>
  <div>
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">🎭 灵感库</h1>
        <p class="page-sub">选择参考图 → 一键改款 / 出款（最多 5 张）</p>
      </div>
      <div class="header-actions" v-if="selectedIds.size">
        <span class="sel-count">已选 {{ selectedIds.size }} 张</span>
        <button class="btn-ghost" @click="clearSelection">清除</button>
      </div>
    </div>

    <!-- 图片网格 -->
    <div class="grid">
      <div
        v-for="item in list"
        :key="item.id"
        :class="['card', isSelected(item) && 'selected']"
        @click="toggle(item)"
      >
        <div class="img-wrap">
          <img :src="item.url" :alt="item.title" />
          <!-- 分数徽章 -->
          <span class="score-badge" :style="{ color: item.score >= 90 ? '#1abc9c' : item.score >= 85 ? '#F59E0B' : '#9CA3AF' }">
            {{ item.score }}
          </span>
          <!-- 选中遮罩 -->
          <div class="check-overlay" v-if="isSelected(item)">
            <span class="check-icon">✓</span>
          </div>
        </div>
        <div class="card-info">
          <div class="card-title">{{ item.title }}</div>
          <div class="card-score">趋势分 {{ item.score }}</div>
        </div>
      </div>
    </div>

    <!-- 底部浮条 -->
    <transition name="slide-up">
      <div v-if="selectedIds.size > 0" class="float-bar">
        <span class="float-count">已选 <strong>{{ selectedIds.size }}</strong> 张</span>
        <div class="float-actions">
          <button class="btn-redesign" @click="goRedesign">🔧 改款</button>
          <button class="btn-generate" @click="goAiGenerate">🎯 出款</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title {
  font-size: 22px;
  font-weight: 800;
  color: #e7e9ea;
  margin: 0 0 4px;
}
.page-sub {
  font-size: 13px;
  color: #627870;
  margin: 0;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sel-count {
  font-size: 13px;
  color: #1abc9c;
  font-weight: 600;
}
.btn-ghost {
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid #1e2e28;
  background: transparent;
  color: #627870;
  font-size: 13px;
  cursor: pointer;
}
.btn-ghost:hover { border-color: #253830; color: #a8b5b0; }

/* 网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  padding-bottom: 100px;
}

.card {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  background: #0d1512;
  transition: border-color 0.15s, transform 0.1s;
}
.card:hover { transform: translateY(-2px); }
.card.selected {
  border-color: #1abc9c;
  box-shadow: 0 0 0 1px #1abc9c, 0 0 16px rgba(26,188,156,0.2);
}

.img-wrap {
  position: relative;
  aspect-ratio: 3 / 4;
  overflow: hidden;
}
.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.score-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0,0,0,0.6);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 800;
  padding: 2px 7px;
  backdrop-filter: blur(4px);
}

.check-overlay {
  position: absolute;
  inset: 0;
  background: rgba(26,188,156,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}
.check-icon {
  width: 36px;
  height: 36px;
  background: #1abc9c;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 900;
}

.card-info {
  padding: 10px 12px;
}
.card-title {
  font-size: 13px;
  font-weight: 600;
  color: #e7e9ea;
}
.card-score {
  font-size: 11px;
  color: #627870;
  margin-top: 2px;
}

/* 底部浮条 */
.float-bar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(8,14,12,0.9);
  border: 1px solid rgba(26,188,156,0.2);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.6);
  z-index: 100;
  white-space: nowrap;
}
.float-count {
  font-size: 13px;
  color: #a8b5b0;
}
.float-count strong { color: #1abc9c; }
.float-actions { display: flex; gap: 8px; }

.btn-redesign, .btn-generate {
  padding: 8px 18px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
}
.btn-redesign:hover, .btn-generate:hover { opacity: 0.85; }
.btn-redesign { background: #182620; color: #F59E0B; border: 1px solid rgba(245,158,11,0.3); }
.btn-generate { background: #1abc9c; color: #fff; }

/* 动画 */
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.25s ease; }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateX(-50%) translateY(20px); }
</style>
