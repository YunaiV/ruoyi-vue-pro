<template>
  <!-- 悬浮气泡按钮 -->
  <div class="ai-chat-fab" @click="open = true" v-if="!open" :title="`AI 助手 · ${moduleName}`">
    <span class="ai-chat-fab__icon">🤖</span>
    <span class="ai-chat-fab__label">AI 助手</span>
  </div>

  <!-- 聊天抽屉 -->
  <div class="ai-chat-drawer" :class="{ 'ai-chat-drawer--open': open }">
    <!-- 标题栏 -->
    <div class="ai-chat-header">
      <span class="ai-chat-header__title">🤖 AI 助手 · {{ moduleName }}</span>
      <button class="ai-chat-header__close" @click="handleClose">✕</button>
    </div>

    <!-- 消息列表 -->
    <div class="ai-chat-messages" ref="messagesEl">
      <template v-for="(msg, idx) in messages" :key="idx">
        <!-- AI 消息气泡 -->
        <div v-if="msg.role === 'ai'" class="ai-chat-bubble ai-chat-bubble--ai">
          <div class="ai-chat-bubble__avatar">🤖</div>
          <div class="ai-chat-bubble__body">
            <div class="ai-chat-bubble__text" v-html="renderText(msg.content)"></div>

            <!-- 图片卡片 -->
            <div v-if="msg.images && msg.images.length" class="ai-chat-images">
              <img
                v-for="(url, i) in msg.images"
                :key="i"
                :src="url"
                class="ai-chat-images__item"
                @click="$emit('imageClick', url)"
                :alt="`推荐款 ${i + 1}`"
              />
            </div>

            <!-- 快速回复按钮 -->
            <div v-if="msg.quickReplies && msg.quickReplies.length" class="ai-chat-quick-replies">
              <button
                v-for="reply in msg.quickReplies"
                :key="reply"
                class="ai-chat-quick-btn"
                @click="handleQuickReply(reply)"
                :disabled="loading"
              >{{ reply }}</button>
            </div>
          </div>
        </div>

        <!-- 用户消息气泡 -->
        <div v-else class="ai-chat-bubble ai-chat-bubble--user">
          <div class="ai-chat-bubble__body">
            <div class="ai-chat-bubble__text">{{ msg.content }}</div>
          </div>
          <div class="ai-chat-bubble__avatar">👤</div>
        </div>
      </template>

      <!-- 正在输入指示 -->
      <div v-if="loading" class="ai-chat-bubble ai-chat-bubble--ai">
        <div class="ai-chat-bubble__avatar">🤖</div>
        <div class="ai-chat-bubble__body">
          <div class="ai-chat-typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="ai-chat-input-area">
      <!-- 自动补全下拉 -->
      <ul v-if="suggestions.length" class="ai-chat-suggestions">
        <li
          v-for="s in suggestions"
          :key="s"
          @click="applySuggestion(s)"
          class="ai-chat-suggestions__item"
        >{{ s }}</li>
      </ul>

      <div class="ai-chat-input-row">
        <input
          ref="inputEl"
          v-model="inputText"
          class="ai-chat-input"
          :placeholder="inputPlaceholder"
          @keydown.enter.prevent="handleSend"
          @input="handleInputChange"
          :disabled="loading"
          maxlength="200"
        />
        <button
          class="ai-chat-send-btn"
          @click="handleSend"
          :disabled="loading || !inputText.trim()"
        >
          <span v-if="!loading">发送</span>
          <span v-else>…</span>
        </button>
      </div>
    </div>
  </div>

  <!-- 背景蒙层（移动端） -->
  <div v-if="open" class="ai-chat-overlay" @click="open = false"></div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onUnmounted } from 'vue'
import { AiChatApi, type ChatReply } from '@/api/ai/chat'

// ---- Props ----
const props = withDefaults(defineProps<{
  /** 板块标识：selection / design / product / inventory / finance / trend / order */
  module?: string
  /** 用户/客户 ID */
  customerId?: number
  /** 开场白（首次打开时 AI 主动说的话） */
  greeting?: string
}>(), {
  module: 'selection',
  customerId: undefined,
  greeting: '你好！我是 AI 助手，直接告诉我你想做什么，例如：「我想做一件极简外套」'
})

