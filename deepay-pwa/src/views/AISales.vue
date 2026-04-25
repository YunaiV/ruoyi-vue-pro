<template>
  <div class="shop-page">

    <!-- ══ HERO BANNER ══ -->
    <div class="hero-banner">
      <div class="hero-glow glow-1"></div>
      <div class="hero-glow glow-2"></div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          AI 驱动 · 智能开店
        </div>
        <h1 class="hero-title">AI <span class="hero-accent">开店</span> 助手</h1>
        <p class="hero-sub">告诉我你的创业想法，AI 帮你从零搭建专属店铺</p>
        <div class="hero-stats">
          <div class="stat-pill">
            <span class="stat-num">12</span>
            <span class="stat-lbl">行业模板</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-pill">
            <span class="stat-num">60s</span>
            <span class="stat-lbl">极速建店</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-pill">
            <span class="stat-num">AI</span>
            <span class="stat-lbl">全程辅助</span>
          </div>
        </div>
      </div>
      <!-- Theme toggle -->
      <button class="theme-btn" @click="themeStore.toggle()" :title="themeStore.isDark ? '切换浅色' : '切换深色'">
        <Transition name="icon-flip" mode="out-in">
          <svg v-if="!themeStore.isDark" key="moon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/></svg>
          <svg v-else key="sun" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
        </Transition>
      </button>
    </div>

    <!-- ══ CHAT INPUT ══ -->
    <section class="chat-section">
      <!-- Messages stream (shows after sending) -->
      <Transition name="slide-up">
        <div v-if="messages.length" class="messages-stream" ref="streamRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-row"
            :class="msg.role"
          >
            <div v-if="msg.role === 'ai'" class="ai-avatar-dot">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a10 10 0 110 20A10 10 0 0112 2zm0 2a8 8 0 100 16A8 8 0 0012 4z"/></svg>
            </div>
            <div class="msg-bubble" :class="msg.role">
              <span v-if="msg.typing" class="typing-dots">
                <span></span><span></span><span></span>
              </span>
              <span v-else>{{ msg.content }}</span>
            </div>
          </div>
        </div>
      </Transition>

      <!-- Input box -->
      <div class="input-wrap" :class="{ focused: inputFocused }">
        <button class="iw-btn" title="附件上传" @click="triggerAttach">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/></svg>
        </button>
        <textarea
          ref="inputRef"
          v-model="inputText"
          class="iw-textarea"
          placeholder="告诉我你的开店想法，或从下方模板快速启动..."
          rows="1"
          @keydown.enter.exact.prevent="sendMessage"
          @input="autoResize"
          @focus="inputFocused = true"
          @blur="inputFocused = false"
        ></textarea>
        <button class="iw-btn" :class="{ recording }" title="语音输入" @click="toggleRecord">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/><path d="M19 10v2a7 7 0 01-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
        </button>
        <button
          class="iw-send"
          :class="{ active: inputText.trim() }"
          :disabled="!inputText.trim() && !recording"
          @click="sendMessage"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
        </button>
      </div>

      <!-- Quick action chips -->
      <div class="chips-row">
        <button
          v-for="chip in actionChips"
          :key="chip.label"
          class="action-chip"
          :class="{ selected: selectedChip === chip.label }"
          @click="selectChip(chip)"
        >
          <span class="chip-icon">{{ chip.icon }}</span>
          {{ chip.label }}
        </button>
        <button class="action-chip nav-chip" @click="router.push('/image-library')">
          <span class="chip-icon">🖼️</span>
          图库素材
        </button>
        <button class="action-chip nav-chip" @click="router.push('/template-library')">
          <span class="chip-icon">📋</span>
          更多模板
        </button>
      </div>
    </section>

    <!-- ══ TEMPLATE LIBRARY ══ -->
    <section class="lib-section">
      <div class="lib-head">
        <div class="lib-head-left">
          <h2 class="lib-title">推荐店铺模板</h2>
          <span class="lib-badge">{{ templates.length }}</span>
        </div>
        <button class="lib-more-btn" @click="router.push('/template-library')">
          全部模板
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M9 18l6-6-6-6"/></svg>
        </button>
        <!-- Search filter -->
        <div class="lib-search">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input v-model="searchQ" class="lib-search-input" placeholder="搜索模板..." />
        </div>
      </div>

      <!-- Category tabs -->
      <div class="cat-tabs">
        <button
          v-for="cat in categories"
          :key="cat"
          class="cat-tab"
          :class="{ active: activeCat === cat }"
          @click="activeCat = cat"
        >{{ cat }}</button>
      </div>

      <!-- Grid -->
      <div class="tpl-grid">
        <TransitionGroup name="tpl-list">
          <div
            v-for="(tpl, i) in filteredTemplates"
            :key="tpl.id"
            class="tpl-card"
            @click="openEditor(tpl)"
          >
            <!-- Glowing banner -->
            <div class="tpl-banner" :style="{ background: tpl.gradient }">
              <div class="banner-shimmer"></div>
              <span class="tpl-emoji">{{ tpl.emoji }}</span>
              <span class="tpl-cat-tag">{{ tpl.cat }}</span>
              <div class="banner-stars">
                <span v-for="n in 3" :key="n" class="bstar" :style="starStyle(tpl.id, n)">✦</span>
              </div>
            </div>
            <!-- Info -->
            <div class="tpl-info">
              <div class="tpl-info-top">
                <h3 class="tpl-name">{{ tpl.name }}</h3>
                <div class="tpl-rating">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                  {{ tpl.rating }}
                </div>
              </div>
              <p class="tpl-desc">{{ tpl.desc }}</p>
              <div class="tpl-footer">
                <span class="tpl-uses">{{ tpl.uses }}人使用</span>
                <span class="tpl-edit-btn">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  编辑
                </span>
              </div>
            </div>
            <!-- Hover glow -->
            <div class="tpl-glow" :style="{ background: tpl.gradient }"></div>
          </div>
        </TransitionGroup>
        <div v-if="!filteredTemplates.length" class="no-result">
          <span>😶‍🌫️ 没有找到匹配的模板</span>
        </div>
      </div>
    </section>

    <!-- ══ EDITOR MODAL ══ -->
    <Transition name="modal">
      <div v-if="showEditor" class="modal-mask" @click.self="closeEditor">
        <div class="modal-panel">
          <!-- Panel glow header -->
          <div class="modal-hero" :style="{ background: editTpl.gradient }">
            <div class="modal-hero-shimmer"></div>
            <span class="modal-hero-emoji">{{ editTpl.emoji }}</span>
            <div class="modal-hero-text">
              <span class="modal-hero-cat">{{ editTpl.cat }}</span>
              <h3 class="modal-hero-name">{{ editTpl.name || '编辑模板' }}</h3>
            </div>
            <button class="modal-close-btn" @click="closeEditor">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M18 6L6 18M6 6l12 12"/></svg>
            </button>
          </div>

          <!-- Form body -->
          <div class="modal-form">
            <div class="mf-field">
              <label class="mf-label">店铺名称</label>
              <input v-model="editTpl.name" class="mf-input" placeholder="输入你的店铺名称..." />
            </div>
            <div class="mf-field">
              <label class="mf-label">店铺介绍</label>
              <textarea v-model="editTpl.desc" class="mf-textarea" rows="3" placeholder="描述你店铺的核心特色和服务..."></textarea>
            </div>
            <div class="mf-row">
              <div class="mf-field" style="flex:1">
                <label class="mf-label">行业分类</label>
                <input v-model="editTpl.cat" class="mf-input" placeholder="如：服装、餐饮..." />
              </div>
              <div class="mf-field" style="flex:1">
                <label class="mf-label">Emoji 图标</label>
                <div class="emoji-row">
                  <span v-for="e in emojiOptions" :key="e" class="emoji-opt" :class="{sel: editTpl.emoji===e}" @click="editTpl.emoji = e">{{ e }}</span>
                </div>
              </div>
            </div>
            <div class="mf-field">
              <label class="mf-label">主题渐变</label>
              <div class="swatch-grid">
                <div
                  v-for="sw in swatches"
                  :key="sw.label"
                  class="swatch"
                  :class="{ sel: editTpl.gradient === sw.value }"
                  :style="{ background: sw.value }"
                  :title="sw.label"
                  @click="editTpl.gradient = sw.value"
                >
                  <svg v-if="editTpl.gradient === sw.value" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="modal-actions">
            <button class="ma-cancel" @click="closeEditor">取消</button>
            <button class="ma-use" @click="useTemplate">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M5 3l14 9-14 9V3z"/></svg>
              立即使用
            </button>
            <button class="ma-save" @click="saveTemplate">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
              保存修改
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ══ TOAST ══ -->
    <Transition name="toast">
      <div v-if="toast.show" class="toast-bar" :class="toast.type">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
        {{ toast.text }}
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/store/index.js'

