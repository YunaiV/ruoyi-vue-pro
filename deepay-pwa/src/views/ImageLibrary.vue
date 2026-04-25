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
              <button class="preview-btn" @click.stop="router.push('/template-library')" title="查看">
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
            🎨 图片编辑
          </button>
          <button class="gen-tab" :class="{ active: genTab === 'img2vid' }" @click="genTab = 'img2vid'">
            🎬 图生视频
          </button>
        </div>

        <!-- Model selector -->
        <div class="gen-section">
          <label class="gen-label">选择模型</label>
          <div class="model-cards">
            <template v-for="group in modelGroups" :key="group.label">
              <div class="model-group-label">{{ group.label }}</div>
              <button
                v-for="m in group.models"
                :key="m.id"
                class="model-card"
                :class="{ selected: selectedModel === m.id }"
                @click="selectedModel = m.id"
              >
                <span class="model-badge" :style="{ background: m.badgeColor }">{{ m.badge }}</span>
                <span class="model-name">{{ m.label }}</span>
                <span class="model-desc">{{ m.desc }}</span>
                <span class="model-maker">{{ m.maker }}</span>
              </button>
            </template>
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

            <!-- Video-only params -->
            <template v-if="isVideoTab">
              <div class="param-row">
                <label class="param-label">
                  帧数 (Frames)
                  <span class="param-val">{{ numFrames }}</span>
                </label>
                <input type="range" v-model.number="numFrames" min="17" max="129" step="8" class="param-slider" :disabled="genStore.isGenerating" />
              </div>
              <div class="param-row">
                <label class="param-label">
                  帧率 (FPS)
                  <span class="param-val">{{ fps }}</span>
                </label>
                <input type="range" v-model.number="fps" min="8" max="30" step="2" class="param-slider" :disabled="genStore.isGenerating" />
              </div>
            </template>

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
        <div v-if="latestResult.url" class="gen-result">
          <div class="gen-result-head">
            <span class="gen-result-label">✨ 生成结果 · {{ latestResult.type === 'video' ? '视频' : '图像' }}</span>
            <div class="gen-result-actions">
              <a :href="latestResult.url" :download="latestResult.type === 'video' ? 'deepay-video.mp4' : 'deepay-image.png'" target="_blank" class="result-action-btn">
                ⬇ 下载
              </a>
              <button v-if="latestResult.type === 'image'" class="result-action-btn accent" @click="useInSales">🏪 用于开店</button>
              <button v-if="latestResult.type === 'image'" class="result-action-btn" @click="useAsRefImage">🔄 作为参考图</button>
            </div>
          </div>
          <video v-if="latestResult.type === 'video'" :src="latestResult.url" class="gen-result-video" controls autoplay loop muted playsinline></video>
          <img v-else :src="latestResult.url" class="gen-result-img" alt="AI 生成结果" />
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
            :disabled="!prompt.trim() || apiKeyMissing()"
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
import { generate, cancelJob, MODELS, MODEL_TYPES, getModelsByType, toImageSrc, hasApiKey } from '@/api/runpod.js'

const router   = useRouter()
const route    = useRoute()
const genStore = useImageGenStore()

// ── Gallery state ──────────────────────────────────────
const loading      = ref(true)
const cardsVisible = ref(false)
const searchQuery  = ref('')
const activeFilter = ref('全部')

// ── Generation modal state ─────────────────────────────
const showGenModal   = ref(false)
const genTab         = ref('txt2img')
const selectedModel  = ref('flux-schnell')
const prompt         = ref('')
const negativePrompt = ref('')
const refImageUrl    = ref('')
const selectedSize   = ref('1024x1024')
const steps          = ref(4)
const guidance       = ref(0)
const seed           = ref(-1)
// Video params
const numFrames      = ref(81)
const fps            = ref(16)
// Result: { url, type: 'image'|'video' }
const latestResult   = ref(null)

const sizeOptions = ['512x512', '768x768', '1024x1024', '1024x768', '768x1024', '1280x720', '1280x1280']

// Model groups per tab (used in template)
const modelGroups = computed(() => {
  if (genTab.value === 'txt2img') {
    return [{ label: '图像生成', models: getModelsByType(MODEL_TYPES.TXT2IMG) }]
  }
  if (genTab.value === 'img2img') {
    return [{ label: '图像编辑', models: getModelsByType(MODEL_TYPES.IMG2IMG) }]
  }
  if (genTab.value === 'img2vid') {
    return [{ label: '图生视频', models: getModelsByType(MODEL_TYPES.IMG2VID) }]
  }
  return []
})