// ---- Emits ----
const emit = defineEmits<{
  (e: 'imageClick', url: string): void
  (e: 'done', sessionId: string): void
}>()

// ---- 模块名称映射 ----
const MODULE_NAMES: Record<string, string> = {
  selection: '选款',
  design:    '设计',
  product:   '商品',
  inventory: '库存',
  finance:   '财务',
  trend:     '趋势',
  order:     '订单'
}

const moduleName = computed(() => MODULE_NAMES[props.module] ?? '助手')

const inputPlaceholders: Record<string, string> = {
  selection: '例如：我想做欧美风极简外套…',
  design:    '例如：帮我设计一款宽松工装外套…',
  product:   '例如：外套定什么价合适？',
  inventory: '例如：外套库存还有多少？',
  finance:   '例如：这款的 ROI 是多少？',
  trend:     '例如：最近什么款最火？',
  order:     '例如：查一下我的最新订单'
}
const inputPlaceholder = computed(() => inputPlaceholders[props.module] ?? '请输入…')

// ---- 状态 ----
const open        = ref(false)
const loading     = ref(false)
const inputText   = ref('')
const sessionId   = ref<string | undefined>(undefined)
const messagesEl  = ref<HTMLElement>()
const inputEl     = ref<HTMLInputElement>()
const suggestions = ref<string[]>([])

interface Message {
  role: 'ai' | 'user'
  content: string
  images?: string[]
  quickReplies?: string[]
}

const messages = ref<Message[]>([])

// ---- 开场白（首次打开） ----
watch(open, (val) => {
  if (val && messages.value.length === 0) {
    messages.value.push({
      role: 'ai',
      content: props.greeting
    })
  }
  if (val) {
    nextTick(() => inputEl.value?.focus())
  }
})

// ---- 发送消息 ----
async function handleSend() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  suggestions.value = []
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value   = true
  scrollToBottom()

  try {
    const reply: ChatReply = await AiChatApi.sendMessage({
      module:      props.module,
      sessionId:   sessionId.value,
      customerId:  props.customerId,
      userMessage: text
    })

    sessionId.value = reply.sessionId

    messages.value.push({
      role:         'ai',
      content:      reply.aiMessage,
      images:       reply.images,
      quickReplies: reply.quickReplies
    })

    if (reply.done) {
      emit('done', reply.sessionId)
    }
  } catch (err: any) {
    messages.value.push({
      role:    'ai',
      content: '抱歉，网络出现了点小问题，请稍后重试。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
    nextTick(() => inputEl.value?.focus())
  }
}

// ---- 快速回复按钮 ----
function handleQuickReply(reply: string) {
  // 提取括号前的中文（如 "欧洲(EU)" → "EU"，"少女" → "少女"）
  const cleaned = reply.replace(/^(.+?)\((.+?)\)$/, '$2')
  inputText.value = cleaned || reply
  handleSend()
}

// ---- 自动补全 ----
let debounceTimer: ReturnType<typeof setTimeout> | null = null

async function handleInputChange() {
  const text = inputText.value.trim()
  if (!text) {
    suggestions.value = []
    return
  }
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(async () => {
    try {
      suggestions.value = await AiChatApi.autocomplete(text)
    } catch {
      suggestions.value = []
    }
  }, 200)
}

function applySuggestion(s: string) {
  inputText.value = s
  suggestions.value = []
  inputEl.value?.focus()
}

// ---- 关闭 ----
async function handleClose() {
  open.value = false
  if (sessionId.value) {
    try {
      await AiChatApi.endSession(sessionId.value)
    } catch { /* ignore */ }
    sessionId.value = undefined
  }
  messages.value = []
}

// ---- 工具 ----
function scrollToBottom() {
  nextTick(() => {
    if (messagesEl.value) {
      messagesEl.value.scrollTop = messagesEl.value.scrollHeight
    }
  })
}

function renderText(text: string): string {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br/>')
}

onUnmounted(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
})
</script>

<style scoped>
/* ---- 悬浮气泡按钮 ---- */
.ai-chat-fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  padding: 12px 18px;
  border-radius: 50px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.45);
  user-select: none;
  z-index: 1000;
  transition: transform 0.2s, box-shadow 0.2s;
}
.ai-chat-fab:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(99, 102, 241, 0.55);
}
.ai-chat-fab__icon { font-size: 20px; }
.ai-chat-fab__label { font-size: 14px; font-weight: 600; }