/* ── Theme ── */
const themeStore = useThemeStore()
const router = useRouter()

/* ── Input state ── */
const inputRef = ref(null)
const inputText = ref('')
const inputFocused = ref(false)
const recording = ref(false)
const selectedChip = ref(null)
const messages = ref([])
const streamRef = ref(null)
let msgId = 0

/* ── Action chips ── */
const actionChips = [
  { icon: '🖼️', label: '生成图片',  fill: '帮我生成一张店铺封面图，风格现代简约' },
  { icon: '✏️', label: '撰写/编辑', fill: '帮我写一段吸引人的店铺介绍文案' },
  { icon: '🔍', label: '查找资料',  fill: '查找适合我行业的竞品分析和市场报告' },
  { icon: '🚀', label: '开店方案',  fill: '帮我规划一套完整的开店方案，包括选品、定价和推广' },
]

function selectChip(chip) {
  selectedChip.value = chip.label
  inputText.value = chip.fill
  nextTick(() => inputRef.value?.focus())
}

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  messages.value.push({ id: ++msgId, role: 'user', content: text })
  inputText.value = ''
  selectedChip.value = null
  if (inputRef.value) { inputRef.value.style.height = 'auto' }
  await nextTick()
  scrollStream()
  // Simulated AI reply
  const aiMsg = { id: ++msgId, role: 'ai', content: '', typing: true }
  messages.value.push(aiMsg)
  await nextTick(); scrollStream()
  setTimeout(() => {
    aiMsg.typing = false
    aiMsg.content = `好的！我正在为你生成「${text.slice(0,12)}${text.length>12?'…':''}」的相关方案，请稍等...`
  }, 1400)
}

