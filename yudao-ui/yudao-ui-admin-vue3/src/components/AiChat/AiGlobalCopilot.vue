<template>
  <Teleport to="body">
    <!-- ══════════════════════════════════════════════════════
         全局 AI Copilot — 悬浮按钮（关闭时）
    ══════════════════════════════════════════════════════ -->
    <Transition name="fab">
      <button
        v-if="!open"
        class="ai-fab"
        :class="{ 'ai-fab--loading': chat.loading.value }"
        :title="fabTitle"
        @click="openCopilot"
        aria-label="打开 AI Copilot"
      >
        <span class="ai-fab__icon">✨</span>
        <span v-if="unreadCount > 0" class="ai-fab__badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
      </button>
    </Transition>

    <!-- ══════════════════════════════════════════════════════
         蒙层（移动端）
    ══════════════════════════════════════════════════════ -->
    <Transition name="overlay">
      <div v-if="open" class="ai-copilot-backdrop" @click="closeCopilot" />
    </Transition>

    <!-- ══════════════════════════════════════════════════════
         聊天侧边栏抽屉
    ══════════════════════════════════════════════════════ -->
    <Transition name="copilot-drawer">
      <div v-if="open" class="ai-copilot-drawer" role="dialog" aria-label="AI Copilot">

        <!-- ── 标题栏 ─────────────────────────────────────── -->
        <header class="ai-copilot-header">
          <div class="ai-copilot-header__info">
            <span class="ai-copilot-header__icon">✨</span>
            <div>
              <div class="ai-copilot-header__title">AI Copilot</div>
              <div class="ai-copilot-header__status">
                <span class="status-dot" :class="statusDotClass" />
                {{ statusLabel }}
              </div>
            </div>
          </div>
          <div class="ai-copilot-header__actions">
            <button class="hbtn" title="清除对话" @click="clearChat">🗑️</button>
            <button class="hbtn" title="关闭" @click="closeCopilot">✕</button>
          </div>
        </header>

        <!-- ── 模块快切标签 ──────────────────────────────── -->
        <div class="ai-copilot-tabs">
          <button
            v-for="tab in MODULE_TABS"
            :key="tab.key"
            class="tab-btn"
            :class="{ 'tab-btn--active': currentModule === tab.key }"
            @click="switchModule(tab.key)"
          >{{ tab.label }}</button>
        </div>

        <!-- ── 消息列表 ──────────────────────────────────── -->
        <div class="ai-copilot-messages" ref="messagesEl">
          <div
            v-for="msg in chat.messages.value"
            :key="msg.id"
            class="msg-row"
            :class="`msg-row--${msg.role}`"
          >
            <div class="msg-bubble" :class="{ 'msg-bubble--streaming': msg.streaming }">
              <span v-if="msg.role === 'ai'" class="msg-avatar">✨</span>
              <div class="msg-content">
                <p class="msg-text">{{ msg.content }}<span v-if="msg.streaming" class="cursor-blink">|</span></p>
                <!-- 快捷回复 -->
                <div v-if="msg.quickReplies?.length" class="quick-replies">
                  <button
                    v-for="qr in msg.quickReplies"
                    :key="qr"
                    class="qr-btn"
                    @click="chat.quickReply(qr)"
                  >{{ qr }}</button>
                </div>
                <!-- 图片 -->
                <div v-if="msg.images?.length" class="msg-images">
                  <img
                    v-for="(url, i) in msg.images"
                    :key="i"
                    :src="url"
                    class="msg-img"
                    loading="lazy"
                    :alt="`图片 ${i + 1}`"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 加载中占位 -->
          <div v-if="chat.loading.value && !hasStreamingMsg" class="msg-row msg-row--ai">
            <div class="msg-bubble">
              <span class="msg-avatar">✨</span>
              <div class="msg-content">
                <p class="msg-text typing-dots"><span>•</span><span>•</span><span>•</span></p>
              </div>
            </div>
          </div>

          <div ref="bottomAnchorEl" />
        </div>

        <!-- ── 输入框 ────────────────────────────────────── -->
        <div class="ai-copilot-input-area">
          <!-- 自动补全建议 -->
          <div v-if="chat.suggestions.value.length" class="suggestions">
            <button
              v-for="s in chat.suggestions.value"
              :key="s"
              class="suggestion-btn"
              @click="selectSuggestion(s)"
            >{{ s }}</button>
          </div>

          <div class="input-row">
            <textarea
              ref="inputEl"
              v-model="inputText"
              class="ai-copilot-input"
              :placeholder="inputPlaceholder"
              :disabled="chat.loading.value"
              rows="1"
              @keydown.enter.exact.prevent="handleSend"
              @keydown.enter.shift.exact="() => {}"
              @input="handleInput"
            />
            <button
              class="send-btn"
              :disabled="!inputText.trim() || chat.loading.value"
              :title="chat.loading.value ? '停止' : '发送'"
              @click="chat.loading.value ? chat.cancel() : handleSend()"
            >
              <span>{{ chat.loading.value ? '⏹' : '➤' }}</span>
            </button>
          </div>
        </div>

      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useAiChat } from '@/composables/useAiChat'
