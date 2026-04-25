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
          <div class="card-img" :style="item.src ? {} : { background: item.gradient }">
            <!-- Real AI-generated image -->
            <img v-if="item.src" :src="item.src" :alt="item.title" class="card-real-img" />
            <div class="card-overlay">
              <button class="use-btn" @click.stop="router.push({ path: '/ai-sales', query: { img: item.src || item.title } })">用于开店</button>
              <button class="preview-btn" @click.stop="item.isAI ? openDetail(item) : router.push('/template-library')" title="查看">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              </button>
            </div>
            <div class="card-badge" :class="{ 'badge-ai': item.isAI }">{{ item.category }}</div>
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

    <!-- ── AI Generation Modal ────────────────────────────── -->
    <Transition name="modal-fade">
    <div v-if="showGenModal" class="modal-backdrop" @click.self="closeModal">
      <div class="gen-modal">

        <!-- Modal header -->
        <div class="gen-modal-head">
          <div class="gen-modal-title-row">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#10a37f" stroke-width="2" stroke-linecap="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
            <h3 class="gen-modal-title">AI 图像生成</h3>
          </div>
          <button class="gen-modal-close" @click="closeModal" :disabled="genStore.isGenerating">✕</button>
        </div>

        <!-- No API key warning -->
        <div v-if="!genStore.apiKey" class="api-key-warn">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          未设置 RunPod API Key —
          <button class="warn-link" @click="$router.push('/settings'); closeModal()">前往设置</button>
          填入后即可生成
        </div>

        <!-- Tab bar -->
        <div class="gen-tabs">
          <button class="gen-tab" :class="{ active: genTab === 'txt2img' }" @click="genTab = 'txt2img'">
            ✏️ 文生图
          </button>
          <button class="gen-tab" :class="{ active: genTab === 'img2img' }" @click="genTab = 'img2img'">
            🔄 图生图 / 局部编辑
          </button>
        </div>

        <!-- Model selector -->
        <div class="gen-section">
          <label class="gen-label">选择模型</label>
          <div class="model-cards">
            <button
              v-for="m in availableModels"
              :key="m.id"
              class="model-card"
              :class="{ selected: selectedModel === m.id }"
              @click="selectedModel = m.id"
            >
              <span class="model-badge" :style="{ background: m.badgeColor }">{{ m.badge }}</span>
              <span class="model-name">{{ m.label }}</span>
              <span class="model-desc">{{ m.desc }}</span>
            </button>
          </div>
        </div>

        <!-- Prompt -->
        <div class="gen-section">
          <label class="gen-label">
            描述 Prompt
            <span class="gen-label-hint">用中文或英文描述你想要的效果</span>
          </label>
          <textarea
            v-model="prompt"
            class="gen-textarea"
            :placeholder="promptPlaceholder"
            rows="4"
            :disabled="genStore.isGenerating"
          ></textarea>
        </div>

        <!-- Negative prompt -->
        <div class="gen-section">
          <label class="gen-label">
            反向词 <span class="gen-label-hint optional">可选</span>
          </label>
          <input
            v-model="negativePrompt"
            class="gen-input"
            placeholder="模糊、低质量、水印、文字..."
            :disabled="genStore.isGenerating"
          />
        </div>

        <!-- Reference image (img2img / Kontext / Qwen) -->
        <div v-if="genTab === 'img2img' || currentModel?.supportsImg" class="gen-section">
          <label class="gen-label">
            参考图片 URL
            <span class="gen-label-hint">{{ genTab === 'img2img' ? '必填' : '可选' }}</span>
          </label>
          <input
            v-model="refImageUrl"
            class="gen-input"
            placeholder="https://... 或从图库点击「用于开店」自动填入"
            :disabled="genStore.isGenerating"
          />
          <div v-if="refImageUrl" class="ref-preview">
            <img :src="refImageUrl" alt="参考图" @error="refImageUrl = ''" />
          </div>
        </div>

        <!-- Advanced params (collapsible) -->
        <details class="gen-advanced">
          <summary class="gen-advanced-toggle">⚙️ 高级参数</summary>
          <div class="gen-advanced-body">

            <div class="param-row">
              <label class="param-label">尺寸</label>
              <div class="size-chips">
                <button
                  v-for="s in sizeOptions"
                  :key="s"
                  class="size-chip"
                  :class="{ active: selectedSize === s }"
                  @click="selectedSize = s"
                >{{ s }}</button>
              </div>
            </div>

            <div class="param-row">
              <label class="param-label">
                推理步数
                <span class="param-val">{{ steps }}</span>
              </label>
              <input type="range" v-model.number="steps" :min="currentModel?.id === 'flux-schnell' ? 1 : 10" :max="50" step="1" class="param-slider" :disabled="genStore.isGenerating" />
            </div>

            <div class="param-row">
              <label class="param-label">
                引导系数 (Guidance)
                <span class="param-val">{{ guidance }}</span>
              </label>
              <input type="range" v-model.number="guidance" min="0" max="15" step="0.5" class="param-slider" :disabled="genStore.isGenerating" />
            </div>

            <div class="param-row">
              <label class="param-label">
                随机种子
                <span class="param-val">{{ seed === -1 ? '随机' : seed }}</span>
              </label>
              <div class="seed-row">
                <input type="range" v-model.number="seed" min="-1" max="999999999" step="1" class="param-slider" :disabled="genStore.isGenerating" />
                <button class="seed-reset" @click="seed = -1" title="随机">🎲</button>
              </div>
            </div>

          </div>
        </details>

        <!-- Progress bar -->
        <div v-if="genStore.isGenerating || genStore.status === 'error'" class="gen-progress-wrap">
          <div class="gen-progress-bar" :class="genStore.status">
            <div v-if="genStore.isGenerating" class="gen-progress-fill"></div>
          </div>
          <p class="gen-progress-text" :class="{ error: genStore.status === 'error' }">
            {{ genStore.progress }}
          </p>
        </div>

        <!-- Latest result preview -->
        <div v-if="latestResult" class="gen-result">
          <div class="gen-result-head">
            <span class="gen-result-label">✨ 生成结果</span>
            <div class="gen-result-actions">
              <a :href="latestResult" download="deepay-generated.png" class="result-action-btn">
                ⬇ 下载
              </a>
              <button class="result-action-btn accent" @click="useInSales">🏪 用于开店</button>
            </div>
          </div>
          <img :src="latestResult" class="gen-result-img" alt="AI 生成结果" />
        </div>

        <!-- Action buttons -->
        <div class="gen-modal-footer">
          <button class="gen-cancel-btn" @click="closeModal">
            {{ genStore.isGenerating ? '后台运行' : '关闭' }}
          </button>
          <button
            v-if="genStore.isGenerating"
            class="gen-stop-btn"
            @click="stopGeneration"
          >⏹ 停止</button>
          <button
            v-else
            class="gen-submit-btn"
            :disabled="!prompt.trim() || !genStore.apiKey"
            @click="startGeneration"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
            开始生成
          </button>
        </div>

      </div>
    </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useImageGenStore } from '@/store/index.js'