function scrollStream() {
  nextTick(() => streamRef.value?.scrollTo({ top: streamRef.value.scrollHeight, behavior: 'smooth' }))
}

function toggleRecord() { recording.value = !recording.value }
function triggerAttach() { console.log('[shop] attach') }

/* ── Templates ── */
const templates = ref([
  { id:1,  emoji:'👗', name:'时尚服装店',   cat:'服装', desc:'潮流服饰，个性化搭配推荐，引领时尚风向标',   rating:'4.9', uses:'2.3k', gradient:'linear-gradient(135deg,#10a37f 0%,#065f46 100%)' },
  { id:2,  emoji:'☕', name:'精品咖啡馆',   cat:'餐饮', desc:'手工精品咖啡，舒适慢生活，打造第三空间',     rating:'4.8', uses:'1.8k', gradient:'linear-gradient(135deg,#d97706 0%,#92400e 100%)' },
  { id:3,  emoji:'🌸', name:'浪漫花艺店',   cat:'花艺', desc:'鲜花定制配送，节日礼盒，传递最真挚的情意',   rating:'4.9', uses:'3.1k', gradient:'linear-gradient(135deg,#ec4899 0%,#9d174d 100%)' },
  { id:4,  emoji:'📚', name:'文艺书店',     cat:'文化', desc:'精选好书与文创周边，构建温暖的阅读社区',     rating:'4.7', uses:'987',  gradient:'linear-gradient(135deg,#3b82f6 0%,#1e3a8a 100%)' },
  { id:5,  emoji:'💻', name:'科技工作室',   cat:'科技', desc:'数码产品销售与技术咨询，你的极客伙伴',       rating:'4.8', uses:'1.2k', gradient:'linear-gradient(135deg,#06b6d4 0%,#164e63 100%)' },
  { id:6,  emoji:'🍰', name:'烘焙甜品坊',   cat:'美食', desc:'每日新鲜出炉，手工甜点带来幸福的味道',       rating:'4.9', uses:'4.5k', gradient:'linear-gradient(135deg,#f97316 0%,#9a3412 100%)' },
  { id:7,  emoji:'🐾', name:'宠物生活馆',   cat:'宠物', desc:'宠物用品、美容护理与寄养服务一站式满足',     rating:'4.8', uses:'2.7k', gradient:'linear-gradient(135deg,#8b5cf6 0%,#4c1d95 100%)' },
  { id:8,  emoji:'💪', name:'健身工作室',   cat:'健康', desc:'私教课程预约，科学健身规划，塑造理想体型',   rating:'4.7', uses:'1.5k', gradient:'linear-gradient(135deg,#10b981 0%,#064e3b 100%)' },
  { id:9,  emoji:'🎨', name:'艺术画廊',     cat:'艺术', desc:'原创艺术品展览销售，定期策展活动邀你参与',   rating:'4.9', uses:'876',  gradient:'linear-gradient(135deg,#f43f5e 0%,#7c3aed 100%)' },
  { id:10, emoji:'🛋️', name:'家居生活馆',   cat:'家居', desc:'软装搭配设计与品质好物精选，打造理想居所',   rating:'4.8', uses:'3.2k', gradient:'linear-gradient(135deg,#0ea5e9 0%,#1d4ed8 100%)' },
  { id:11, emoji:'💄', name:'美妆工作室',   cat:'美妆', desc:'专业美妆教程，精选产品测评与一对一咨询',     rating:'4.9', uses:'5.1k', gradient:'linear-gradient(135deg,#f472b6 0%,#db2777 100%)' },
  { id:12, emoji:'✂️', name:'手作创意坊',   cat:'创意', desc:'手工 DIY 课程套件，激发创造力的快乐工坊',   rating:'4.7', uses:'1.1k', gradient:'linear-gradient(135deg,#84cc16 0%,#166534 100%)' },
])

