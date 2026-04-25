<template>
  <transition name="chat-slide">
    <div v-if="isOpen" class="chat-overlay" @click.self="$emit('close')">
      <div class="chat-panel" :class="{ mobile: isMobile }">
        <!-- Header -->
        <div class="chat-header">
          <div class="chat-header-left">
            <div class="ai-avatar">✨</div>
            <div>
              <div class="chat-title">Deepay AI</div>
              <div class="chat-subtitle">设计助手在线</div>
            </div>
          </div>
          <button class="close-btn" @click="$emit('close')" aria-label="关闭">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <!-- Messages -->
        <div class="messages-area scrollbar-hide" ref="messagesEl">
          <!-- Suggestion chips when no user messages -->
          <div v-if="isNewChat" class="suggestions">
            <p class="suggest-label">快速开始</p>
            <div class="suggest-grid">
              <button
                v-for="s in suggestions"
                :key="s"
                class="suggest-chip"
                @click="sendSuggestion(s)"
              >{{ s }}</button>
            </div>
          </div>

          <!-- Messages -->
          <div
            v-for="msg in chatStore.messages"
            :key="msg.id"
            class="msg-row"
            :class="msg.role"
          >
            <div v-if="msg.role === 'assistant'" class="msg-avatar">✨</div>
            <div class="msg-bubble" :class="msg.role">
              <pre class="msg-text">{{ msg.content }}</pre>
              <span class="msg-time">{{ formatTime(msg.time) }}</span>
            </div>
            <div v-if="msg.role === 'user'" class="msg-avatar user-av">{{ userStore.avatarLetter }}</div>
          </div>

          <!-- Typing -->
          <div v-if="isTyping" class="msg-row assistant">
            <div class="msg-avatar">✨</div>
            <div class="msg-bubble assistant typing-bubble">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="chat-input-area">
          <textarea
            ref="inputEl"
            v-model="inputText"
            class="chat-textarea"
            placeholder="描述你的设计想法…"
            rows="1"
            @keydown.enter.prevent="handleEnter"
            @input="autoResize"
          />
          <button
            class="send-btn"
            :class="{ active: inputText.trim() }"
            :disabled="!inputText.trim() || isTyping"
            @click="sendMessage"
            aria-label="发送"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M22 2L11 13M22 2L15 22l-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { useChatStore } from '@/store/index.js'
import { useUserStore } from '@/store/index.js'

const props = defineProps({
  isOpen:  { type: Boolean, default: false },
  isMobile:{ type: Boolean, default: true },
})
const emit = defineEmits(['close'])

const cs = useChatStore()
const userStore = useUserStore()
const inputText = ref('')
const isTyping = ref(false)
const messagesEl = ref(null)
const inputEl = ref(null)

const isNewChat = computed(() => {
  const msgs = cs.messages
  return msgs.length <= 1
})

const suggestions = [
  '🎯 帮我出一款夏季连衣裙',
  '✨ 推荐适合街头风的配色',
  '🌿 设计一套整季系列',
  '🔧 帮我改良这件外套',
]

function formatTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return d.getHours().toString().padStart(2,'0') + ':' + d.getMinutes().toString().padStart(2,'0')
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  })
}

function autoResize() {
  if (!inputEl.value) return
  inputEl.value.style.height = 'auto'
  inputEl.value.style.height = Math.min(inputEl.value.scrollHeight, 120) + 'px'
}

function handleEnter(e) {
  if (!e.shiftKey) sendMessage()
}

async function sendSuggestion(text) {
  inputText.value = text
  await sendMessage()
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return
  inputText.value = ''
  if (inputEl.value) { inputEl.value.style.height = 'auto' }

  cs.addMessage('user', text)
  scrollToBottom()
  isTyping.value = true

  await new Promise(r => setTimeout(r, 800 + Math.random() * 1200))

  const replies = [
    '好的！根据你的描述，我为你规划了以下设计方向：\n\n1. **廓形**：A字型剪裁，突出腰线\n2. **面料**：建议使用雪纺或真丝，透气飘逸\n3. **配色**：主色调选米白或浅绿，搭配金色细节\n\n要继续深化某个方向吗？',
    '非常好的创意！✨ 这个款式非常适合当季流行趋势。\n\n建议参考以下元素：\n• 落肩设计 — 今年非常流行\n• 抽绳细节 — 增加层次感\n• 撞色拼接 — 提升时尚度\n\n需要我出具体的设计稿吗？',
    '明白了！我来帮你分析这个方向的市场潜力。\n\n根据近期数据，这类款式：\n📈 搜索热度：↑23%\n🎯 目标客群：18-28岁\n💰 建议零售价：299-499元\n\n要看详细的竞品分析吗？',
  ]
  const reply = replies[Math.floor(Math.random() * replies.length)]
  cs.addMessage('assistant', reply)
  isTyping.value = false
  scrollToBottom()
}

watch(() => props.isOpen, (v) => {
  if (v) {
    if (!cs.activeId) cs.newSession()
    nextTick(() => scrollToBottom())
  }
})
</script>

