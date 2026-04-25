<template>
  <div class="chat-page">

    <!-- ══ WELCOME SCREEN (no messages) ══ -->
    <div v-if="showWelcome" class="welcome-screen">
      <header class="chat-topbar">
        <button class="model-selector">
          <span class="model-name">Deepay</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
        </button>
        <div class="topbar-actions">
          <button class="tb-icon-btn" title="分享">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8M16 6l-4-4-4 4M12 2v13"/></svg>
          </button>
          <div class="tb-avatar">{{ userStore.avatarLetter }}</div>
        </div>
      </header>

      <div class="welcome-body">
        <h1 class="welcome-greeting">你好，{{ userStore.displayName }}</h1>
        <p class="welcome-sub">今天想创作什么？</p>

        <div class="suggestion-chips">
          <button
            v-for="chip in suggestions"
            :key="chip.text"
            class="chip"
            @click="sendSuggestion(chip.text)"
          >
            <span class="chip-icon">{{ chip.icon }}</span>
            {{ chip.label }}
          </button>
        </div>

        <!-- 快速入口 -->
        <div class="quick-nav">
          <button class="qn-card" @click="router.push('/image-library')">
            <span class="qn-emoji">🖼️</span>
            <span class="qn-label">图库</span>
            <span class="qn-sub">AI 素材库</span>
          </button>
          <button class="qn-card" @click="router.push('/ai-sales')">
            <span class="qn-emoji">🏪</span>
            <span class="qn-label">AI 开店</span>
            <span class="qn-sub">60 秒建店</span>
          </button>
          <button class="qn-card" @click="router.push('/template-library')">
            <span class="qn-emoji">📋</span>
            <span class="qn-label">模板库</span>
            <span class="qn-sub">精选风格</span>
          </button>
          <button class="qn-card" @click="router.push('/settings')">
            <span class="qn-emoji">⚙️</span>
            <span class="qn-label">设置</span>
            <span class="qn-sub">账号管理</span>
          </button>
        </div>
      </div>

      <!-- Input bar -->
      <div class="input-area">
        <div class="input-box" :class="{ focused: inputFocused }">
          <button class="input-icon-btn" title="附件">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/></svg>
          </button>
          <input
            ref="inputRef"
            v-model="inputText"
            class="input-field"
            placeholder="给 Deepay 发消息"
            @keydown.enter.exact.prevent="sendMessage"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
          />
          <button class="input-icon-btn" title="语音">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/><path d="M19 10v2a7 7 0 01-14 0v-2M12 19v4M8 23h8"/></svg>
          </button>
          <button
            class="send-btn"
            :class="{ active: inputText.trim() }"
            @click="sendMessage"
            :disabled="!inputText.trim()"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </button>
        </div>
        <p class="input-hint">Deepay 可能会出现错误，请核实重要信息</p>
      </div>
    </div>

    <!-- ══ ACTIVE CHAT ══ -->
    <div v-else class="chat-screen">
      <header class="chat-topbar">
        <button class="model-selector">
          <span class="model-name">Deepay</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
        </button>
        <div class="topbar-actions">
          <button class="tb-icon-btn" title="分享">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8M16 6l-4-4-4 4M12 2v13"/></svg>
          </button>
          <div class="tb-avatar">{{ userStore.avatarLetter }}</div>
        </div>
      </header>

      <!-- Messages -->
      <div class="messages-wrap" ref="messagesWrap">
        <div class="messages-inner">
          <div
            v-for="msg in chatStore.messages"
            :key="msg.id"
            class="msg-row"
            :class="msg.role"
          >
            <!-- AI message -->
            <template v-if="msg.role === 'assistant'">
              <div class="ai-avatar">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z"/></svg>
              </div>
              <div class="ai-bubble-wrap">
                <div class="ai-bubble">
                  <span class="msg-text" v-html="formatMessage(msg.content)"></span>
                </div>
                <div class="msg-actions">
                  <button class="msg-action-btn" title="复制">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
                  </button>
                  <button class="msg-action-btn" title="点赞">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 00-3-3l-4 9v11h11.28a2 2 0 002-1.7l1.38-9a2 2 0 00-2-2.3zM7 22H4a2 2 0 01-2-2v-7a2 2 0 012-2h3"/></svg>
                  </button>
                  <button class="msg-action-btn" title="重新生成">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>
                  </button>
                </div>
              </div>
            </template>

            <!-- User message -->
            <template v-else>
              <div class="user-bubble">{{ msg.content }}</div>
            </template>
          </div>

          <!-- Typing indicator -->
          <div v-if="isTyping" class="msg-row assistant">
            <div class="ai-avatar">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z"/></svg>
            </div>
            <div class="ai-bubble-wrap">
              <div class="ai-bubble typing-bubble">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>

          <div ref="bottomAnchor"></div>
        </div>
      </div>

      <!-- Input bar -->
      <div class="input-area">
        <div class="input-box" :class="{ focused: inputFocused }">
          <button class="input-icon-btn" title="附件">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/></svg>
          </button>
          <input
            ref="inputRef"
            v-model="inputText"
            class="input-field"
            placeholder="给 Deepay 发消息"
            @keydown.enter.exact.prevent="sendMessage"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
          />
          <button class="input-icon-btn" title="语音">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/><path d="M19 10v2a7 7 0 01-14 0v-2M12 19v4M8 23h8"/></svg>
          </button>
          <button
            class="send-btn"
            :class="{ active: inputText.trim() }"
            @click="sendMessage"
            :disabled="!inputText.trim()"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </button>
        </div>
        <p class="input-hint">Deepay 可能会出现错误，请核实重要信息</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, useChatStore } from '@/store/index.js'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const inputText = ref('')
