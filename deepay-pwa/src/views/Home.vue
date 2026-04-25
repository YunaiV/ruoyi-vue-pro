<template>
  <div class="chat-page">

    <!-- ══ When no active session: Welcome Screen ══ -->
    <div v-if="!chatStore.activeId || isNewChat" class="welcome-screen">

      <!-- Top bar -->
      <header class="chat-topbar">
        <div class="model-selector">
          <span class="model-name">Deepay</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
        </div>
        <div class="topbar-actions">
          <button class="tb-btn" title="分享">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8M16 6l-4-4-4 4M12 2v13"/></svg>
          </button>
          <button class="tb-btn" title="设置">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 010 14.14M4.93 4.93a10 10 0 000 14.14"/></svg>
          </button>
          <div class="avatar-btn">{{ userStore.avatarLetter }}</div>
        </div>
      </header>

      <!-- Greeting -->
      <div class="welcome-body">
        <h1 class="welcome-greeting">你好，{{ userStore.displayName }}</h1>
        <h2 class="welcome-question">今天想创作什么？</h2>

        <!-- Quick suggestion chips -->
        <div class="suggestion-chips">
          <button
            v-for="chip in chips"
            :key="chip.text"
            class="chip"
            @click="sendMessage(chip.prompt)"
          >
            <span class="chip-icon">{{ chip.icon }}</span>
            <span>{{ chip.text }}</span>
          </button>
        </div>
      </div>

      <!-- Bottom input bar -->
      <div class="input-zone">
        <div class="input-bar">
          <button class="input-action" title="上传文件">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/></svg>
          </button>
          <input
            ref="inputEl"
            v-model="inputText"
            type="text"
            placeholder="给 Deepay 发消息"
            class="input-field"
            @keydown.enter.prevent="sendMessage()"
          />
          <button class="input-action" title="语音输入">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/><path d="M19 10v2a7 7 0 01-14 0v-2M12 19v4M8 23h8"/></svg>
          </button>
          <button
            class="send-btn"
            :class="{ active: inputText.trim() }"
            :disabled="!inputText.trim()"
            @click="sendMessage()"
            title="发送"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M12 19V5M5 12l7-7 7 7"/></svg>
          </button>
        </div>
        <p class="input-hint">Deepay 可能会出现错误，重要决策请自行核实。</p>
      </div>
    </div>

    <!-- ══ Active Chat ══ -->
    <div v-else class="chat-active">

      <!-- Top bar -->
      <header class="chat-topbar">
        <div class="model-selector">
          <span class="model-name">Deepay</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
        </div>
        <div class="topbar-actions">
          <button class="tb-btn"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8M16 6l-4-4-4 4M12 2v13"/></svg></button>
          <div class="avatar-btn">{{ userStore.avatarLetter }}</div>
        </div>
      </header>

      <!-- Messages -->
      <div class="messages-wrap" ref="messagesEl">
        <div class="messages-inner">
          <div
            v-for="msg in chatStore.messages"
            :key="msg.id"
            class="msg-row"
            :class="msg.role"
          >
            <!-- Assistant -->
            <template v-if="msg.role === 'assistant'">
              <div class="msg-ai-avatar">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2.4 7.4H22l-6.2 4.5 2.4 7.4L12 17l-6.2 4.3 2.4-7.4L2 9.4h7.6L12 2z"/></svg>
              </div>
              <div class="msg-ai-content">
                <pre class="msg-text">{{ msg.content }}</pre>
                <div class="msg-actions">
                  <button class="msg-action-btn" title="复制">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
                  </button>
                  <button class="msg-action-btn" title="点赞">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3H14z"/><path d="M7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3"/></svg>
                  </button>
                  <button class="msg-action-btn" title="重新生成">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/></svg>
                  </button>
                </div>
              </div>
            </template>

            <!-- User -->
            <template v-else>
              <div class="msg-user-bubble">{{ msg.content }}</div>
            </template>
          </div>

          <!-- Typing indicator -->
          <div v-if="isTyping" class="msg-row assistant">
            <div class="msg-ai-avatar">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2.4 7.4H22l-6.2 4.5 2.4 7.4L12 17l-6.2 4.3 2.4-7.4L2 9.4h7.6L12 2z"/></svg>
            </div>
            <div class="typing-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Input bar (always at bottom) -->
      <div class="input-zone">
        <div class="input-bar">
          <button class="input-action">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/></svg>
          </button>
          <input
            ref="inputEl"
            v-model="inputText"
            type="text"
            placeholder="给 Deepay 发消息"
            class="input-field"
            @keydown.enter.prevent="sendMessage()"
          />
          <button class="input-action">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/><path d="M19 10v2a7 7 0 01-14 0v-2M12 19v4M8 23h8"/></svg>
          </button>
          <button
            class="send-btn"
            :class="{ active: inputText.trim() }"
            :disabled="!inputText.trim()"
            @click="sendMessage()"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M12 19V5M5 12l7-7 7 7"/></svg>
          </button>
        </div>
        <p class="input-hint">Deepay 可能会出现错误，重要决策请自行核实。</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { useUserStore, useChatStore } from '@/store/index.js'

