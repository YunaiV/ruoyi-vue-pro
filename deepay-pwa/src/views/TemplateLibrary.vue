<template>
  <div class="page">
    <!-- Cross-page nav -->
    <div class="page-nav-row">
      <button class="pnr-btn" @click="router.push('/')">💬 AI 对话</button>
      <button class="pnr-btn" @click="router.push('/image-library')">🖼️ 图库</button>
      <button class="pnr-btn" @click="router.push('/ai-sales')">🏪 AI 开店</button>
      <button class="pnr-btn active-page" disabled>📋 模板库</button>
      <button class="pnr-btn" @click="router.push('/settings')">⚙️ 设置</button>
    </div>
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
        @click="openPreview(tpl)"
      >
        <div class="tpl-cover" :style="{ background: tpl.gradient }">
          <span class="tpl-emoji">{{ tpl.emoji }}</span>
          <span class="tpl-badge">{{ tpl.category }}</span>
        </div>
        <div class="tpl-info">
          <span class="tpl-name">{{ tpl.name }}</span>
          <span class="tpl-meta">{{ tpl.count }}款 · {{ tpl.category }}</span>
          <button class="use-btn dp-btn" @click.stop="useTemplate(tpl)">立即使用</button>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="empty-state">
      <div class="empty-icon">🔍</div>
      <p>未找到相关模板</p>
    </div>

    <!-- Preview Modal -->
    <Transition name="modal">
      <div v-if="previewTpl" class="modal-mask" @click.self="previewTpl = null">
        <div class="modal-box">
          <div class="modal-cover" :style="{ background: previewTpl.gradient }">
            <span class="modal-emoji">{{ previewTpl.emoji }}</span>
            <button class="modal-close" @click="previewTpl = null">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M18 6L6 18M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="modal-body">
            <h3 class="modal-title">{{ previewTpl.name }}</h3>
            <p class="modal-desc">{{ previewTpl.count }} 款精选模板 · {{ previewTpl.category }}风格</p>
            <p class="modal-hint">选用此模板后，AI 将基于该风格为你快速生成完整的店铺方案。</p>
            <div class="modal-actions">
              <button class="dp-btn-ghost" @click="previewTpl = null">取消</button>
              <button class="dp-btn" @click="useTemplate(previewTpl)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M5 3l14 9-14 9V3z"/></svg>
                立即使用
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const categories = ['全部', '极简', '街头', '高奢', '运动', '韩系']
const activeCategory = ref('全部')
const search = ref('')
const loading = ref(true)
const previewTpl = ref(null)

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

function openPreview(tpl) { previewTpl.value = tpl }

function useTemplate(tpl) {
  previewTpl.value = null
  router.push({ path: '/ai-sales', query: { tpl: tpl.name } })
}

onMounted(() => { setTimeout(() => { loading.value = false }, 600) })
</script>

<style scoped>
.page { padding: 16px; max-width: 800px; margin: 0 auto; }

/* Cross-page nav */
.page-nav-row { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 14px; }
.pnr-btn {
  display: flex; align-items: center; gap: 5px;
  padding: 5px 13px; background: var(--dp-surface);
  border: 1px solid var(--dp-card-border);
  border-radius: 20px; color: var(--dp-text-sub);
  font-size: 12px; font-weight: 500; cursor: pointer; transition: all 0.15s;
}
.pnr-btn:hover { border-color: var(--dp-accent); color: var(--dp-accent); background: rgba(16,163,127,0.07); }
.pnr-btn.active-page { background: rgba(16,163,127,0.12); border-color: rgba(16,163,127,0.35); color: #10a37f; font-weight: 600; cursor: default; }
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
.tpl-badge {
  position: absolute; top: 10px; left: 10px;
  font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.4px;
  background: rgba(0,0,0,0.3); color: rgba(255,255,255,0.9);
  padding: 3px 9px; border-radius: 20px; backdrop-filter: blur(4px);
}
.tpl-info { padding: 12px; }
.tpl-name { display: block; font-size: 14px; font-weight: 700; color: var(--dp-text-bright); margin-bottom: 3px; }
.tpl-meta { display: block; font-size: 11px; color: var(--dp-text-muted); margin-bottom: 10px; }
.use-btn { width: 100%; padding: 8px; font-size: 13px; }
.empty-state { text-align: center; padding: 60px 0; color: var(--dp-text-muted); }
.empty-icon { font-size: 48px; margin-bottom: 12px; }

/* Modal */
.modal-mask {
  position: fixed; inset: 0; z-index: 400;
  background: rgba(0,0,0,0.65); backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center; padding: 20px;
}
.modal-box {
  width: 100%; max-width: 420px;
  background: var(--dp-card);
  border: 1px solid var(--dp-card-border);
  border-radius: 20px; overflow: hidden;
  box-shadow: var(--dp-shadow-lg);
}
.modal-cover {
  height: 140px; display: flex; align-items: center; justify-content: center;
  position: relative;
}
.modal-emoji { font-size: 56px; }
.modal-close {
  position: absolute; top: 12px; right: 12px;
  width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.25); border: none; color: rgba(255,255,255,0.8);
  border-radius: 50%; cursor: pointer; transition: background 0.15s;
}
.modal-close:hover { background: rgba(0,0,0,0.45); }
.modal-body { padding: 20px; }
.modal-title { font-size: 18px; font-weight: 700; color: var(--dp-text); margin: 0 0 6px; }
.modal-desc { font-size: 13px; color: var(--dp-text-sub); margin: 0 0 10px; }
.modal-hint { font-size: 12px; color: var(--dp-text-muted); margin: 0 0 18px; line-height: 1.5; }
.modal-actions { display: flex; gap: 10px; }
.modal-actions .dp-btn, .modal-actions .dp-btn-ghost { flex: 1; }

.modal-enter-active { transition: opacity 0.2s ease-out; }
.modal-leave-active { transition: opacity 0.15s ease-in; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal-box { transform: scale(0.94) translateY(12px); }
</style>