const currentModel = computed(() => MODELS[selectedModel.value])

const isVideoTab = computed(() => genTab.value === 'img2vid')

const promptPlaceholder = computed(() => {
  if (genTab.value === 'img2img') return '描述要修改的内容，例如：把领口改成深V领，颜色换成深海蓝，保持模特姿势不变'
  if (genTab.value === 'img2vid') return '描述视频动作，例如：模特缓缓转身，头发随风飘动，背景虚化，时尚走秀风格'
  return '例如：一款高级感秋冬外套，深藏蓝色，简约廓形，欧根纱领，模特平铺拍摄，超清质感，商业摄影'
})

// Switch model when tab changes
watch(genTab, (tab) => {
  const typeMap = { txt2img: MODEL_TYPES.TXT2IMG, img2img: MODEL_TYPES.IMG2IMG, img2vid: MODEL_TYPES.IMG2VID }
  const models  = getModelsByType(typeMap[tab])
  if (models.length && !models.find(m => m.id === selectedModel.value)) {
    selectedModel.value = models[0].id
  }
})

// Sync advanced params when model changes
watch(selectedModel, (id) => {
  const m = MODELS[id]
  if (!m) return
  steps.value    = m.defaults.num_inference_steps
  guidance.value = m.defaults.guidance ?? 0
  if (m.defaults.num_frames) numFrames.value = m.defaults.num_frames
  if (m.defaults.fps)        fps.value        = m.defaults.fps
  genStore.setModel(id)
})

// Pre-fill ref image URL from route query (?img=)
watch(() => route.query.img, (v) => {
  if (v && String(v).startsWith('http')) {
    refImageUrl.value  = String(v)
    genTab.value       = 'img2img'
    showGenModal.value = true
  }
}, { immediate: true })

// ── Gallery data ───────────────────────────────────────
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

const allImages = computed(() => {
  const genItems = genStore.history.map(h => ({
    id:       h.id,
    title:    h.prompt.slice(0, 20) + (h.prompt.length > 20 ? '…' : ''),
    category: 'AI 生成',
    size:     h.type === 'video' ? '视频' : '1024×1024',
    gradient: 'linear-gradient(135deg,#10a37f,#0d5f4e)',
    src:      h.type === 'video' ? null : toImageSrc(h.src),
    video:    h.type === 'video' ? h.src : null,
    isAI:     true,
    prompt:   h.prompt,
    model:    h.model,
    seed:     h.seed,
  }))
  return [...genItems, ...staticImages]
})

const filteredItems = computed(() => {
  let list = allImages.value
  if (activeFilter.value !== '全部') list = list.filter(i => i.category === activeFilter.value)
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(i => i.title.toLowerCase().includes(q) || i.category.toLowerCase().includes(q))
  }
  return list
})

// ── Generation logic ───────────────────────────────────
async function startGeneration() {
  if (!prompt.value.trim()) return
  genStore.reset()
  latestResult.value = null

  const params = {
    prompt:              prompt.value.trim(),
    negative_prompt:     negativePrompt.value.trim(),
    seed:                seed.value,
    num_inference_steps: steps.value,
    guidance:            guidance.value,
    size:                selectedSize.value,
    output_format:       'png',
  }
  if (refImageUrl.value.trim())  params.image      = refImageUrl.value.trim()
  if (isVideoTab.value) {
    params.num_frames = numFrames.value
    params.fps        = fps.value
  }

  try {
    const result = await generate(
      genStore.apiKey,
      selectedModel.value,
      params,
      (status, msg) => genStore.updateProgress(status, msg),
    )

    if (result.type === 'video') {
      genStore.addResult({ images: [result.video], seed: result.seed, prompt: params.prompt, model: selectedModel.value, params, type: 'video' })
      latestResult.value = { url: result.video, type: 'video' }
    } else {
      const imgSrc = toImageSrc(result.images[0])
      genStore.addResult({ images: result.images, seed: result.seed, prompt: params.prompt, model: selectedModel.value, params, type: 'image' })
      latestResult.value = { url: imgSrc, type: 'image' }
    }
  } catch (err) {
    genStore.setError(`❌ ${err.message}`)
  }
}

async function stopGeneration() {
  if (genStore.jobId) {
    try { await cancelJob(genStore.apiKey, selectedModel.value, genStore.jobId) } catch { /* ignore */ }
  }
  genStore.reset()
}

function closeModal() {
  showGenModal.value = false
  if (!genStore.isGenerating) genStore.reset()
}