const inputFocused = ref(false)
const isTyping = ref(false)
const inputRef = ref(null)
const messagesWrap = ref(null)
const bottomAnchor = ref(null)

const showWelcome = computed(() =>
  !chatStore.activeId || !chatStore.messages.length
)

const suggestions = [
  { icon: '🎨', label: '生成服装设计图', text: '帮我生成一款时尚的春季服装设计图，风格简约现代' },
  { icon: '👗', label: '模特试衣展示', text: '帮我展示这套服装在模特身上的效果' },
  { icon: '📊', label: 'AI销售分析', text: '分析我的服装销售数据，给出优化建议' },
  { icon: '🛒', label: '电商主图', text: '帮我生成一张吸引人的电商主图' },
  { icon: '✨', label: '设计灵感', text: '给我一些2024年流行的服装设计灵感' },
  { icon: '📐', label: '模板推荐', text: '推荐适合我品牌风格的设计模板' },
]

const aiReplies = [
  '好的！我来帮您设计一款时尚的春季服装 ✨\n\n**设计方案：**\n- 款式：宽松廓形外套\n- 色彩：奶油白 + 莫兰迪绿\n- 面料：天然棉麻混纺\n- 细节：隐藏式口袋，金属扣设计\n\n这个方案兼顾了时尚感和实用性，非常适合春季穿搭。需要我进一步调整任何细节吗？',
  '当然可以！以下是我的建议 🎯\n\n根据当前市场趋势，建议您关注：\n1. **渐变色系**：粉紫渐变在年轻消费者中非常受欢迎\n2. **复古元素**：Y2K 风格正在强势回归\n3. **环保材质**：消费者对可持续时尚越来越关注\n\n您想深入了解哪个方向呢？',
  '我帮您分析了最近的销售数据 📊\n\n**关键发现：**\n- 上装销量同比增长 23%\n- 裙装在 25-35 岁女性中转化率最高（12.4%）\n- 周末下午 2-5 点是峰值时段\n\n**建议：** 加大上装品类投入，针对核心用户群做精准推广。',
  '为您推荐以下设计灵感 ✨\n\n**2024年流行趋势：**\n- 静奢风（Quiet Luxury）持续走热\n- 解构主义元素融入日常穿搭\n- 大地色系搭配金属光泽配饰\n- 宽松剪裁 + 腰部收束的对比感\n\n需要我为您生成具体的设计稿吗？',
  '好的，我来为您生成电商主图方案 🛒\n\n**主图设计要点：**\n- 背景：简洁的纯白或渐变色\n- 构图：1:1 或 3:4 比例最佳\n- 主体：产品占画面 60-70%\n- 文字：简洁有力，突出卖点\n\n建议配合场景图和细节图一起展示，可以显著提升点击率！',
]

function formatMessage(text) {
  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  return escaped
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

function scrollToBottom() {
  nextTick(() => {
    bottomAnchor.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return

  if (!chatStore.activeId) {
    chatStore.newSession()
  }

  inputText.value = ''
  chatStore.addMessage('user', text)
  scrollToBottom()

  isTyping.value = true
  const delay = 1200 + Math.random() * 600
  await new Promise(r => setTimeout(r, delay))

  isTyping.value = false
  const reply = aiReplies[Math.floor(Math.random() * aiReplies.length)]
  chatStore.addMessage('assistant', reply)
  scrollToBottom()
}

function sendSuggestion(text) {
  inputText.value = text
  nextTick(() => sendMessage())
}

watch(() => chatStore.messages.length, () => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--gpt-main);
  color: var(--gpt-text);
}

/* ── Top bar ─────────────────────────────────── */
.chat-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--gpt-border);
}

