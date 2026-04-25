<template>
  <div class="gallery-page">

    <!-- Header -->
    <div class="gallery-header">
      <div class="header-top">
        <h1 class="page-title">图库</h1>
        <p class="page-sub">发现并使用 AI 生成的时尚设计素材</p>
      </div>

      <!-- Cross-page nav -->
      <div class="page-nav-row">
        <button class="pnr-btn" @click="router.push('/')">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          AI 对话
        </button>
        <button class="pnr-btn active-page" disabled>🖼️ 图库</button>
        <button class="pnr-btn" @click="router.push('/ai-sales')">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          AI 开店
        </button>
        <button class="pnr-btn" @click="router.push('/template-library')">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          模板库
        </button>
      </div>

      <!-- Search + filters -->
      <div class="header-controls">
        <div class="search-box">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" class="search-ico"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input v-model="searchQuery" class="search-input" placeholder="搜索图片..." />
        </div>

        <div class="filter-chips">
          <button
            v-for="f in filters"
            :key="f"
            class="filter-chip"
            :class="{ active: activeFilter === f }"
            @click="activeFilter = f"
          >{{ f }}</button>
        </div>
      </div>
    </div>

    <!-- Grid -->
    <div class="gallery-grid">
      <!-- Skeleton loading -->
      <template v-if="loading">
        <div v-for="i in 12" :key="'sk-' + i" class="gallery-card skeleton-card">
          <div class="skeleton-img"></div>
          <div class="skeleton-text"></div>
        </div>
      </template>

      <!-- Actual cards -->
      <template v-else>
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="gallery-card"
          :class="{ visible: cardsVisible }"
        >
          <div class="card-img" :style="{ background: item.gradient }">
            <div class="card-overlay">
              <button class="use-btn" @click.stop="router.push({ path: '/ai-sales', query: { img: item.title } })">用于开店</button>
              <button class="preview-btn" @click.stop="router.push('/template-library')" title="查看模板">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
              </button>
            </div>
            <div class="card-badge">{{ item.category }}</div>
          </div>
          <div class="card-info">
            <span class="card-title">{{ item.title }}</span>
            <span class="card-meta">{{ item.size }}</span>
          </div>
        </div>
      </template>
    </div>

    <!-- Floating AI generate button -->
    <button class="fab-btn" @click="showGenModal = true">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
      AI 生成
    </button>

    <!-- Simple modal -->
    <div v-if="showGenModal" class="modal-backdrop" @click.self="showGenModal = false">
      <div class="modal">
        <h3 class="modal-title">AI 图像生成</h3>
        <p class="modal-sub">描述你想要生成的图像</p>
        <textarea class="modal-textarea" placeholder="例如：一款高级感秋冬外套，深藏蓝色，简约廓形..." rows="4"></textarea>
        <div class="modal-actions">
          <button class="modal-cancel" @click="showGenModal = false">取消</button>
          <button class="modal-confirm" @click="showGenModal = false">开始生成</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(true)
const cardsVisible = ref(false)
const searchQuery = ref('')
const activeFilter = ref('全部')
const showGenModal = ref(false)

const filters = ['全部', '服装', '配饰', '鞋靴', '背景']