function useInSales() {
  if (latestResult.value?.url?.startsWith('http')) {
    router.push({ path: '/ai-sales', query: { img: latestResult.value.url } })
  }
}

function useAsRefImage() {
  if (latestResult.value?.url) {
    refImageUrl.value = latestResult.value.url.startsWith('http') ? latestResult.value.url : ''
    genTab.value = 'img2img'
  }
}

function apiKeyMissing() {
  return !genStore.apiKey && !hasApiKey()
}

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
  background: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 200;
  padding: 16px 16px 40px;
  overflow-y: auto;
}

/* ── Generation Modal ───────────────────────────────────── */
.gen-modal {
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 18px;
  padding: 0;
  width: 100%;
  max-width: 640px;
  margin: auto;
  box-shadow: 0 24px 80px rgba(0,0,0,0.5);
  overflow: hidden;
}

.gen-modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 22px 16px;
  border-bottom: 1px solid var(--gpt-border);
}
.gen-modal-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.gen-modal-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--gpt-text);
  margin: 0;
}
.gen-modal-close {
  width: 30px;
  height: 30px;
  background: var(--gpt-sidebar-hover);
  border: none;
  border-radius: 50%;
  color: var(--gpt-text-sub);
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.gen-modal-close:hover { background: var(--gpt-border); }
.gen-modal-close:disabled { opacity: 0.4; cursor: not-allowed; }

/* API key warning */
.api-key-warn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  background: rgba(245,158,11,0.1);
  border-bottom: 1px solid rgba(245,158,11,0.2);
  font-size: 13px;
  color: #d97706;
  flex-wrap: wrap;
}
.warn-link {
  background: none;
  border: none;
  color: #10a37f;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;
}

/* Tabs */
.gen-tabs {
  display: flex;
  gap: 4px;
  padding: 14px 22px 0;
}
.gen-tab {
  padding: 8px 18px;
  background: transparent;
  border: 1px solid var(--gpt-border);
  border-radius: 10px 10px 0 0;
  border-bottom: none;
  color: var(--gpt-text-sub);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.gen-tab:hover { color: var(--gpt-text); background: var(--gpt-sidebar-hover); }
.gen-tab.active {
  background: var(--gpt-main);
  border-color: var(--gpt-border);
  color: #10a37f;
  font-weight: 600;
}

/* Sections */
.gen-section {
  padding: 16px 22px 0;
}
.gen-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--gpt-text-muted);
  margin-bottom: 8px;
}
.gen-label-hint {
  font-size: 11px;
  font-weight: 400;
  text-transform: none;
  letter-spacing: 0;
  color: var(--gpt-text-muted);
  opacity: 0.8;
}
.gen-label-hint.optional { opacity: 0.6; }

/* Model group header */
.model-group-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--gpt-text-muted);
  padding: 8px 4px 4px;
}

