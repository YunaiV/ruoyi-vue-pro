<template>
  <div class="page">
    <!-- Header -->
    <div class="page-header dp-fade-up">
      <h1 class="page-title">AI销售顾问</h1>
      <p class="page-sub">基于实时市场数据，为你的产品提供销售建议</p>
    </div>

    <!-- Messages -->
    <div class="messages-area scrollbar-hide" ref="messagesEl">
      <!-- Suggestions (when new) -->
      <div v-if="isNewChat" class="suggestions dp-fade-up">
        <button
          v-for="s in suggestions"
          :key="s"
          class="suggest-chip"
          @click="sendSuggestion(s)"
        >{{ s }}</button>
      </div>

      <!-- Messages List -->
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="msg-row"
        :class="msg.role"
      >
        <div v-if="msg.role === 'assistant'" class="msg-avatar">📊</div>
        <div class="msg-bubble" :class="msg.role">
          <pre class="msg-text">{{ msg.content }}</pre>
          <span class="msg-time">{{ formatTime(msg.time) }}</span>
        </div>
        <div v-if="msg.role === 'user'" class="msg-avatar user-av">我</div>
      </div>

      <!-- Typing Indicator -->
      <div v-if="isTyping" class="msg-row assistant">
        <div class="msg-avatar">📊</div>
        <div class="msg-bubble assistant typing-bubble">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>
        </div>
      </div>
    </div>

    <!-- Input Bar -->
    <div class="input-bar">
      <textarea
        ref="inputEl"
        v-model="inputText"
        class="chat-textarea"
        placeholder="描述你的产品，获取销售建议…"
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
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'

const messages = ref([
  {
    id: 1, role: 'assistant',
    content: '你好！我是Deepay AI销售顾问 📊\n请描述你的产品，我来为你分析市场趋势、定价策略和销售建议！',
    time: new Date().toISOString()
  }
])
const inputText = ref('')
const isTyping = ref(false)
const messagesEl = ref(null)
const inputEl = ref(null)

const isNewChat = computed(() => messages.value.length <= 1)

const suggestions = [
  '📈 分析这款外套的市场趋势',
  '💰 帮我制定最优定价策略',
  '🎯 推荐最适合的目标客群',
  '🌍 哪个市场最有销售潜力',
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
  if (inputEl.value) inputEl.value.style.height = 'auto'

  messages.value.push({ id: Date.now(), role: 'user', content: text, time: new Date().toISOString() })
  scrollToBottom()
  isTyping.value = true

  await new Promise(r => setTimeout(r, 1000 + Math.random() * 1500))

  const salesReplies = [
    '根据最新市场数据分析：\n\n📈 **市场趋势**\n该品类今年增速达 +31%，需求旺盛\n\n💰 **定价建议**\n竞品区间：199-599元\n建议定价：**299元**（性价比最优区间）\n\n🎯 **目标人群**\n25-35岁女性，月收入8000+，注重品质',
    '好的，我为你制定了详细定价策略：\n\n🔥 **入门款**：199元（引流）\n⭐ **主力款**：399元（主推）\n💎 **高端款**：699元（利润）\n\n建议主推中档，毛利率可达 **58%**\n\n还需要分析竞争对手吗？',
    '根据数据，最适合的目标客群：\n\n👥 **核心人群**：都市白领女性\n• 年龄：22-32岁\n• 城市：一二线城市\n• 消费特征：愿意为品质付溢价\n• 购买场景：通勤+休闲两用\n\n📲 **触达渠道**：小红书 > 抖音 > 微信小程序',
  ]

  const reply = salesReplies[Math.floor(Math.random() * salesReplies.length)]
  messages.value.push({ id: Date.now() + 1, role: 'assistant', content: reply, time: new Date().toISOString() })
  isTyping.value = false
  scrollToBottom()
}
</script>