const images = [
  { id: 1, title: '春季宽松外套', category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 2, title: '复古皮革手包', category: '配饰', size: '1920×1920', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { id: 3, title: '极简白色连衣裙', category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { id: 4, title: '厚底增高老爹鞋', category: '鞋靴', size: '1800×1800', gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { id: 5, title: '工作室白背景', category: '背景', size: '3000×2000', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
  { id: 6, title: '丹宁牛仔套装', category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' },
  { id: 7, title: '丝巾配饰', category: '配饰', size: '1600×1600', gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' },
  { id: 8, title: '秋冬针织毛衣', category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' },
  { id: 9, title: '高跟细跟凉鞋', category: '鞋靴', size: '1800×1800', gradient: 'linear-gradient(135deg, #fd7043 0%, #ff8a65 100%)' },
  { id: 10, title: '渐变粉色背景', category: '背景', size: '3000×2000', gradient: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)' },
  { id: 11, title: '运动休闲套装', category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg, #0fd850 0%, #f9f047 100%)' },
  { id: 12, title: '金属质感腰带', category: '配饰', size: '1600×600', gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)' },
]

const filteredItems = computed(() => {
  let list = images
  if (activeFilter.value !== '全部') {
    list = list.filter(i => i.category === activeFilter.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(i => i.title.toLowerCase().includes(q) || i.category.toLowerCase().includes(q))
  }
  return list
})

onMounted(() => {
  setTimeout(() => { loading.value = false }, 1000)
  setTimeout(() => { cardsVisible.value = true }, 1050)
})
</script>

<style scoped>
.gallery-page {
  min-height: 100%;
  padding: 24px 28px 100px;
  background: var(--gpt-main);
  color: var(--gpt-text);
  position: relative;
}

/* Header */
.gallery-header {
  max-width: 1100px;
  margin: 0 auto 28px;
}
.header-top { margin-bottom: 14px; }

/* Cross-page nav row */
.page-nav-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.pnr-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 20px;
  color: var(--gpt-text-sub);
  font-size: 12.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.pnr-btn:hover { border-color: #10a37f; color: #10a37f; background: rgba(16,163,127,0.07); }
.pnr-btn.active-page { background: rgba(16,163,127,0.12); border-color: rgba(16,163,127,0.35); color: #10a37f; font-weight: 600; cursor: default; }
.page-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--gpt-text);
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}
.page-sub {
  font-size: 14px;
  color: var(--gpt-text-sub);
  margin: 0;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 10px;
  padding: 8px 14px;
  flex: 1;
  min-width: 200px;
  max-width: 360px;
}
.search-ico { color: var(--gpt-text-muted); flex-shrink: 0; }
.search-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-size: 14px;
  color: var(--gpt-text);
}
.search-input::placeholder { color: var(--gpt-text-muted); }

.filter-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.filter-chip {
  padding: 7px 14px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 20px;
  font-size: 13px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}
.filter-chip:hover { border-color: #10a37f; color: #10a37f; }
.filter-chip.active {
  background: #10a37f;
  border-color: #10a37f;
  color: white;
}

/* Grid */
.gallery-grid {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 900px) {
  .gallery-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 560px) {
  .gallery-grid { grid-template-columns: 1fr; }
  .gallery-page { padding: 16px 16px 100px; }
}

/* Card */
.gallery-card {
  background: var(--dp-card);
  border: 1px solid var(--gpt-border);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, opacity 0.4s;
  opacity: 0;
}
.gallery-card.visible { opacity: 1; }
.gallery-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--dp-shadow-lg);
}

.card-img {
  height: 200px;
  position: relative;
  overflow: hidden;
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.2s;
}
.gallery-card:hover .card-overlay { opacity: 1; }

.use-btn {
  padding: 8px 20px;
  background: #10a37f;
  border: none;
  border-radius: 20px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s;
}
.use-btn:hover { background: #0d8b6e; transform: scale(1.04); }

.preview-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.preview-btn:hover { background: rgba(255, 255, 255, 0.25); }

.card-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 10px;
  font-size: 11px;
  color: white;
  backdrop-filter: blur(4px);
}

.card-info {
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  font-size: 13.5px;
  font-weight: 500;
  color: var(--gpt-text);
}
.card-meta {
  font-size: 11px;
  color: var(--gpt-text-muted);
}

/* Skeleton */
.skeleton-card {
  opacity: 1;
}
.skeleton-img {
  height: 200px;
  background: var(--gpt-input-bg);
  animation: shimmer 1.5s ease-in-out infinite;
}
.skeleton-text {
  height: 16px;
  margin: 14px;
  background: var(--gpt-input-bg);
  border-radius: 4px;
  animation: shimmer 1.5s ease-in-out infinite 0.3s;
}

@keyframes shimmer {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 0.8; }
}

/* FAB */
.fab-btn {
  position: fixed;
  bottom: 28px;
  right: 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 22px;
  background: #10a37f;
  border: none;
  border-radius: 28px;
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(16, 163, 127, 0.4);
  transition: background 0.15s, transform 0.15s, box-shadow 0.15s;
  z-index: 10;
}
.fab-btn:hover {
  background: #0d8b6e;
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(16, 163, 127, 0.5);
}

/* Modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 24px;
}
.modal {
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 16px;
  padding: 28px;
  width: 100%;
  max-width: 460px;
}
.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--gpt-text);
  margin: 0 0 6px;
}
.modal-sub {
  font-size: 13px;
  color: var(--gpt-text-sub);
  margin: 0 0 16px;
}
.modal-textarea {
  width: 100%;
  background: var(--gpt-main);
  border: 1px solid var(--gpt-border);
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
  color: var(--gpt-text);
  outline: none;
  resize: vertical;
  box-sizing: border-box;
  font-family: inherit;
  transition: border-color 0.2s;
}
.modal-textarea:focus { border-color: #10a37f; }
.modal-textarea::placeholder { color: var(--gpt-text-muted); }
.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 18px;
}
.modal-cancel {
  padding: 9px 20px;
  background: transparent;
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  color: var(--gpt-text-sub);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}
.modal-cancel:hover { background: var(--gpt-sidebar-hover); }
.modal-confirm {
  padding: 9px 22px;
  background: #10a37f;
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.modal-confirm:hover { background: #0d8b6e; }
</style>