const categories = computed(() => ['全部', ...new Set(templates.value.map(t => t.cat))])
const activeCat = ref('全部')
const searchQ = ref('')
const filteredTemplates = computed(() => {
  let list = templates.value
  if (activeCat.value !== '全部') list = list.filter(t => t.cat === activeCat.value)
  if (searchQ.value.trim()) {
    const q = searchQ.value.toLowerCase()
    list = list.filter(t => t.name.includes(q) || t.desc.includes(q) || t.cat.includes(q))
  }
  return list
})

/* ── Editor ── */
const showEditor = ref(false)
const editTpl = reactive({})
const swatches = [
  { label:'绿色',  value:'linear-gradient(135deg,#10a37f 0%,#065f46 100%)' },
  { label:'蓝色',  value:'linear-gradient(135deg,#3b82f6 0%,#1e3a8a 100%)' },
  { label:'紫色',  value:'linear-gradient(135deg,#8b5cf6 0%,#4c1d95 100%)' },
  { label:'玫红',  value:'linear-gradient(135deg,#ec4899 0%,#9d174d 100%)' },
  { label:'橙色',  value:'linear-gradient(135deg,#f97316 0%,#9a3412 100%)' },
  { label:'黄色',  value:'linear-gradient(135deg,#eab308 0%,#713f12 100%)' },
  { label:'青色',  value:'linear-gradient(135deg,#06b6d4 0%,#164e63 100%)' },
  { label:'深夜',  value:'linear-gradient(135deg,#374151 0%,#111827 100%)' },
  { label:'极光',  value:'linear-gradient(135deg,#f43f5e 0%,#7c3aed 100%)' },
  { label:'草绿',  value:'linear-gradient(135deg,#84cc16 0%,#166534 100%)' },
]
const emojiOptions = ['👗','☕','🌸','📚','💻','🍰','🐾','💪','🎨','🛋️','💄','✂️','🏪','🍜','🎵','📷','🌿','🍷']

function starStyle(id, n) {
  // Deterministic positions based on template id + star index (no Math.random in template)
  const x = ((id * 37 + n * 97) % 70) + 5
  const y = ((id * 53 + n * 61) % 70) + 5
  return { '--d': (n * 0.3) + 's', '--x': x + '%', '--y': y + '%' }
}
function closeEditor() { showEditor.value = false }

function saveTemplate() {
  const idx = templates.value.findIndex(t => t.id === editTpl.id)
  if (idx !== -1) templates.value.splice(idx, 1, { ...editTpl })
  closeEditor()
  showToastMsg('✅ 模板已保存', 'success')
}

function useTemplate() {
  closeEditor()
  inputText.value = `帮我基于「${editTpl.name}」模板，生成完整的开店方案`
  nextTick(() => inputRef.value?.focus())
  showToastMsg('🚀 已加载模板，发送即可开始', 'info')
}

/* ── Toast ── */
const toast = reactive({ show: false, text: '', type: 'success' })
let toastTimer = null
function showToastMsg(text, type = 'success') {
  clearTimeout(toastTimer)
  Object.assign(toast, { show: true, text, type })
  toastTimer = setTimeout(() => { toast.show = false }, 2500)
}
</script>