import type { AiChatContext } from '@/api/ai/chat'

// ── 模块标签配置 ────────────────────────────────────────────
const MODULE_TABS = [
  { key: 'global',    label: '全站' },
  { key: 'selection', label: '选款' },
  { key: 'design',    label: '设计' },
  { key: 'product',   label: '商品' },
  { key: 'order',     label: '订单' },
  { key: 'trend',     label: '趋势' },
  { key: 'finance',   label: '财务' },
] as const

type ModuleKey = (typeof MODULE_TABS)[number]['key']

// ── State ───────────────────────────────────────────────────
const open          = ref(false)
const currentModule = ref<ModuleKey>('global')
const inputText     = ref('')
const unreadCount   = ref(0)
const messagesEl    = ref<HTMLElement>()
const bottomAnchorEl = ref<HTMLElement>()
const inputEl       = ref<HTMLTextAreaElement>()

const route = useRoute()

// ── Context supplier — 每次发消息时注入当前路由上下文 ──────
function buildContext(): AiChatContext {
  return {
    route:  route?.path ?? '',
    module: currentModule.value,
  }
}

// ── Chat composable ─────────────────────────────────────────
const chat = useAiChat({
  get module() { return currentModule.value },
  scrollEl:   () => messagesEl.value,
  persistKey: 'ai_copilot_global',
  contextFn:  buildContext,
  greeting:   '你好！我是 AI Copilot，随时可以帮你处理选款、设计、订单、财务等问题，快来问我吧 ✨',
})

// ── 计算属性 ─────────────────────────────────────────────────
const hasStreamingMsg = computed(() =>
  chat.messages.value.some(m => m.streaming)
)

const statusLabel = computed(() => {
  if (chat.loading.value) return '思考中…'
  return '在线'
})

const statusDotClass = computed(() => ({
  'status-dot--active':   !chat.loading.value,
  'status-dot--thinking':  chat.loading.value,
}))

const fabTitle = computed(() => unreadCount.value > 0
  ? `AI Copilot（${unreadCount.value} 条新消息）`
  : 'AI Copilot'
)

const inputPlaceholder = computed(() =>
  `向 AI 提问（${MODULE_TABS.find(t => t.key === currentModule.value)?.label ?? 'AI'}）…`
)

// ── 监听路由变化 → 自动推断模块 ─────────────────────────────
watch(
  () => route?.path,
  (path) => {
    if (!path) return
    const found = MODULE_TABS.find(t => t.key !== 'global' && path.includes(t.key))
    if (found) currentModule.value = found.key
  },
  { immediate: true }
)