/* ---- 抽屉 ---- */
.ai-chat-drawer {
  position: fixed;
  bottom: 0;
  right: 0;
  width: 400px;
  max-width: 100vw;
  height: 580px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 16px 16px 0 0;
  box-shadow: -4px 0 40px rgba(0,0,0,0.18);
  z-index: 1001;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.ai-chat-drawer--open { transform: translateY(0); }

/* ---- 标题栏 ---- */
.ai-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border-radius: 16px 16px 0 0;
  flex-shrink: 0;
}
.ai-chat-header__title { font-size: 15px; font-weight: 700; }
.ai-chat-header__close {
  background: none;
  border: none;
  color: rgba(255,255,255,0.8);
  font-size: 18px;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}
.ai-chat-header__close:hover { color: #fff; }

/* ---- 消息列表 ---- */
.ai-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f8f9fc;
}

/* ---- 气泡 ---- */
.ai-chat-bubble {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 90%;
}
.ai-chat-bubble--user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-chat-bubble__avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #e0e7ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.ai-chat-bubble--user .ai-chat-bubble__avatar {
  background: #d1fae5;
}

.ai-chat-bubble__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ai-chat-bubble__text {
  background: #fff;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: #1f2937;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  word-break: break-word;
}
.ai-chat-bubble--user .ai-chat-bubble__text {
  background: #6366f1;
  color: #fff;
  border-radius: 12px 4px 12px 12px;
}
.ai-chat-bubble--ai .ai-chat-bubble__text {
  border-radius: 4px 12px 12px 12px;
}

/* ---- 图片卡片 ---- */
.ai-chat-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 300px;
}
.ai-chat-images__item {
  width: 90px;
  height: 110px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.15s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}
.ai-chat-images__item:hover { transform: scale(1.04); }

/* ---- 快速回复 ---- */
.ai-chat-quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.ai-chat-quick-btn {
  background: #ede9fe;
  color: #5b21b6;
  border: 1px solid #c4b5fd;
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.ai-chat-quick-btn:hover:not(:disabled) {
  background: #ddd6fe;
}
.ai-chat-quick-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ---- 打字动画 ---- */
.ai-chat-typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 4px 12px 12px 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.ai-chat-typing span {
  width: 7px;
  height: 7px;
  background: #a5b4fc;
  border-radius: 50%;
  animation: typing-bounce 1.2s infinite;
}
.ai-chat-typing span:nth-child(2) { animation-delay: 0.2s; }
.ai-chat-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing-bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40%           { transform: translateY(-6px); }
}

/* ---- 输入区域 ---- */
.ai-chat-input-area {
  padding: 12px 14px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  position: relative;
  flex-shrink: 0;
}
.ai-chat-input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.ai-chat-input {
  flex: 1;
  border: 1.5px solid #e0e7ff;
  border-radius: 24px;
  padding: 10px 16px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  color: #1f2937;
}
.ai-chat-input:focus { border-color: #6366f1; }
.ai-chat-input:disabled { background: #f9f9f9; }
.ai-chat-send-btn {
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 24px;
  padding: 10px 18px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}
.ai-chat-send-btn:hover:not(:disabled) { background: #4f46e5; }
.ai-chat-send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ---- 自动补全 ---- */
.ai-chat-suggestions {
  position: absolute;
  bottom: 100%;
  left: 14px;
  right: 14px;
  background: #fff;
  border: 1px solid #e0e7ff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  list-style: none;
  margin: 0 0 4px;
  padding: 6px 0;
  z-index: 10;
  max-height: 200px;
  overflow-y: auto;
}
.ai-chat-suggestions__item {
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  color: #374151;
}
.ai-chat-suggestions__item:hover { background: #f5f3ff; }

/* ---- 蒙层 ---- */
.ai-chat-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.3);
  z-index: 1000;
}

/* ---- 响应式：移动端全宽 ---- */
@media (max-width: 480px) {
  .ai-chat-drawer {
    width: 100vw;
    border-radius: 16px 16px 0 0;
  }
  .ai-chat-overlay { display: block; }
  .ai-chat-fab { right: 16px; bottom: 16px; }
}
</style>