<style scoped>
/* ══ ROOT ══ */
.shop-page {
  --page-bg:    var(--gpt-main);
  --surface:    var(--gpt-input-bg);
  --surface2:   var(--dp-surface2);
  --border:     var(--gpt-border);
  --text:       var(--gpt-text);
  --text-sub:   var(--gpt-text-sub);
  --text-muted: var(--gpt-text-muted);
  --accent:     var(--gpt-accent);
  --accent-hov: var(--gpt-accent-hover);
  --chip-bg:    var(--dp-chip-bg);
  --shadow:     var(--dp-shadow);
  --shadow-lg:  var(--dp-shadow-lg);
  background: var(--page-bg);
  color: var(--text);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  min-height: 100vh;
  padding: 0 0 60px;
  position: relative;
}

/* ══ HERO ══ */
.hero-banner {
  position: relative;
  padding: 40px 32px 36px;
  overflow: hidden;
  border-bottom: 1px solid var(--border);
}
.hero-glow {
  position: absolute; border-radius: 50%; filter: blur(80px); pointer-events: none;
}
.glow-1 { width: 300px; height: 300px; background: rgba(16,163,127,0.18); top: -80px; left: -60px; }
.glow-2 { width: 250px; height: 250px; background: rgba(59,130,246,0.12); top: -40px; right: -40px; }

.hero-content { position: relative; z-index: 1; }
.hero-badge {
  display: inline-flex; align-items: center; gap: 7px;
  font-size: 12px; font-weight: 600; letter-spacing: 0.3px;
  background: rgba(16,163,127,0.15); color: var(--accent);
  border: 1px solid rgba(16,163,127,0.25);
  padding: 4px 12px; border-radius: 20px; margin-bottom: 14px;
}
.badge-dot {
  width: 7px; height: 7px; border-radius: 50%; background: var(--accent);
  animation: pulse-dot 1.8s ease-in-out infinite;
}
@keyframes pulse-dot {
  0%,100% { transform: scale(1); opacity: 1; }
  50%      { transform: scale(1.4); opacity: 0.6; }
}