// ── 监听新 AI 消息 → 悬浮按钮未读角标 ──────────────────────
watch(
  () => chat.messages.value.length,
  (newLen, oldLen) => {
    if (!open.value && newLen > oldLen) {
      const last = chat.messages.value[newLen - 1]
      if (last?.role === 'ai') unreadCount.value++
    }
  }
)

// ── Actions ──────────────────────────────────────────────────
function openCopilot() {
  open.value = true
  unreadCount.value = 0
  nextTick(() => {
    inputEl.value?.focus()
    scrollToBottom()
    if (chat.messages.value.length === 0) {
      chat.showGreeting()
    }
  })
}

function closeCopilot() {
  open.value = false
}

function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''
  chat.clearSuggestions()
  chat.send(text)
  resizeTextarea()
}

function handleInput() {
  resizeTextarea()
  chat.updateSuggestions(inputText.value)
}

function selectSuggestion(text: string) {
  inputText.value = text
  chat.clearSuggestions()
  handleSend()
}

function clearChat() {
  chat.endSession()
  nextTick(() => chat.showGreeting())
}

function switchModule(key: ModuleKey) {
  currentModule.value = key
}

function scrollToBottom() {
  nextTick(() => {
    if (bottomAnchorEl.value) {
      bottomAnchorEl.value.scrollIntoView({ behavior: 'smooth' })
    }
  })
}

function resizeTextarea() {
  nextTick(() => {
    if (!inputEl.value) return
    inputEl.value.style.height = 'auto'
    inputEl.value.style.height = Math.min(inputEl.value.scrollHeight, 120) + 'px'
  })
}
</script>

<style scoped>
/* ── 悬浮按钮 ──────────────────────────────────────────── */
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9998;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s;
}
.ai-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 28px rgba(99, 102, 241, 0.55);
}
.ai-fab--loading {
  animation: pulse-fab 1.4s ease-in-out infinite;
}
.ai-fab__icon { font-size: 22px; line-height: 1; }
.ai-fab__badge {
  position: absolute;
  top: 2px; right: 2px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  padding: 0 4px;
}

/* ── 蒙层 ──────────────────────────────────────────────── */
.ai-copilot-backdrop {
  position: fixed;
  inset: 0;
  z-index: 9998;
  background: rgba(0, 0, 0, 0.25);
}

/* ── 抽屉 ──────────────────────────────────────────────── */
.ai-copilot-drawer {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 9999;
  width: 380px;
  max-width: 100vw;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.12);
}

/* ── 标题栏 ──────────────────────────────────────────────*/
.ai-copilot-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  flex-shrink: 0;
}
.ai-copilot-header__info { display: flex; align-items: center; gap: 10px; }
.ai-copilot-header__icon { font-size: 22px; }
.ai-copilot-header__title { font-size: 15px; font-weight: 700; }
.ai-copilot-header__status { font-size: 11px; opacity: 0.85; display: flex; align-items: center; gap: 5px; margin-top: 2px; }
.ai-copilot-header__actions { display: flex; gap: 4px; }