const userStore = useUserStore()
const chatStore = useChatStore()

const inputText = ref('')
const isTyping = ref(false)
const inputEl = ref(null)
const messagesEl = ref(null)

const isNewChat = computed(() =>
  !chatStore.messages.length || chatStore.messages.every(m => m.role === 'assistant')
)

const chips = [
  { icon: '🎨', text: '生成服装设计图', prompt: '帮我生成一套简约风格的服装设计图，包含上衣和裤子' },
  { icon: '👗', text: '模特试衣展示', prompt: '为我的服装设计选择合适的模特并展示试衣效果' },
  { icon: '📊', text: 'AI 销售分析', prompt: '分析我的店铺销售数据，给出提升转化率的建议' },
  { icon: '🛒', text: '电商主图生成', prompt: '帮我制作一张吸引人的电商产品主图' },
  { icon: '✨', text: '设计灵感', prompt: '2024年最流行的服装设计趋势有哪些？' },
  { icon: '📐', text: '模板推荐', prompt: '根据我的品牌风格推荐合适的店铺设计模板' },
]

async function sendMessage(text) {
  const content = text || inputText.value.trim()
  if (!content) return

  inputText.value = ''
  if (!chatStore.activeId) chatStore.newSession()
  chatStore.addMessage('user', content)

  await nextTick()
  scrollToBottom()

  isTyping.value = true
  setTimeout(async () => {
    const replies = [
      '收到！我正在为您处理这个请求，马上为您生成设计方案 ✨',
      '好的！根据您的需求，我将为您创作最合适的设计内容 🎨',
      '明白了！让我来帮您分析并给出专业建议 📊',
      '正在生成中...您的专属设计方案即将呈现 👗',
    ]
    const reply = replies[Math.floor(Math.random() * replies.length)]
    isTyping.value = false
    chatStore.addMessage('assistant', reply)
    await nextTick()
    scrollToBottom()
  }, 1200 + Math.random() * 800)
}