.hero-title {
  font-size: 32px; font-weight: 700; letter-spacing: -0.5px;
  color: var(--text); margin: 0 0 8px; line-height: 1.2;
}
.hero-accent {
  background: linear-gradient(135deg,#10a37f,#3b82f6);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.hero-sub { font-size: 14px; color: var(--text-sub); margin: 0 0 20px; }

.hero-stats { display: flex; align-items: center; gap: 12px; }
.stat-pill { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.stat-num { font-size: 18px; font-weight: 700; color: var(--accent); }
.stat-lbl { font-size: 11px; color: var(--text-muted); }
.stat-divider { width: 1px; height: 28px; background: var(--border); }

.theme-btn {
  position: absolute; top: 24px; right: 24px; z-index: 2;
  width: 38px; height: 38px;
  display: flex; align-items: center; justify-content: center;
  background: var(--surface); border: 1px solid var(--border);
  color: var(--text-sub); border-radius: 50%; cursor: pointer;
  transition: all 0.2s;
}
.theme-btn:hover { color: var(--text); border-color: var(--accent); transform: rotate(20deg); }

.icon-flip-enter-active,
.icon-flip-leave-active { transition: all 0.2s; }
.icon-flip-enter-from   { transform: rotate(-90deg) scale(0.5); opacity: 0; }
.icon-flip-leave-to     { transform: rotate(90deg) scale(0.5); opacity: 0; }

/* ══ CHAT SECTION ══ */
.chat-section { padding: 24px 28px 0; display: flex; flex-direction: column; gap: 12px; }

/* Messages stream */
.messages-stream {
  max-height: 220px; overflow-y: auto;
  display: flex; flex-direction: column; gap: 12px;
  padding: 4px 0 8px;
  scrollbar-width: thin; scrollbar-color: var(--border) transparent;
}
.msg-row { display: flex; align-items: flex-start; gap: 10px; }
.msg-row.user { justify-content: flex-end; }
.msg-row.ai   { justify-content: flex-start; }
.ai-avatar-dot {
  width: 26px; height: 26px; border-radius: 50%; flex-shrink: 0;
  background: var(--accent); color: #fff;
  display: flex; align-items: center; justify-content: center;
}
.msg-bubble {
  max-width: 75%; font-size: 13px; line-height: 1.55;
  padding: 10px 14px; border-radius: 14px;
}
.msg-bubble.user {
  background: var(--accent); color: #fff; border-bottom-right-radius: 4px;
}
.msg-bubble.ai {
  background: var(--surface); color: var(--text); border-bottom-left-radius: 4px;
  border: 1px solid var(--border);
}
.typing-dots { display: flex; gap: 4px; align-items: center; }
.typing-dots span {
  width: 6px; height: 6px; border-radius: 50%; background: var(--text-sub);
  animation: bounce 1.2s ease-in-out infinite;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,80%,100% { transform: translateY(0); } 40% { transform: translateY(-5px); } }

.slide-up-enter-active { transition: all 0.3s ease-out; }
.slide-up-enter-from   { opacity: 0; transform: translateY(16px); }

/* Input wrap */
.input-wrap {
  display: flex; align-items: flex-end; gap: 4px;
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: 18px; padding: 8px 10px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.input-wrap.focused {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(16,163,127,0.13);
}

.iw-btn {
  width: 34px; height: 34px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: transparent; border: none; color: var(--text-sub);
  border-radius: 9px; cursor: pointer; transition: all 0.15s;
}
.iw-btn:hover         { background: rgba(255,255,255,0.07); color: var(--text); }
.iw-btn.recording     { color: #ef4444; animation: pulse-dot 1.2s infinite; }

.iw-textarea {
  flex: 1; background: transparent; border: none; outline: none;
  font-size: 14px; color: var(--text); resize: none; line-height: 1.5;
  padding: 6px 4px; min-height: 22px; max-height: 160px; overflow-y: auto;
  font-family: inherit;
}
.iw-textarea::placeholder { color: var(--text-sub); }

.iw-send {
  width: 34px; height: 34px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.08); border: none;
  border-radius: 9px; cursor: pointer; color: var(--text-muted);
  transition: all 0.2s;
}
.iw-send.active { background: var(--accent); color: #fff; box-shadow: 0 2px 12px rgba(16,163,127,0.4); }
.iw-send:disabled { cursor: default; }

/* Chips */
.chips-row { display: flex; gap: 8px; flex-wrap: wrap; }
.action-chip {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 14px; font-size: 13px; font-weight: 500;
  background: var(--chip-bg); border: 1px solid var(--border);
  color: var(--text-sub); border-radius: 20px; cursor: pointer;
  transition: all 0.18s;
}
.action-chip:hover    { background: rgba(255,255,255,0.1); color: var(--text); border-color: var(--accent); }
.action-chip.selected { background: rgba(16,163,127,0.15); color: var(--accent); border-color: var(--accent); }
.action-chip.nav-chip { border-style: dashed; opacity: 0.8; }
.action-chip.nav-chip:hover { opacity: 1; border-style: solid; }
.chip-icon { font-size: 14px; }

/* ══ LIBRARY ══ */
.lib-section { padding: 28px 28px 0; }
.lib-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 14px;
}
.lib-head-left  { display: flex; align-items: center; gap: 10px; }
.lib-title      { font-size: 16px; font-weight: 600; color: var(--text); margin: 0; }
.lib-badge {
  font-size: 11px; font-weight: 600;
  background: var(--accent); color: #fff;
  padding: 2px 8px; border-radius: 20px;
}
.lib-more-btn {
  display: flex; align-items: center; gap: 5px;
  padding: 5px 12px; background: transparent;
  border: 1px solid var(--border); border-radius: 20px;
  color: var(--text-sub); font-size: 12px; font-weight: 500; cursor: pointer;
  transition: all 0.15s;
}
.lib-more-btn:hover { border-color: var(--accent); color: var(--accent); }

.lib-search {
  display: flex; align-items: center; gap: 7px;
  background: var(--surface); border: 1px solid var(--border);
  border-radius: 20px; padding: 6px 12px;
  transition: border-color 0.2s;
}
.lib-search:focus-within { border-color: var(--accent); }
.lib-search svg { color: var(--text-muted); flex-shrink: 0; }
.lib-search-input {
  background: transparent; border: none; outline: none;
  font-size: 13px; color: var(--text); width: 130px;
}
.lib-search-input::placeholder { color: var(--text-muted); }

/* Category tabs */
.cat-tabs { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 18px; }
.cat-tab {
  padding: 5px 14px; font-size: 12px; font-weight: 500;
  background: transparent; border: 1px solid var(--border);
  color: var(--text-sub); border-radius: 20px; cursor: pointer;
  transition: all 0.18s;
}
.cat-tab:hover  { border-color: var(--accent); color: var(--text); }
.cat-tab.active { background: var(--accent); color: #fff; border-color: var(--accent); }

/* Grid */
.tpl-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 16px;
}
.no-result {
  grid-column: 1/-1; text-align: center;
  padding: 40px; color: var(--text-sub); font-size: 14px;
}

/* Template card */
.tpl-card {
  position: relative;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px; overflow: hidden; cursor: pointer;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s, border-color 0.22s;
}
.tpl-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 16px 40px rgba(0,0,0,0.45);
  border-color: rgba(16,163,127,0.4);
}
.tpl-card:hover .tpl-glow { opacity: 1; }

.tpl-banner {
  height: 118px; position: relative;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.banner-shimmer {
  position: absolute; inset: 0;
  background: linear-gradient(105deg,transparent 40%,rgba(255,255,255,0.12) 50%,transparent 60%);
  background-size: 200% 100%;
  animation: shimmer 3s infinite;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.tpl-emoji { font-size: 40px; filter: drop-shadow(0 3px 8px rgba(0,0,0,0.4)); z-index: 1; }
.tpl-cat-tag {
  position: absolute; top: 10px; right: 10px; z-index: 2;
  font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;
  background: rgba(0,0,0,0.35); color: rgba(255,255,255,0.9);
  padding: 3px 8px; border-radius: 20px; backdrop-filter: blur(6px);
}
.banner-stars { position: absolute; inset: 0; pointer-events: none; }
.bstar {
  position: absolute; font-size: 9px; color: rgba(255,255,255,0.5);
  left: var(--x); top: var(--y);
  animation: twinkle 2s var(--d) ease-in-out infinite alternate;
}
@keyframes twinkle { from { opacity: 0.2; transform: scale(0.8); } to { opacity: 0.8; transform: scale(1.2); } }

.tpl-info { padding: 14px; }
.tpl-info-top { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 5px; }
.tpl-name { font-size: 14px; font-weight: 600; color: var(--text); margin: 0; }
.tpl-rating {
  display: flex; align-items: center; gap: 3px;
  font-size: 11px; font-weight: 600; color: #f59e0b;
}
.tpl-desc {
  font-size: 12px; color: var(--text-sub); line-height: 1.5;
  margin: 0 0 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.tpl-footer { display: flex; align-items: center; justify-content: space-between; }
.tpl-uses { font-size: 11px; color: var(--text-muted); }
.tpl-edit-btn {
  display: flex; align-items: center; gap: 4px;
  font-size: 11px; font-weight: 500; color: var(--accent);
  padding: 3px 8px; border-radius: 20px;
  background: rgba(16,163,127,0.1); transition: background 0.15s;
}
.tpl-card:hover .tpl-edit-btn { background: rgba(16,163,127,0.2); }

.tpl-glow {
  position: absolute; inset: 0; border-radius: 16px;
  opacity: 0; transition: opacity 0.3s;
  mix-blend-mode: screen; pointer-events: none;
  filter: blur(20px); transform: scale(1.1);
}

/* TransitionGroup */
.tpl-list-enter-active { transition: all 0.35s ease-out; }
.tpl-list-leave-active { transition: all 0.2s ease-in; }
.tpl-list-enter-from   { opacity: 0; transform: translateY(14px) scale(0.96); }
.tpl-list-leave-to     { opacity: 0; transform: scale(0.95); }

/* ══ MODAL ══ */
.modal-mask {
  position: fixed; inset: 0; z-index: 400;
  background: rgba(0,0,0,0.65); backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center; padding: 20px;
}
.modal-panel {
  width: 100%; max-width: 520px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 20px; overflow: hidden;
  box-shadow: var(--shadow-lg);
  max-height: 90vh; display: flex; flex-direction: column;
}

.modal-hero {
  position: relative; padding: 28px 22px;
  display: flex; align-items: center; gap: 14px; overflow: hidden;
}
.modal-hero-shimmer {
  position: absolute; inset: 0;
  background: linear-gradient(105deg,transparent 30%,rgba(255,255,255,0.1) 50%,transparent 70%);
  background-size: 200% 100%; animation: shimmer 2.5s infinite;
}
.modal-hero-emoji { font-size: 44px; filter: drop-shadow(0 3px 10px rgba(0,0,0,0.4)); z-index: 1; }
.modal-hero-text  { z-index: 1; }
.modal-hero-cat   { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; color: rgba(255,255,255,0.65); margin-bottom: 3px; display: block; }
.modal-hero-name  { font-size: 18px; font-weight: 700; color: #fff; margin: 0; text-shadow: 0 2px 8px rgba(0,0,0,0.3); }
.modal-close-btn  {
  position: absolute; top: 14px; right: 14px; z-index: 2;
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.25); border: none; color: rgba(255,255,255,0.8);
  border-radius: 50%; cursor: pointer; transition: background 0.15s;
}
.modal-close-btn:hover { background: rgba(0,0,0,0.45); color: #fff; }

.modal-form {
  padding: 20px 22px;
  display: flex; flex-direction: column; gap: 16px;
  overflow-y: auto; flex: 1;
}
.mf-field  { display: flex; flex-direction: column; gap: 6px; }
.mf-row    { display: flex; gap: 14px; }
.mf-label  { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.4px; color: var(--text-sub); }
.mf-input, .mf-textarea {
  background: var(--page-bg); border: 1px solid var(--border);
  border-radius: 10px; color: var(--text); font-size: 14px; font-family: inherit;
  padding: 10px 13px; outline: none; resize: vertical;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.mf-input:focus, .mf-textarea:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(16,163,127,0.12);
}

.emoji-row { display: flex; gap: 5px; flex-wrap: wrap; }
.emoji-opt {
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  font-size: 18px; border-radius: 8px; cursor: pointer;
  background: var(--page-bg); border: 1px solid var(--border);
  transition: all 0.15s;
}
.emoji-opt:hover { border-color: var(--accent); transform: scale(1.12); }
.emoji-opt.sel   { border-color: var(--accent); background: rgba(16,163,127,0.12); transform: scale(1.12); }

.swatch-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.swatch {
  width: 36px; height: 36px; border-radius: 9px; cursor: pointer;
  border: 2px solid transparent;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.15s, border-color 0.15s;
}
.swatch:hover { transform: scale(1.15); }
.swatch.sel   { border-color: rgba(255,255,255,0.7); transform: scale(1.15); box-shadow: 0 0 0 2px rgba(255,255,255,0.2); }

.modal-actions {
  display: flex; gap: 8px; padding: 16px 22px;
  border-top: 1px solid var(--border); justify-content: flex-end;
}
.ma-cancel, .ma-use, .ma-save {
  display: flex; align-items: center; gap: 6px;
  padding: 9px 18px; font-size: 13px; font-weight: 500;
  border: none; border-radius: 10px; cursor: pointer;
  transition: all 0.18s;
}
.ma-cancel { background: var(--chip-bg); color: var(--text-sub); }
.ma-cancel:hover { color: var(--text); }
.ma-use {
  background: rgba(59,130,246,0.15); color: #60a5fa;
  border: 1px solid rgba(59,130,246,0.25);
}
.ma-use:hover { background: rgba(59,130,246,0.25); }
.ma-save {
  background: var(--accent); color: #fff;
  box-shadow: 0 2px 12px rgba(16,163,127,0.35);
}
.ma-save:hover { background: var(--accent-hov); box-shadow: 0 4px 20px rgba(16,163,127,0.5); }
.ma-cancel:active, .ma-use:active, .ma-save:active { transform: scale(0.96); }

/* Modal transition */
.modal-enter-active { transition: opacity 0.22s ease-out; }
.modal-leave-active { transition: opacity 0.18s ease-in; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal-panel { transform: scale(0.94) translateY(14px); }
.modal-leave-to .modal-panel   { transform: scale(0.96) translateY(8px); }

/* ══ TOAST ══ */
.toast-bar {
  position: fixed; bottom: 28px; left: 50%; transform: translateX(-50%);
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; font-weight: 500;
  padding: 10px 20px; border-radius: 20px;
  z-index: 500; pointer-events: none; white-space: nowrap;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}
.toast-bar.success { background: var(--accent); color: #fff; box-shadow: 0 4px 20px rgba(16,163,127,0.4); }
.toast-bar.info    { background: #3b82f6;        color: #fff; box-shadow: 0 4px 20px rgba(59,130,246,0.4); }

.toast-enter-active { transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); }
.toast-leave-active { transition: all 0.2s ease-in; }
.toast-enter-from   { opacity: 0; transform: translateX(-50%) translateY(14px); }
.toast-leave-to     { opacity: 0; transform: translateX(-50%) translateY(8px); }

/* ══ RESPONSIVE ══ */
@media (max-width: 640px) {
  .hero-banner  { padding: 28px 18px 24px; }
  .hero-title   { font-size: 24px; }
  .chat-section,
  .lib-section  { padding-left: 16px; padding-right: 16px; }
  .tpl-grid     { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
  .tpl-banner   { height: 90px; }
  .tpl-emoji    { font-size: 30px; }
  .mf-row       { flex-direction: column; }
  .lib-head     { flex-direction: column; align-items: flex-start; gap: 10px; }
}
</style>