.status-dot {
  width: 7px; height: 7px; border-radius: 50%;
  display: inline-block;
}
.status-dot--active   { background: #4ade80; }
.status-dot--thinking { background: #fbbf24; animation: blink 1s ease-in-out infinite; }

.hbtn {
  background: rgba(255,255,255,0.15);
  border: none;
  color: #fff;
  width: 28px; height: 28px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s;
}
.hbtn:hover { background: rgba(255,255,255,0.28); }

/* ── 模块标签 ───────────────────────────────────────────*/
.ai-copilot-tabs {
  display: flex;
  gap: 6px;
  padding: 8px 12px;
  flex-wrap: wrap;
  flex-shrink: 0;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}
.tab-btn {
  padding: 3px 10px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  font-size: 12px;
  cursor: pointer;
  color: #374151;
  transition: all 0.15s;
}
.tab-btn--active {
  background: #6366f1;
  border-color: #6366f1;
  color: #fff;
}

/* ── 消息列表 ───────────────────────────────────────────*/
.ai-copilot-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.msg-row { display: flex; }
.msg-row--user { justify-content: flex-end; }
.msg-row--ai   { justify-content: flex-start; }

.msg-bubble {
  display: flex;
  gap: 8px;
  max-width: 88%;
}
.msg-avatar { font-size: 18px; flex-shrink: 0; padding-top: 2px; }
.msg-content { display: flex; flex-direction: column; gap: 6px; }
.msg-text {
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13.5px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}
.msg-row--ai   .msg-text { background: #f3f4f6; color: #1f2937; border-bottom-left-radius: 4px; }
.msg-row--user .msg-text { background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff; border-bottom-right-radius: 4px; }

.msg-bubble--streaming .msg-text { background: #ede9fe; }
.cursor-blink { animation: blink 0.9s step-end infinite; margin-left: 1px; }

.quick-replies { display: flex; flex-wrap: wrap; gap: 5px; }
.qr-btn {
  padding: 3px 10px;
  border-radius: 12px;
  border: 1px solid #6366f1;
  background: #fff;
  color: #6366f1;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.qr-btn:hover { background: #6366f1; color: #fff; }

.msg-images { display: flex; flex-wrap: wrap; gap: 6px; }
.msg-img { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; cursor: pointer; }

.typing-dots { display: flex; gap: 4px; align-items: center; padding: 10px 14px !important; }
.typing-dots span {
  display: inline-block;
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #9ca3af;
  animation: typing 1.2s ease-in-out infinite;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

/* ── 输入区 ─────────────────────────────────────────────*/
.ai-copilot-input-area {
  flex-shrink: 0;
  padding: 10px 12px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}
.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 6px;
}
.suggestion-btn {
  padding: 2px 10px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  font-size: 12px;
  cursor: pointer;
  color: #374151;
  transition: all 0.15s;
}
.suggestion-btn:hover { border-color: #6366f1; color: #6366f1; }

.input-row { display: flex; gap: 8px; align-items: flex-end; }
.ai-copilot-input {
  flex: 1;
  padding: 9px 12px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 13.5px;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
  line-height: 1.5;
  min-height: 38px;
  max-height: 120px;
  overflow-y: auto;
  font-family: inherit;
}
.ai-copilot-input:focus { border-color: #6366f1; }
.ai-copilot-input:disabled { background: #f9fafb; color: #9ca3af; cursor: not-allowed; }

.send-btn {
  width: 38px; height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 16px;
  display: flex; align-items: center; justify-content: center;
  transition: opacity 0.2s;
  flex-shrink: 0;
}
.send-btn:disabled { opacity: 0.45; cursor: not-allowed; }

/* ── Transitions ────────────────────────────────────────*/
.fab-enter-active,
.fab-leave-active { transition: transform 0.25s, opacity 0.25s; }
.fab-enter-from,
.fab-leave-to { transform: scale(0.5); opacity: 0; }

.overlay-enter-active,
.overlay-leave-active { transition: opacity 0.25s; }
.overlay-enter-from,
.overlay-leave-to { opacity: 0; }

.copilot-drawer-enter-active,
.copilot-drawer-leave-active { transition: transform 0.28s cubic-bezier(.4,0,.2,1); }
.copilot-drawer-enter-from,
.copilot-drawer-leave-to { transform: translateX(100%); }

/* ── Keyframes ──────────────────────────────────────────*/
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
@keyframes typing { 0%, 60%, 100% { transform: translateY(0); } 30% { transform: translateY(-4px); } }
@keyframes pulse-fab { 0%, 100% { box-shadow: 0 4px 20px rgba(99,102,241,0.4); } 50% { box-shadow: 0 4px 32px rgba(99,102,241,0.7); } }
</style>