function scrollToBottom() {
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

watch(() => chatStore.messages.length, () => {
  nextTick(scrollToBottom)
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--gpt-main, #212121);
}

/* ─── Top bar ─────────────────────────────────── */
.chat-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--gpt-border);
}
.model-selector {
  display: flex; align-items: center; gap: 6px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.15s;
}
.model-selector:hover { background: var(--gpt-sidebar-hover); }
.model-name {
  font-size: 16px; font-weight: 700;
  color: var(--gpt-text);
}
.topbar-actions { display: flex; align-items: center; gap: 8px; }
.tb-btn {
  width: 34px; height: 34px;
  border: 1px solid var(--gpt-border);
  background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.tb-btn:hover { background: var(--gpt-sidebar-hover); color: var(--gpt-text); }
.avatar-btn {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: white;
  cursor: pointer;
}

/* ─── Welcome screen ─────────────────────────── */
.welcome-screen {
  flex: 1;
  display: flex; flex-direction: column;
}
.welcome-body {
  flex: 1;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 40px 24px 20px;
  text-align: center;
}
.welcome-greeting {
  font-size: 32px; font-weight: 700;
  color: var(--gpt-text);
  margin: 0 0 8px;
}
.welcome-question {
  font-size: 32px; font-weight: 700;
  color: var(--gpt-text);
  margin: 0 0 40px;
}
@media (max-width: 600px) {
  .welcome-greeting, .welcome-question { font-size: 24px; }
}

/* Chips */
.suggestion-chips {
  display: flex; flex-wrap: wrap; gap: 10px;
  justify-content: center;
  max-width: 640px;
}
.chip {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 18px;
  background: var(--gpt-input-bg, #2f2f2f);
  border: 1px solid var(--gpt-border);
  border-radius: 999px;
  color: var(--gpt-text-sub);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.chip:hover {
  background: var(--gpt-sidebar-hover);
  color: var(--gpt-text);
  border-color: #10a37f;
}
.chip-icon { font-size: 16px; }

/* ─── Active chat ────────────────────────────── */
.chat-active {
  flex: 1;
  display: flex; flex-direction: column;
  overflow: hidden;
}

/* Messages */
.messages-wrap {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}
.messages-wrap::-webkit-scrollbar { width: 4px; }
.messages-wrap::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }

.messages-inner {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 24px 8px;
  display: flex; flex-direction: column; gap: 24px;
}

/* Message rows */
.msg-row {
  display: flex; align-items: flex-start; gap: 14px;
}
.msg-row.user { justify-content: flex-end; }

/* AI avatar */
.msg-ai-avatar {
  width: 32px; height: 32px; flex-shrink: 0;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 14px;
}

/* AI message */
.msg-ai-content { flex: 1; min-width: 0; }
.msg-text {
  font-family: inherit;
  font-size: 15px; line-height: 1.65;
  color: var(--gpt-text);
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}
.msg-actions {
  display: flex; gap: 4px; margin-top: 8px; opacity: 0;
  transition: opacity 0.15s;
}
.msg-ai-content:hover .msg-actions { opacity: 1; }
.msg-action-btn {
  width: 28px; height: 28px;
  border: none; background: transparent;
  border-radius: 6px;
  color: var(--gpt-text-muted); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.msg-action-btn:hover { background: var(--gpt-sidebar-hover); color: var(--gpt-text); }

/* User bubble */
.msg-user-bubble {
  max-width: 480px;
  background: var(--gpt-input-bg, #2f2f2f);
  border: 1px solid var(--gpt-border);
  border-radius: 18px 18px 4px 18px;
  padding: 12px 18px;
  font-size: 15px; line-height: 1.55;
  color: var(--gpt-text);
  word-break: break-word;
}

/* Typing */
.typing-dots {
  display: flex; align-items: center; gap: 4px;
  padding: 14px 18px;
  background: var(--gpt-input-bg);
  border-radius: 18px;
}
.typing-dots span {
  width: 8px; height: 8px;
  background: var(--gpt-text-sub);
  border-radius: 50%;
  animation: bounce 1.2s ease-in-out infinite;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40% { transform: scale(1.2); opacity: 1; }
}

/* ─── Input zone ─────────────────────────────── */
.input-zone {
  flex-shrink: 0;
  padding: 16px 24px 20px;
  max-width: 760px;
  width: 100%;
  margin: 0 auto;
}
.input-bar {
  display: flex; align-items: center; gap: 8px;
  background: var(--gpt-input-bg, #2f2f2f);
  border: 1px solid var(--gpt-border);
  border-radius: 16px;
  padding: 8px 12px;
  transition: border-color 0.15s;
}
.input-bar:focus-within { border-color: #10a37f; }
.input-action {
  width: 34px; height: 34px; flex-shrink: 0;
  border: none; background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.input-action:hover { color: var(--gpt-text); }
.input-field {
  flex: 1;
  background: transparent; border: none; outline: none;
  font-size: 15px; line-height: 1.5;
  color: var(--gpt-text);
  padding: 4px 4px;
}
.input-field::placeholder { color: var(--gpt-text-muted, #555); }
.send-btn {
  width: 34px; height: 34px; flex-shrink: 0;
  border: none;
  background: var(--gpt-border);
  border-radius: 8px;
  color: var(--gpt-text-muted);
  cursor: not-allowed;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.send-btn.active {
  background: #10a37f;
  color: white;
  cursor: pointer;
}
.send-btn.active:hover { background: #0d8b6e; transform: scale(1.05); }
.input-hint {
  text-align: center;
  font-size: 12px;
  color: var(--gpt-text-muted, #555);
  margin: 8px 0 0;
}
</style>