<style scoped>
.page {
  display: flex; flex-direction: column;
  height: calc(100vh - 56px - 64px - env(safe-area-inset-bottom));
  padding: 0;
  overflow: hidden;
}
@media (min-width: 1024px) {
  .page { height: 100vh; }
}
.page-header {
  padding: 20px 20px 12px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--dp-border);
}
.page-title {
  font-size: 20px; font-weight: 800; color: var(--dp-text-bright);
  margin: 0 0 4px; letter-spacing: -0.02em;
}
.page-sub { font-size: 13px; color: var(--dp-text-sub); margin: 0; }

.messages-area {
  flex: 1; overflow-y: auto;
  padding: 16px; display: flex; flex-direction: column; gap: 12px;
}

/* Suggestions */
.suggestions { display: flex; flex-direction: column; gap: 8px; margin-bottom: 4px; }
.suggest-chip {
  background: var(--dp-card); border: 1px solid var(--dp-card-border);
  border-radius: 12px; padding: 13px 16px;
  color: var(--dp-text); font-size: 13px; font-weight: 500;
  text-align: left; cursor: pointer; transition: all 0.15s;
}
.suggest-chip:hover { border-color: rgba(26,188,156,0.4); color: #1abc9c; background: var(--dp-accent-bg); }

/* Messages */
.msg-row { display: flex; align-items: flex-end; gap: 8px; }
.msg-row.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, rgba(26,188,156,0.2), rgba(26,188,156,0.05));
  border: 1px solid rgba(26,188,156,0.25);
  display: flex; align-items: center; justify-content: center; font-size: 16px;
}
.msg-avatar.user-av {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-color: transparent; color: white; font-size: 11px; font-weight: 700;
}
.msg-bubble {
  max-width: 78%; padding: 12px 16px;
  border-radius: 18px 18px 18px 4px;
  background: var(--dp-card); border: 1px solid var(--dp-card-border);
}
.msg-bubble.user {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border-color: transparent; border-radius: 18px 18px 4px 18px;
}
.msg-text {
  font-size: 14px; line-height: 1.65; color: var(--dp-text);
  white-space: pre-wrap; word-break: break-word; margin: 0; font-family: inherit;
}
.msg-bubble.user .msg-text { color: white; }
.msg-time { font-size: 10px; color: var(--dp-text-muted); display: block; margin-top: 5px; }
.msg-bubble.user .msg-time { color: rgba(255,255,255,0.6); text-align: right; }

.typing-bubble { display: flex; gap: 4px; align-items: center; padding: 14px 18px; }
.dot { width: 7px; height: 7px; border-radius: 50%; background: var(--dp-accent); opacity: 0.7; animation: bounce 1.2s ease-in-out infinite; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,80%,100%{transform:translateY(0)} 40%{transform:translateY(-6px)} }

/* Input */
.input-bar {
  display: flex; align-items: flex-end; gap: 10px;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  border-top: 1px solid var(--dp-border);
  background: var(--dp-surface);
  flex-shrink: 0;
}
@media (min-width: 1024px) {
  .input-bar { padding-bottom: 20px; }
}
.chat-textarea {
  flex: 1; resize: none; min-height: 44px; max-height: 120px;
  background: var(--dp-input-bg); border: 1px solid var(--dp-card-border);
  border-radius: 22px; padding: 11px 16px;
  font-size: 14px; color: var(--dp-text); outline: none;
  font-family: inherit; line-height: 1.5; transition: border-color 0.15s;
}
.chat-textarea::placeholder { color: var(--dp-text-muted); }
.chat-textarea:focus { border-color: #1abc9c; }
.send-btn {
  width: 44px; height: 44px; border-radius: 50%; border: none;
  background: var(--dp-chip-bg); color: var(--dp-text-muted);
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.2s; flex-shrink: 0;
}
.send-btn.active {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  color: white; box-shadow: 0 4px 16px rgba(26,188,156,0.4);
}
.send-btn:hover.active { transform: scale(1.05); }
</style>