import { generate, cancelJob, MODELS, toImageSrc } from '@/api/runpod.js'

const router = useRouter()
const route  = useRoute()
const genStore = useImageGenStore()

// ── Gallery state ──────────────────────────────────────
const loading      = ref(true)
const cardsVisible = ref(false)
const searchQuery  = ref('')
const activeFilter = ref('全部')

// ── Generation modal state ─────────────────────────────
const showGenModal  = ref(false)
const genTab        = ref('txt2img')
const selectedModel = ref('flux-kontext')
const prompt        = ref('')
const negativePrompt = ref('')
const refImageUrl   = ref('')
const selectedSize  = ref('1024x1024')
const steps         = ref(28)
const guidance      = ref(2)
const seed          = ref(-1)
const latestResult  = ref('')

const sizeOptions = ['512x512', '768x768', '1024x1024', '1024x768', '768x1024', '1280x720']

// Models available per tab
const availableModels = computed(() => {
  const all = Object.values(MODELS)
  if (genTab.value === 'img2img') return all.filter(m => m.supportsImg)
  return all
})

const currentModel = computed(() => MODELS[selectedModel.value])

const promptPlaceholder = computed(() => {
  if (genTab.value === 'img2img') {
    return '描述想要的修改，例如：把领口改成深V领，颜色换成深海蓝，保持其他部分不变'
  }
  return '例如：一款高级感秋冬外套，深藏蓝色，简约廓形，欧根纱领，模特平铺拍摄，超清质感'
})

// Sync model defaults with params when model changes
watch(selectedModel, (id) => {
  const m = MODELS[id]
  if (!m) return
  steps.value    = m.defaults.num_inference_steps
  guidance.value = m.defaults.guidance
  genStore.setModel(id)
})

// When img2img tab is selected, switch to a model that supports images
watch(genTab, (tab) => {
  if (tab === 'img2img' && !currentModel.value?.supportsImg) {
    selectedModel.value = 'flux-kontext'
  }
})

// Pre-fill ref image URL from route query (?img=)
watch(() => route.query.img, (v) => {
  if (v) {
    refImageUrl.value = String(v)
    genTab.value = 'img2img'
    showGenModal.value = true
  }
}, { immediate: true })