.model-selector {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--gpt-text);
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.15s;
}
.model-selector:hover { background: var(--gpt-input-bg); }
.model-name { font-size: 15px; font-weight: 600; }

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tb-icon-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}
.tb-icon-btn:hover {
  background: var(--gpt-input-bg);
  color: var(--gpt-text);
}

.tb-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: white;
  cursor: pointer;
}

/* ── Welcome ─────────────────────────────────── */
.welcome-screen {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.welcome-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px 24px;
  text-align: center;
}

.welcome-greeting {
  font-size: clamp(28px, 5vw, 42px);
  font-weight: 700;
  color: var(--gpt-text);
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.welcome-sub {
  font-size: 18px;
  color: var(--gpt-text-sub);
  margin: 0 0 36px;
}

.suggestion-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  max-width: 680px;
}

.chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 24px;
  color: var(--gpt-text);
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, transform 0.15s;
  white-space: nowrap;
}
.chip:hover {
  border-color: #10a37f;
  background: rgba(16, 163, 127, 0.08);
  transform: translateY(-1px);
}
.chip-icon { font-size: 16px; }

/* ── Quick Nav Cards ─────────────────────────── */
.quick-nav {
  display: flex;
  gap: 10px;
  margin-top: 24px;
  flex-wrap: wrap;
  justify-content: center;
  max-width: 560px;
}
.qn-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 20px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.34,1.56,0.64,1);
  min-width: 100px;
  flex: 1;
}
.qn-card:hover {
  border-color: #10a37f;
  background: rgba(16,163,127,0.08);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(16,163,127,0.15);
}
.qn-emoji { font-size: 24px; }
.qn-label { font-size: 13px; font-weight: 600; color: var(--gpt-text); }
.qn-sub   { font-size: 11px; color: var(--gpt-text-muted); }

/* ── Chat screen ─────────────────────────────── */
.chat-screen {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.messages-wrap {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--gpt-border) transparent;
}

.messages-inner {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 24px 8px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ── Message rows ────────────────────────────── */
.msg-row {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}
.msg-row.user {
  justify-content: flex-end;
}

/* AI avatar */
.ai-avatar {
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  margin-top: 2px;
}

.ai-bubble-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 82%;
}

.ai-bubble {
  padding: 14px 16px;
  background: transparent;
  color: var(--gpt-text);
  font-size: 15px;
  line-height: 1.65;
  border-radius: 0 16px 16px 16px;
}

.msg-text { display: block; }

.user-bubble {
  padding: 12px 16px;
  background: var(--gpt-input-bg);
  color: var(--gpt-text);
  font-size: 15px;
  line-height: 1.55;
  border-radius: 16px 16px 4px 16px;
  max-width: 72%;
  word-break: break-word;
}

/* Action buttons under AI message */
.msg-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.15s;
}
.ai-bubble-wrap:hover .msg-actions { opacity: 1; }

.msg-action-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}
.msg-action-btn:hover {
  background: var(--gpt-input-bg);
  color: var(--gpt-text);
}

/* Typing dots */
.typing-bubble {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 14px 18px;
  min-height: 46px;
}
.dot {
  width: 7px;
  height: 7px;
  background: var(--gpt-text-sub);
  border-radius: 50%;
  animation: bounce 1.2s ease-in-out infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* ── Input area ──────────────────────────────── */
.input-area {
  padding: 12px 24px 16px;
  flex-shrink: 0;
  max-width: 760px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

.input-box {
  display: flex;
  align-items: center;
  gap: 4px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 16px;
  padding: 6px 8px;
  transition: border-color 0.2s;
}
.input-box.focused { border-color: rgba(16, 163, 127, 0.5); }

.input-icon-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 10px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: color 0.15s, background 0.15s;
}
.input-icon-btn:hover {
  color: var(--gpt-text);
  background: rgba(255, 255, 255, 0.06);
}

.input-field {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-size: 15px;
  color: var(--gpt-text);
  padding: 4px 8px;
  min-width: 0;
  line-height: 1.5;
}
.input-field::placeholder { color: var(--gpt-text-muted); }

.send-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: var(--gpt-border);
  border-radius: 10px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s, color 0.2s, transform 0.15s;
}
.send-btn.active {
  background: #10a37f;
  color: white;
}
.send-btn.active:hover {
  background: #0d8b6e;
  transform: scale(1.05);
}
.send-btn:disabled { cursor: not-allowed; opacity: 0.7; }

.input-hint {
  text-align: center;
  font-size: 12px;
  color: var(--gpt-text-muted);
  margin: 8px 0 0;
}

@media (max-width: 640px) {
  .messages-inner { padding: 16px 16px 8px; }
  .input-area { padding: 8px 16px 12px; }
  .suggestion-chips { flex-direction: column; align-items: center; }
  .chip { width: 100%; max-width: 320px; justify-content: center; }
}
</style>
