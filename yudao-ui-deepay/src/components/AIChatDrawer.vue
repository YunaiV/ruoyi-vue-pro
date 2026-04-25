<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
  open:     { type: Boolean, default: false },
  isMobile: { type: Boolean, default: true  },
})
const emit = defineEmits(['update:open'])

const close = () => emit('update:open', false)

// Chat state
const messages = ref([
  {
    id: 1, role: 'assistant',
    content: '你好！我是 Deepay AI 设计助手 ✨\n\n告诉我你想要什么风格的服装，我来帮你出款！',
    time: new Date(),
  },
])
const input      = ref('')
const loading    = ref(false)
const messagesEl = ref(null)

const SUGGESTIONS = [
  '🎯 帮我出10款欧美街头风外套',
  '🌿 生成一个完整的春季系列',
  '🔧 改款：极简黑色卫衣变成高奢感',
  '🎭 推荐当季最热门的设计元素',
]

async function sendMessage(text) {
  const content = (text || input.value).trim()
  if (!content || loading.value) return
  input.value = ''

  messages.value.push({ id: Date.now(), role: 'user', content, time: new Date() })
  await scrollToBottom()

  loading.value = true
  try {
    // TODO: connect to real API
    await new Promise(r => setTimeout(r, 1200))
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: `好的！我正在为你分析「${content}」的市场趋势...\n\n🔍 基于TikTok + 1688实时数据，这个方向非常有潜力！点击下方「AI生成」开始出款 👇`,
      time: new Date(),
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

function formatTime(d) {
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <!-- Overlay (mobile only) -->
  <div v-if="open" class="drawer-overlay" :class="{ mobile: isMobile }" @click.self="close" />

  <!-- Drawer panel -->
  <div
    class="ai-drawer"
    :class="{
      'drawer-mobile':  isMobile,
      'drawer-desktop': !isMobile,
      'drawer-open':    open,
    }"
  >
    <!-- Header -->
    <div class="drawer-header">
      <div v-if="isMobile" class="drawer-handle" />
      <div class="drawer-title-row">
        <div class="drawer-avatar">✨</div>
        <div>
          <div class="drawer-title">Deepay AI 助手</div>
          <div class="drawer-subtitle">AI时尚设计，随时待命</div>
        </div>
        <button class="drawer-close" @click="close">✕</button>
      </div>
    </div>

    <!-- Messages -->
    <div ref="messagesEl" class="drawer-messages">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="msg-row"
        :class="msg.role === 'user' ? 'msg-user' : 'msg-assistant'"
      >
        <div v-if="msg.role === 'assistant'" class="msg-avatar">✨</div>
        <div class="msg-bubble">
          <p class="msg-content">{{ msg.content }}</p>
          <span class="msg-time">{{ formatTime(msg.time) }}</span>
        </div>
      </div>

      <!-- Typing indicator -->
      <div v-if="loading" class="msg-row msg-assistant">
        <div class="msg-avatar">✨</div>
        <div class="msg-bubble">
          <div class="typing-dots">
            <span /><span /><span />
          </div>
        </div>
      </div>
    </div>

    <!-- Suggestions (shown when only 1 message) -->
    <div v-if="messages.length <= 1 && !loading" class="suggestions">
      <button
        v-for="s in SUGGESTIONS"
        :key="s"
        class="suggestion-chip"
        @click="sendMessage(s)"
      >{{ s }}</button>
    </div>

    <!-- Input area -->
    <div class="drawer-input-area">
      <div class="drawer-input-inner">
        <textarea
          v-model="input"
          class="drawer-textarea"
          placeholder="描述你想要的款式或设计需求…"
          rows="1"
          @keydown.enter.exact.prevent="sendMessage()"
          @input="$event.target.style.height = 'auto'; $event.target.style.height = Math.min($event.target.scrollHeight, 120) + 'px'"
        />
        <button
          class="drawer-send"
          :disabled="!input.trim() || loading"
          @click="sendMessage()"
        >
          <span>→</span>
        </button>
      </div>
    </div>

  </div>
</template>

<style scoped>
.drawer-overlay {
  position: fixed; inset: 0; z-index: 300;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* ── Mobile: bottom sheet ─── */
.drawer-mobile {
  position: fixed; left: 0; right: 0; bottom: 0;
  z-index: 400;
  max-height: 85vh;
  border-radius: 20px 20px 0 0;
  background: var(--c-surface);
  border: 1px solid var(--c-card-border);
  border-bottom: none;
  display: flex; flex-direction: column;
  transform: translateY(100%);
  transition: transform 0.35s cubic-bezier(.32,1,.45,1);
  box-shadow: 0 -8px 40px rgba(0,0,0,0.4);
}
.drawer-mobile.drawer-open {
  transform: translateY(0);
}

/* ── Desktop: right side panel ─── */
.drawer-desktop {
  position: fixed; top: 0; right: 0; bottom: 0;
  z-index: 400;
  width: 380px;
  background: var(--c-surface);
  border-left: 1px solid var(--c-card-border);
  display: flex; flex-direction: column;
  transform: translateX(100%);
  transition: transform 0.3s cubic-bezier(.32,1,.45,1);
  box-shadow: -8px 0 40px rgba(0,0,0,0.3);
}
.drawer-desktop.drawer-open {
  transform: translateX(0);
}

/* ── Header ─── */
.drawer-header {
  padding: 12px 16px 0;
  border-bottom: 1px solid var(--c-border);
  flex-shrink: 0;
}
.drawer-handle {
  width: 36px; height: 4px; border-radius: 2px;
  background: var(--c-card-border);
  margin: 0 auto 12px;
}
.drawer-title-row {
  display: flex; align-items: center; gap: 12px; padding-bottom: 12px;
}
.drawer-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: linear-gradient(135deg, rgba(26,188,156,0.2), rgba(26,188,156,0.05));
  border: 1px solid rgba(26,188,156,0.3);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.drawer-title    { font-size: 15px; font-weight: 700; color: var(--c-text-bright); }
.drawer-subtitle { font-size: 12px; color: var(--c-text-sub); margin-top: 1px; }
.drawer-close {
  margin-left: auto; width: 32px; height: 32px; border-radius: 50%;
  background: var(--c-card); border: 1px solid var(--c-card-border);
  color: var(--c-text-sub); cursor: pointer; font-size: 14px;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.drawer-close:hover { color: var(--c-text); }

/* ── Messages ─── */
.drawer-messages {
  flex: 1; overflow-y: auto; padding: 16px;
  display: flex; flex-direction: column; gap: 12px;
  scroll-behavior: smooth;
}
.msg-row { display: flex; gap: 10px; align-items: flex-end; }
.msg-user { flex-direction: row-reverse; }
.msg-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: rgba(26,188,156,0.15); border: 1px solid rgba(26,188,156,0.3);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; flex-shrink: 0;
}
.msg-bubble {
  max-width: 78%; padding: 10px 14px; border-radius: 16px;
  position: relative;
}
.msg-user .msg-bubble {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-radius: 16px 16px 4px 16px;
}
.msg-assistant .msg-bubble {
  background: var(--c-card);
  border: 1px solid var(--c-card-border);
  border-radius: 16px 16px 16px 4px;
}
.msg-content {
  font-size: 14px; line-height: 1.6; margin: 0;
  white-space: pre-wrap; word-break: break-word;
}
.msg-user .msg-content { color: #fff; }
.msg-assistant .msg-content { color: var(--c-text); }
.msg-time { font-size: 10px; color: rgba(255,255,255,0.6); display: block; margin-top: 4px; text-align: right; }
.msg-assistant .msg-time { color: var(--c-text-muted); }

/* Typing dots */
.typing-dots { display: flex; gap: 5px; padding: 4px 0; }
.typing-dots span {
  width: 8px; height: 8px; border-radius: 50%;
  background: #1abc9c; display: block;
  animation: typingBounce 1.4s ease-in-out infinite;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typingBounce {
  0%,80%,100% { transform: scale(0.6); opacity: 0.4; }
  40%         { transform: scale(1);   opacity: 1; }
}

/* ── Suggestions ─── */
.suggestions {
  padding: 8px 16px; display: flex; flex-direction: column; gap: 8px;
  border-top: 1px solid var(--c-border);
}
.suggestion-chip {
  text-align: left; padding: 10px 14px; border-radius: 12px;
  background: var(--c-card); border: 1px solid var(--c-card-border);
  color: var(--c-text); font-size: 13px; cursor: pointer;
  transition: all 0.15s;
}
.suggestion-chip:hover {
  border-color: rgba(26,188,156,0.4);
  background: rgba(26,188,156,0.06);
}

/* ── Input ─── */
.drawer-input-area {
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  border-top: 1px solid var(--c-border);
  flex-shrink: 0;
}
.drawer-input-inner {
  display: flex; align-items: flex-end; gap: 10px;
  background: var(--c-input-bg);
  border: 1px solid var(--c-card-border);
  border-radius: 18px; padding: 8px 8px 8px 16px;
}
.drawer-textarea {
  flex: 1; background: transparent; border: none; outline: none;
  resize: none; font-size: 14px; color: var(--c-text);
  line-height: 1.5; min-height: 24px; max-height: 120px;
  overflow-y: auto; padding: 0;
}
.drawer-textarea::placeholder { color: var(--c-text-muted); }
.drawer-send {
  width: 36px; height: 36px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border: none; cursor: pointer; color: #fff; font-size: 16px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s; box-shadow: 0 2px 8px rgba(26,188,156,0.4);
}
.drawer-send:disabled { background: var(--c-card); box-shadow: none; color: var(--c-text-muted); cursor: not-allowed; }
</style>