<style scoped>
.chat-overlay {
  position: fixed; inset: 0; z-index: 500;
  background: rgba(0,0,0,0.55);
  backdrop-filter: blur(4px);
  display: flex; align-items: flex-end; justify-content: flex-end;
}
.chat-panel {
  width: 400px;
  height: 100vh;
  background: var(--dp-surface);
  display: flex; flex-direction: column;
  border-left: 1px solid var(--dp-border);
  box-shadow: var(--dp-shadow-lg);
}
.chat-panel.mobile {
  width: 100%;
  height: 80vh;
  border-radius: 20px 20px 0 0;
  border-left: none;
  border-top: 1px solid var(--dp-border);
}
.chat-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dp-border);
  background: var(--dp-surface);
  flex-shrink: 0;
}
.chat-header-left { display: flex; align-items: center; gap: 12px; }
.ai-avatar {
  width: 40px; height: 40px;
  background: linear-gradient(135deg, rgba(26,188,156,0.25), rgba(26,188,156,0.1));
  border: 1px solid rgba(26,188,156,0.3);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px;
}
.chat-title { font-size: 15px; font-weight: 700; color: var(--dp-text-bright); }
.chat-subtitle { font-size: 12px; color: #1abc9c; }
.close-btn {
  background: var(--dp-chip-bg); border: 1px solid var(--dp-chip-border);
  color: var(--dp-text-sub); width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.15s;
}
.close-btn:hover { background: var(--dp-card); color: var(--dp-text); }

.messages-area {
  flex: 1; overflow-y: auto;
  padding: 16px;
  display: flex; flex-direction: column; gap: 12px;
}
.suggestions { margin-bottom: 8px; }
.suggest-label { font-size: 12px; color: var(--dp-text-muted); margin-bottom: 10px; }
.suggest-grid { display: flex; flex-direction: column; gap: 8px; }
.suggest-chip {
  background: var(--dp-card); border: 1px solid var(--dp-card-border);
  border-radius: 12px; padding: 12px 16px;
  color: var(--dp-text); font-size: 13px; font-weight: 500;
  text-align: left; cursor: pointer; transition: all 0.15s;
}
.suggest-chip:hover { border-color: rgba(26,188,156,0.4); color: #1abc9c; background: var(--dp-accent-bg); }

.msg-row { display: flex; align-items: flex-end; gap: 8px; }
.msg-row.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 30px; height: 30px; border-radius: 50%;
  background: linear-gradient(135deg, rgba(26,188,156,0.2), rgba(26,188,156,0.05));
  border: 1px solid rgba(26,188,156,0.25);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; flex-shrink: 0;
}
.msg-avatar.user-av {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-color: transparent;
  color: white; font-size: 12px; font-weight: 700;
}
.msg-bubble {
  max-width: 75%; padding: 10px 14px;
  border-radius: 16px 16px 16px 4px;
  background: var(--dp-card);
  border: 1px solid var(--dp-card-border);
}
.msg-bubble.user {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-color: transparent;
  border-radius: 16px 16px 4px 16px;
}
.msg-text {
  font-size: 13px; line-height: 1.6;
  color: var(--dp-text); white-space: pre-wrap;
  word-break: break-word; margin: 0;
  font-family: inherit;
}
.msg-bubble.user .msg-text { color: white; }
.msg-time { font-size: 10px; color: var(--dp-text-muted); display: block; margin-top: 4px; text-align: right; }
.msg-bubble.user .msg-time { color: rgba(255,255,255,0.65); }

.typing-bubble { display: flex; gap: 4px; align-items: center; padding: 12px 16px; }
.dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: var(--dp-accent); opacity: 0.7;
  animation: bounce 1.2s ease-in-out infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,80%,100%{transform:translateY(0)} 40%{transform:translateY(-6px)} }

.chat-input-area {
  display: flex; align-items: flex-end; gap: 10px;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  border-top: 1px solid var(--dp-border);
  background: var(--dp-surface);
  flex-shrink: 0;
}
.chat-textarea {
  flex: 1; resize: none; min-height: 44px; max-height: 120px;
  background: var(--dp-input-bg);
  border: 1px solid var(--dp-card-border);
  border-radius: 22px; padding: 11px 16px;
  font-size: 14px; color: var(--dp-text);
  outline: none; font-family: inherit; line-height: 1.5;
  transition: border-color 0.15s;
}
.chat-textarea::placeholder { color: var(--dp-text-muted); }
.chat-textarea:focus { border-color: #1abc9c; }
.send-btn {
  width: 44px; height: 44px; border-radius: 50%; border: none;
  background: var(--dp-chip-bg);
  color: var(--dp-text-muted); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s; flex-shrink: 0;
}
.send-btn.active {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  color: white;
  box-shadow: 0 4px 16px rgba(26,188,156,0.4);
}
.send-btn:hover.active { transform: scale(1.05); }

/* Transition */
.chat-slide-enter-active, .chat-slide-leave-active { transition: all 0.3s cubic-bezier(.22,1,.36,1); }
.chat-slide-enter-from .chat-panel { transform: translateX(100%); }
.chat-slide-leave-to .chat-panel { transform: translateX(100%); }
.chat-slide-enter-from .chat-panel.mobile { transform: translateY(100%); }
.chat-slide-leave-to .chat-panel.mobile { transform: translateY(100%); }
.chat-slide-enter-from { opacity: 0; }
.chat-slide-leave-to { opacity: 0; }
</style>
