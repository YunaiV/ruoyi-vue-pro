<template>
  <div class="shop-page">

    <!-- ══ PAGE HEADER ══ -->
    <div class="shop-header">
      <div class="header-left">
        <div class="header-icon">🏪</div>
        <div>
          <h1 class="page-title">AI 开店助手</h1>
          <p class="page-sub">告诉我你的开店想法，或选择一个模板快速启动</p>
        </div>
      </div>
    </div>

    <!-- ══ CHAT INPUT AREA ══ -->
    <section class="chat-section">
      <!-- Input box -->
      <div class="input-box" :class="{ focused: inputFocused }">
        <button class="ib-icon-btn" title="附件上传">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/></svg>
        </button>
        <input
          v-model="userInput"
          class="ib-input"
          placeholder="告诉我你的开店想法，或选择一个模板开始..."
          @keydown.enter.exact.prevent="sendMessage"
          @focus="inputFocused = true"
          @blur="inputFocused = false"
        />
        <button class="ib-icon-btn" title="语音输入">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/><path d="M19 10v2a7 7 0 01-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
        </button>
        <button class="ib-send-btn" :class="{ active: userInput.trim() }" :disabled="!userInput.trim()" @click="sendMessage">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
        </button>
      </div>

      <!-- Quick action chips -->
      <div class="quick-chips">
        <button class="qc-chip" @click="fillInput('帮我生成一张店铺封面图')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
          生成图片
        </button>
        <button class="qc-chip" @click="fillInput('帮我撰写一段店铺介绍文案')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          撰写/编辑
        </button>
        <button class="qc-chip" @click="fillInput('查找适合我店铺的行业资料和竞品分析')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          查找资料
        </button>
        <button class="qc-chip" @click="fillInput('帮我规划一个完整的开店方案')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          开店方案
        </button>
      </div>
    </section>

    <!-- ══ TEMPLATE LIBRARY ══ -->
    <section class="templates-section">
      <div class="section-head">
        <h2 class="section-title">推荐店铺模板</h2>
        <span class="section-count">{{ templates.length }} 个模板</span>
      </div>

      <div class="templates-grid">
        <div
          v-for="(tpl, i) in templates"
          :key="tpl.id"
          class="tpl-card"
          :style="{ '--enter-delay': (i * 40) + 'ms' }"
          @click="openEditor(tpl)"
        >
          <!-- Preview banner -->
          <div class="tpl-banner" :style="{ background: tpl.gradient }">
            <span class="tpl-emoji">{{ tpl.emoji }}</span>
            <div class="tpl-tag">{{ tpl.tag }}</div>
          </div>
          <!-- Info -->
          <div class="tpl-info">
            <h3 class="tpl-name">{{ tpl.name }}</h3>
            <p class="tpl-desc">{{ tpl.desc }}</p>
          </div>
          <!-- Hover overlay -->
          <div class="tpl-hover-overlay">
            <span class="tpl-edit-hint">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              点击编辑
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ EDITOR MODAL ══ -->
    <Transition name="modal">
      <div v-if="showEditor" class="modal-backdrop" @click.self="closeEditor">
        <div class="modal-box">
          <!-- Modal header -->
          <div class="modal-head">
            <div class="modal-head-left">
              <span class="modal-emoji">{{ editingTpl.emoji }}</span>
              <div>
                <h3 class="modal-title">编辑模板</h3>
                <p class="modal-sub">{{ editingTpl.tag }}</p>
              </div>
            </div>
            <button class="modal-close" @click="closeEditor">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6L6 18M6 6l12 12"/></svg>
            </button>
          </div>

          <!-- Modal body -->
          <div class="modal-body">
            <div class="form-field">
              <label class="field-label">店铺名称</label>
              <input v-model="editingTpl.name" class="field-input" placeholder="输入店铺名称" />
            </div>
            <div class="form-field">
              <label class="field-label">店铺介绍</label>
              <textarea v-model="editingTpl.desc" class="field-textarea" rows="3" placeholder="描述你的店铺特色..."></textarea>
            </div>
            <div class="form-field">
              <label class="field-label">行业标签</label>
              <input v-model="editingTpl.tag" class="field-input" placeholder="如：服装、餐饮、美妆..." />
            </div>
            <div class="form-field">
              <label class="field-label">主题渐变色</label>
              <div class="color-row">
                <div
                  v-for="preset in colorPresets"
                  :key="preset.label"
                  class="color-swatch"
                  :class="{ selected: editingTpl.gradient === preset.value }"
                  :style="{ background: preset.value }"
                  :title="preset.label"
                  @click="editingTpl.gradient = preset.value"
                ></div>
              </div>
            </div>
          </div>

          <!-- Modal footer -->
          <div class="modal-foot">
            <button class="foot-btn secondary" @click="closeEditor">取消</button>
            <button class="foot-btn primary" @click="saveTemplate">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
              保存修改
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ══ SAVE TOAST ══ -->
    <Transition name="toast">
      <div v-if="showToast" class="save-toast">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
        模板已保存
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