// ── Gallery data: static + AI-generated history ─────────
const staticImages = [
  { id: 1,  title: '春季宽松外套',   category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg,#667eea,#764ba2)', src: null },
  { id: 2,  title: '复古皮革手包',   category: '配饰', size: '1920×1920', gradient: 'linear-gradient(135deg,#f093fb,#f5576c)', src: null },
  { id: 3,  title: '极简白色连衣裙', category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg,#4facfe,#00f2fe)', src: null },
  { id: 4,  title: '厚底增高老爹鞋', category: '鞋靴', size: '1800×1800', gradient: 'linear-gradient(135deg,#43e97b,#38f9d7)', src: null },
  { id: 5,  title: '工作室白背景',   category: '背景', size: '3000×2000', gradient: 'linear-gradient(135deg,#fa709a,#fee140)', src: null },
  { id: 6,  title: '丹宁牛仔套装',   category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg,#a18cd1,#fbc2eb)', src: null },
  { id: 7,  title: '丝巾配饰',       category: '配饰', size: '1600×1600', gradient: 'linear-gradient(135deg,#ffecd2,#fcb69f)', src: null },
  { id: 8,  title: '秋冬针织毛衣',   category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg,#a1c4fd,#c2e9fb)', src: null },
  { id: 9,  title: '高跟细跟凉鞋',   category: '鞋靴', size: '1800×1800', gradient: 'linear-gradient(135deg,#fd7043,#ff8a65)', src: null },
  { id: 10, title: '渐变粉色背景',   category: '背景', size: '3000×2000', gradient: 'linear-gradient(135deg,#e0c3fc,#8ec5fc)', src: null },
  { id: 11, title: '运动休闲套装',   category: '服装', size: '2048×2048', gradient: 'linear-gradient(135deg,#0fd850,#f9f047)', src: null },
  { id: 12, title: '金属质感腰带',   category: '配饰', size: '1600×600',  gradient: 'linear-gradient(135deg,#30cfd0,#330867)', src: null },
]

const filters = ['全部', '服装', '配饰', '鞋靴', '背景', 'AI 生成']

// Merge static + generated history as gallery items
const allImages = computed(() => {
  const genItems = genStore.history.map(h => ({
    id:       h.id,
    title:    h.prompt.slice(0, 20) + (h.prompt.length > 20 ? '…' : ''),
    category: 'AI 生成',
    size:     '1024×1024',
    gradient: 'linear-gradient(135deg,#10a37f,#0d5f4e)',
    src:      toImageSrc(h.src),
    isAI:     true,
    prompt:   h.prompt,
    model:    h.model,
    seed:     h.seed,
  }))
  return [...genItems, ...staticImages]
})

const filteredItems = computed(() => {
  let list = allImages.value
  if (activeFilter.value !== '全部') {
    list = list.filter(i => i.category === activeFilter.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(i => i.title.toLowerCase().includes(q) || i.category.toLowerCase().includes(q))
  }
  return list
})

// ── Generation logic ───────────────────────────────────
async function startGeneration() {
  if (!prompt.value.trim() || !genStore.apiKey) return
  genStore.reset()
  latestResult.value = ''

  const params = {
    prompt:              prompt.value.trim(),
    negative_prompt:     negativePrompt.value.trim(),
    seed:                seed.value,
    num_inference_steps: steps.value,
    guidance:            guidance.value,
    size:                selectedSize.value,
    output_format:       'png',
  }
  if (refImageUrl.value.trim()) {
    params.image = refImageUrl.value.trim()
  }

  try {
    const result = await generate(
      genStore.apiKey,
      selectedModel.value,
      params,
      (status, msg) => genStore.updateProgress(status, msg),
    )

    genStore.addResult({
      images: result.images,
      seed:   result.seed,
      prompt: params.prompt,
      model:  selectedModel.value,
      params,
    })

    // Show first image as preview
    if (result.images[0]) {
      latestResult.value = toImageSrc(result.images[0])
    }

  } catch (err) {
    genStore.setError(`❌ ${err.message}`)
  }
}

async function stopGeneration() {
  if (genStore.jobId) {
    try {
      await cancelJob(genStore.apiKey, selectedModel.value, genStore.jobId)
    } catch { /* ignore */ }
  }
  genStore.reset()
}

function closeModal() {
  if (!genStore.isGenerating) {
    showGenModal.value = false
    genStore.reset()
  } else {
    // Keep generating in background, just close UI
    showGenModal.value = false
  }
}

function useInSales() {
  if (latestResult.value) {
    router.push({ path: '/ai-sales', query: { img: latestResult.value.startsWith('http') ? latestResult.value : 'generated' } })
  }
}

// ── Lifecycle ──────────────────────────────────────────
onMounted(() => {
  setTimeout(() => { loading.value = false }, 800)
  setTimeout(() => { cardsVisible.value = true }, 850)
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