/* Model cards */
.model-cards {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--gpt-border);
  border-radius: 10px;
  padding: 6px;
  background: var(--gpt-main);
}
.model-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s;
  width: 100%;
}
.model-card:hover { background: var(--gpt-sidebar-hover); border-color: var(--gpt-border); }
.model-card.selected {
  background: rgba(16,163,127,0.1);
  border-color: rgba(16,163,127,0.4);
}
.model-badge {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 700;
  color: white;
  white-space: nowrap;
  flex-shrink: 0;
  min-width: 36px;
  text-align: center;
}
.model-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--gpt-text);
  flex-shrink: 0;
  min-width: 120px;
}
.model-desc {
  font-size: 12px;
  color: var(--gpt-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.model-maker {
  font-size: 11px;
  color: var(--gpt-text-muted);
  opacity: 0.6;
  margin-left: auto;
  flex-shrink: 0;
}

/* Inputs */
.gen-textarea {
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
  line-height: 1.6;
  transition: border-color 0.2s;
}
.gen-textarea:focus { border-color: #10a37f; }
.gen-textarea::placeholder { color: var(--gpt-text-muted); }
.gen-textarea:disabled { opacity: 0.6; cursor: not-allowed; }

.gen-input {
  width: 100%;
  background: var(--gpt-main);
  border: 1px solid var(--gpt-border);
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 14px;
  color: var(--gpt-text);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.gen-input:focus { border-color: #10a37f; }
.gen-input::placeholder { color: var(--gpt-text-muted); }
.gen-input:disabled { opacity: 0.6; cursor: not-allowed; }

/* Reference image preview */
.ref-preview {
  margin-top: 8px;
  border-radius: 8px;
  overflow: hidden;
  max-height: 160px;
  border: 1px solid var(--gpt-border);
}
.ref-preview img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}

/* Advanced params */
.gen-advanced {
  margin: 12px 22px 0;
  border: 1px solid var(--gpt-border);
  border-radius: 10px;
  overflow: hidden;
}
.gen-advanced-toggle {
  padding: 10px 14px;
  font-size: 12px;
  font-weight: 600;
  color: var(--gpt-text-sub);
  cursor: pointer;
  user-select: none;
  list-style: none;
  background: var(--gpt-main);
}
.gen-advanced-toggle::-webkit-details-marker { display: none; }
.gen-advanced[open] .gen-advanced-toggle { border-bottom: 1px solid var(--gpt-border); }
.gen-advanced-body { padding: 12px 14px; background: var(--gpt-main); }

.param-row {
  margin-bottom: 12px;
}
.param-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--gpt-text-sub);
  margin-bottom: 6px;
}
.param-val {
  font-weight: 700;
  color: #10a37f;
}
.param-slider {
  width: 100%;
  accent-color: #10a37f;
}
.param-slider:disabled { opacity: 0.5; }

.size-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.size-chip {
  padding: 5px 12px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  font-size: 12px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  transition: all 0.15s;
}
.size-chip:hover { border-color: #10a37f; color: #10a37f; }
.size-chip.active { background: #10a37f; border-color: #10a37f; color: white; }

.seed-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.seed-reset {
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.15s;
}
.seed-reset:hover { background: var(--gpt-sidebar-hover); }

/* Video params */
.video-params {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

/* Progress */
.gen-progress-wrap {
  margin: 14px 22px 0;
}
.gen-progress-bar {
  height: 4px;
  background: var(--gpt-border);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 6px;
}
.gen-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #10a37f, #0d8b6e, #10a37f);
  background-size: 200% 100%;
  animation: progress-slide 1.5s ease-in-out infinite;
  border-radius: 2px;
  width: 60%;
}
@keyframes progress-slide {
  0%   { background-position: 200% center; }
  100% { background-position: -200% center; }
}
.gen-progress-bar.error { background: rgba(239,68,68,0.2); }
.gen-progress-text {
  font-size: 12px;
  color: var(--gpt-text-muted);
  margin: 0;
}
.gen-progress-text.error { color: #ef4444; }

/* Result */
.gen-result {
  margin: 14px 22px 0;
  border: 1px solid var(--gpt-border);
  border-radius: 12px;
  overflow: hidden;
  background: var(--gpt-main);
}
.gen-result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid var(--gpt-border);
}
.gen-result-label {
  font-size: 12px;
  font-weight: 600;
  color: #10a37f;
}
.gen-result-actions {
  display: flex;
  gap: 6px;
}
.result-action-btn {
  padding: 5px 12px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  font-size: 12px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  text-decoration: none;
  transition: all 0.15s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.result-action-btn:hover { border-color: #10a37f; color: #10a37f; }
.result-action-btn.accent { background: #10a37f; border-color: #10a37f; color: white; }
.result-action-btn.accent:hover { background: #0d8b6e; }

.gen-result-img {
  width: 100%;
  max-height: 400px;
  object-fit: contain;
  display: block;
  background: #000;
}
.gen-result-video {
  width: 100%;
  max-height: 400px;
  display: block;
  background: #000;
}

/* Footer */
.gen-modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 22px 20px;
  border-top: 1px solid var(--gpt-border);
  margin-top: 16px;
}
.gen-cancel-btn {
  padding: 10px 20px;
  background: transparent;
  border: 1px solid var(--gpt-border);
  border-radius: 10px;
  color: var(--gpt-text-sub);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}
.gen-cancel-btn:hover { background: var(--gpt-sidebar-hover); }
.gen-stop-btn {
  padding: 10px 20px;
  background: rgba(239,68,68,0.1);
  border: 1px solid rgba(239,68,68,0.3);
  border-radius: 10px;
  color: #ef4444;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.gen-stop-btn:hover { background: rgba(239,68,68,0.2); }
.gen-submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  background: #10a37f;
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
}
.gen-submit-btn:hover:not(:disabled) { background: #0d8b6e; transform: translateY(-1px); }
.gen-submit-btn:disabled { opacity: 0.45; cursor: not-allowed; transform: none; }

/* AI badge on card */
.badge-ai { background: rgba(16,163,127,0.7) !important; }
.card-real-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* Transition */
.modal-fade-enter-active,
.modal-fade-leave-active { transition: opacity 0.2s; }
.modal-fade-enter-from,
.modal-fade-leave-to { opacity: 0; }
</style>