/* ── State ── */
const userInput = ref('')
const inputFocused = ref(false)
const showEditor = ref(false)
const showToast = ref(false)
const editingTpl = reactive({})

/* ── Color presets ── */
const colorPresets = [
  { label: '绿色', value: 'linear-gradient(135deg,#10a37f,#0d6b5a)' },
  { label: '蓝色', value: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
  { label: '紫色', value: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
  { label: '玫红', value: 'linear-gradient(135deg,#ec4899,#be185d)' },
  { label: '橙色', value: 'linear-gradient(135deg,#f97316,#c2410c)' },
  { label: '黄色', value: 'linear-gradient(135deg,#eab308,#a16207)' },
  { label: '青色', value: 'linear-gradient(135deg,#06b6d4,#0e7490)' },
  { label: '深灰', value: 'linear-gradient(135deg,#374151,#111827)' },
]

/* ── Templates ── */
const templates = ref([
  { id:1,  emoji:'👗', name:'时尚服装店',   desc:'潮流服饰，个性化搭配推荐，引领时尚风向',    tag:'服装',   gradient:'linear-gradient(135deg,#10a37f,#0d6b5a)' },
  { id:2,  emoji:'☕', name:'精品咖啡馆',   desc:'手工精品咖啡，舒适空间，美好时光',          tag:'餐饮',   gradient:'linear-gradient(135deg,#eab308,#a16207)' },
  { id:3,  emoji:'🌸', name:'浪漫花店',     desc:'鲜花定制与配送，传递浪漫与心意',            tag:'花艺',   gradient:'linear-gradient(135deg,#ec4899,#be185d)' },
  { id:4,  emoji:'📚', name:'文艺书店',     desc:'精选好书与文创周边，构建阅读社区',          tag:'文化',   gradient:'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
  { id:5,  emoji:'💻', name:'科技工作室',   desc:'数码产品销售与技术咨询服务',                tag:'科技',   gradient:'linear-gradient(135deg,#06b6d4,#0e7490)' },
  { id:6,  emoji:'��', name:'美食烘焙坊',   desc:'新鲜手工烘焙，每日限量出品的美味甜点',      tag:'美食',   gradient:'linear-gradient(135deg,#f97316,#c2410c)' },
  { id:7,  emoji:'🐾', name:'宠物生活馆',   desc:'宠物用品、美容护理与寄养服务一站式',        tag:'宠物',   gradient:'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
  { id:8,  emoji:'💪', name:'健身工作室',   desc:'私教课程预约，健康生活方式指导',            tag:'健康',   gradient:'linear-gradient(135deg,#10a37f,#065f46)' },
  { id:9,  emoji:'🎨', name:'艺术画廊',     desc:'原创艺术品展览与销售，定期举办艺术活动',    tag:'艺术',   gradient:'linear-gradient(135deg,#ec4899,#7c3aed)' },
  { id:10, emoji:'🛋️', name:'家居生活馆',   desc:'软装搭配设计与品质家居好物精选',            tag:'家居',   gradient:'linear-gradient(135deg,#06b6d4,#3b82f6)' },
  { id:11, emoji:'💄', name:'美妆工作室',   desc:'美妆教程分享，精选产品推荐与试用体验',      tag:'美妆',   gradient:'linear-gradient(135deg,#f472b6,#ec4899)' },
  { id:12, emoji:'✂️', name:'手作工坊',     desc:'手工DIY课程与创意材料套件，激发创造力',    tag:'创意',   gradient:'linear-gradient(135deg,#84cc16,#15803d)' },
])

/* ── Methods ── */
function fillInput(text) {
  userInput.value = text
}

function sendMessage() {
  if (!userInput.value.trim()) return
  console.log('[AI Shop] send:', userInput.value)
  userInput.value = ''
}

function openEditor(tpl) {
  Object.assign(editingTpl, { ...tpl })
  showEditor.value = true
}

function closeEditor() {
  showEditor.value = false
}

function saveTemplate() {
  const idx = templates.value.findIndex(t => t.id === editingTpl.id)
  if (idx !== -1) {
    templates.value[idx] = { ...editingTpl }
  }
  closeEditor()
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 2200)
}
</script>

<style scoped>
.shop-page {
  padding: 24px 28px 40px;
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 28px;
  color: var(--gpt-text);
}

/* ── Header ── */
.shop-header { display: flex; align-items: center; justify-content: space-between; }
.header-left { display: flex; align-items: center; gap: 14px; }
.header-icon { font-size: 32px; line-height: 1; }
.page-title { font-size: 20px; font-weight: 600; color: var(--gpt-text); margin: 0 0 2px; }
.page-sub   { font-size: 13px; color: var(--gpt-text-sub); margin: 0; }

/* ── Chat Section ── */
.chat-section { display: flex; flex-direction: column; gap: 12px; }

.input-box {
  display: flex;
  align-items: center;
  gap: 4px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 16px;
  padding: 6px 8px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.input-box.focused {
  border-color: var(--gpt-accent);
  box-shadow: 0 0 0 3px rgba(16,163,127,0.12);
}

.ib-icon-btn {
  width: 34px; height: 34px;
  display: flex; align-items: center; justify-content: center;
  background: transparent; border: none;
  color: var(--gpt-text-sub); border-radius: 8px; cursor: pointer;
  transition: background 0.15s, color 0.15s; flex-shrink: 0;
}
.ib-icon-btn:hover { background: rgba(255,255,255,0.07); color: var(--gpt-text); }

.ib-input {
  flex: 1; background: transparent; border: none; outline: none;
  font-size: 14px; color: var(--gpt-text); padding: 6px 4px;
}
.ib-input::placeholder { color: var(--gpt-text-sub); }

.ib-send-btn {
  width: 34px; height: 34px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.08); border: none;
  border-radius: 8px; cursor: pointer; color: var(--gpt-text-sub);
  transition: all 0.2s; flex-shrink: 0;
}
.ib-send-btn.active { background: var(--gpt-accent); color: #fff; }
.ib-send-btn:disabled { cursor: default; }

/* ── Quick Chips ── */
.quick-chips { display: flex; gap: 8px; flex-wrap: wrap; }
.qc-chip {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 14px; font-size: 13px; font-weight: 500;
  background: var(--dp-chip-bg); border: 1px solid var(--dp-chip-border);
  color: var(--gpt-text-sub); border-radius: 20px; cursor: pointer;
  transition: all 0.2s;
}
.qc-chip:hover { background: var(--gpt-sidebar-hover); color: var(--gpt-text); border-color: var(--gpt-accent); }

/* ── Templates Section ── */
.section-head {
  display: flex; align-items: baseline; gap: 10px; margin-bottom: 16px;
}
.section-title { font-size: 16px; font-weight: 600; color: var(--gpt-text); margin: 0; }
.section-count  { font-size: 12px; color: var(--gpt-text-muted); }

.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.tpl-card {
  position: relative;
  background: var(--dp-surface2);
  border: 1px solid var(--gpt-border);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.22s, box-shadow 0.22s, border-color 0.22s;
  animation: cardEnter 0.4s var(--enter-delay, 0ms) both ease-out;
}
.tpl-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.4);
  border-color: var(--gpt-accent);
}
.tpl-card:hover .tpl-hover-overlay { opacity: 1; }

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

.tpl-banner {
  height: 120px;
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.tpl-emoji { font-size: 42px; filter: drop-shadow(0 2px 6px rgba(0,0,0,0.3)); }
.tpl-tag {
  position: absolute; top: 10px; right: 10px;
  font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;
  background: rgba(0,0,0,0.35); color: rgba(255,255,255,0.9);
  padding: 3px 8px; border-radius: 20px; backdrop-filter: blur(4px);
}

.tpl-info { padding: 14px; }
.tpl-name { font-size: 14px; font-weight: 600; color: var(--gpt-text); margin: 0 0 5px; }
.tpl-desc { font-size: 12px; color: var(--gpt-text-sub); line-height: 1.5; margin: 0; }

.tpl-hover-overlay {
  position: absolute; inset: 0;
  background: rgba(16,163,127,0.12);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
  border-radius: 14px;
}
.tpl-edit-hint {
  display: flex; align-items: center; gap: 6px;
  background: var(--gpt-accent); color: #fff;
  font-size: 13px; font-weight: 500;
  padding: 8px 18px; border-radius: 20px;
  box-shadow: 0 4px 16px rgba(16,163,127,0.4);
}

/* ── Modal ── */
.modal-backdrop {
  position: fixed; inset: 0; z-index: 400;
  background: rgba(0,0,0,0.6); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 20px;
}

.modal-box {
  width: 100%; max-width: 500px;
  background: var(--dp-surface2);
  border: 1px solid var(--gpt-border);
  border-radius: 18px;
  overflow: hidden;
  box-shadow: var(--dp-shadow-lg);
}

.modal-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid var(--gpt-border);
}
.modal-head-left { display: flex; align-items: center; gap: 12px; }
.modal-emoji { font-size: 28px; }
.modal-title { font-size: 15px; font-weight: 600; color: var(--gpt-text); margin: 0 0 2px; }
.modal-sub   { font-size: 12px; color: var(--gpt-text-sub); margin: 0; }
.modal-close {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: transparent; border: none;
  color: var(--gpt-text-sub); border-radius: 8px; cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.modal-close:hover { background: rgba(255,255,255,0.07); color: var(--gpt-text); }

.modal-body { padding: 20px; display: flex; flex-direction: column; gap: 16px; max-height: 60vh; overflow-y: auto; }

.form-field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 12px; font-weight: 600; color: var(--gpt-text-sub); text-transform: uppercase; letter-spacing: 0.4px; }
.field-input, .field-textarea {
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 10px;
  color: var(--gpt-text);
  font-size: 14px; font-family: inherit;
  padding: 10px 14px; outline: none; resize: vertical;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.field-input:focus, .field-textarea:focus {
  border-color: var(--gpt-accent);
  box-shadow: 0 0 0 3px rgba(16,163,127,0.12);
}

.color-row { display: flex; gap: 8px; flex-wrap: wrap; }
.color-swatch {
  width: 32px; height: 32px; border-radius: 8px; cursor: pointer;
  border: 2px solid transparent; transition: transform 0.15s, border-color 0.15s;
}
.color-swatch:hover { transform: scale(1.12); }
.color-swatch.selected { border-color: var(--gpt-accent); transform: scale(1.12); box-shadow: 0 0 0 2px rgba(16,163,127,0.3); }

.modal-foot {
  display: flex; align-items: center; justify-content: flex-end; gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid var(--gpt-border);
}
.foot-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 9px 18px; font-size: 14px; font-weight: 500;
  border: none; border-radius: 10px; cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}
.foot-btn:active { transform: scale(0.97); }
.foot-btn.secondary { background: rgba(255,255,255,0.06); color: var(--gpt-text-sub); }
.foot-btn.secondary:hover { background: rgba(255,255,255,0.1); color: var(--gpt-text); }
.foot-btn.primary { background: var(--gpt-accent); color: #fff; }
.foot-btn.primary:hover { background: var(--gpt-accent-hover); }

/* ── Toast ── */
.save-toast {
  position: fixed; bottom: 28px; left: 50%; transform: translateX(-50%);
  display: flex; align-items: center; gap: 8px;
  background: var(--gpt-accent); color: #fff;
  font-size: 13px; font-weight: 500;
  padding: 10px 20px; border-radius: 20px;
  box-shadow: 0 4px 20px rgba(16,163,127,0.4);
  z-index: 500; pointer-events: none;
}

/* ── Transitions ── */
.modal-enter-active  { transition: all 0.22s ease-out; }
.modal-leave-active  { transition: all 0.18s ease-in; }
.modal-enter-from    { opacity: 0; }
.modal-leave-to      { opacity: 0; }
.modal-enter-from .modal-box { transform: scale(0.94) translateY(12px); }
.modal-leave-to .modal-box   { transform: scale(0.94) translateY(8px); }

.toast-enter-active  { transition: all 0.3s cubic-bezier(0.175,0.885,0.32,1.275); }
.toast-leave-active  { transition: all 0.2s ease-in; }
.toast-enter-from    { opacity: 0; transform: translateX(-50%) translateY(12px); }
.toast-leave-to      { opacity: 0; transform: translateX(-50%) translateY(8px); }

/* ── Responsive ── */
@media (max-width: 640px) {
  .shop-page { padding: 16px 16px 32px; gap: 20px; }
  .templates-grid { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
  .tpl-banner { height: 90px; }
  .tpl-emoji { font-size: 32px; }
  .quick-chips { gap: 6px; }
  .qc-chip { padding: 6px 10px; font-size: 12px; }
}
</style>
