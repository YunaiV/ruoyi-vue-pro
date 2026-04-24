<template>
  <Teleport to="body">
    <!-- ── FAB 悬浮气泡 ────────────────────────────────────── -->
    <Transition name="ai-fab">
      <button
        v-if="!open"
        class="ai-fab"
        :title="`AI 助手 · ${moduleName}`"
        aria-label="打开 AI 助手"
        @click="openDrawer"
      >
        <span class="ai-fab__ring" />
        <span class="ai-fab__icon">🤖</span>
        <span v-if="unread > 0" class="ai-fab__badge">{{ unread > 9 ? '9+' : unread }}</span>
      </button>
    </Transition>

    <!-- ── 半透明蒙层（移动端） ───────────────────────────── -->
    <Transition name="ai-overlay">
      <div v-if="open" class="ai-overlay" @click="closeDrawer" />
    </Transition>

    <!-- ── 对话抽屉 ──────────────────────────────────────── -->
    <Transition name="ai-drawer">
      <div v-if="open" class="ai-drawer" role="dialog" :aria-label="`AI 助手 · ${moduleName}`">

        <!-- 标题栏 -->
        <header class="ai-drawer__header">
          <div class="ai-drawer__header-left">
            <div class="ai-avatar-dot" />
            <div>
              <div class="ai-drawer__title">AI 助手 · {{ moduleName }}</div>
              <div class="ai-drawer__status">
                <span class="ai-status-dot" :class="chat.loading.value ? 'ai-status-dot--thinking' : 'ai-status-dot--online'" />
                {{ chat.loading.value ? '正在思考…' : '在线' }}
              </div>
            </div>
          </div>
          <div class="ai-drawer__header-right">
            <button
              class="ai-icon-btn"
              :title="typewriterOn ? '⚡ 切换为即时显示' : '⌨️ 切换为打字机效果'"
              @click="typewriterOn = !typewriterOn"
            >{{ typewriterOn ? '⌨️' : '⚡' }}</button>
            <button class="ai-icon-btn" title="清空对话" @click="handleClear">🗑</button>
            <button class="ai-icon-btn" title="最小化" @click="minimized = true; open = false">－</button>
            <button class="ai-icon-btn ai-icon-btn--close" title="关闭并结束会话" @click="closeDrawer">✕</button>
          </div>
        </header>

        <!-- 消息区 -->
        <div class="ai-messages" ref="messagesEl">
          <template v-for="msg in chat.messages.value" :key="msg.id">

            <!-- AI 气泡 -->
            <div v-if="msg.role === 'ai'" class="ai-msg ai-msg--ai">
              <div class="ai-msg__avatar">🤖</div>
              <div class="ai-msg__body">
                <div class="ai-msg__bubble">
                  <span v-html="renderText(msg.content)" />
                  <span v-if="msg.streaming" class="ai-cursor" />
                </div>

                <!-- 图片卡片 -->
                <div v-if="msg.images?.length" class="ai-msg__images">
                  <div
                    v-for="(url, i) in msg.images"
                    :key="i"
                    class="ai-msg__image-card"
                    @click="emit('imageClick', url)"
                    :title="`推荐款 ${i + 1} — 点击查看`"
                  >
                    <img :src="url" :alt="`推荐款 ${i + 1}`" loading="lazy" />
                    <div class="ai-msg__image-label">款 {{ i + 1 }}</div>
                  </div>
                </div>

                <!-- 快速回复 -->
                <div v-if="msg.quickReplies?.length && !msg.streaming" class="ai-msg__quick">
                  <button
                    v-for="r in msg.quickReplies"
                    :key="r"
                    class="ai-quick-btn"
                    :disabled="chat.loading.value"
                    @click="chat.quickReply(r)"
                  >{{ r }}</button>
                </div>
              </div>
            </div>

            <!-- 用户气泡 -->
            <div v-else class="ai-msg ai-msg--user">
              <div class="ai-msg__body">
                <!-- 上传的图片预览 -->
                <img
                  v-if="msg.uploadedImage"
                  :src="msg.uploadedImage"
                  class="ai-msg__uploaded-img"
                  alt="上传图片"
                />
                <div class="ai-msg__bubble">{{ msg.content }}</div>
              </div>
              <div class="ai-msg__avatar ai-msg__avatar--user">👤</div>
            </div>

          </template>

          <!-- 等待动画（REST 非流式） -->
          <div v-if="chat.loading.value && !typewriterOn" class="ai-msg ai-msg--ai">
            <div class="ai-msg__avatar">🤖</div>
            <div class="ai-msg__body">
              <div class="ai-msg__bubble ai-typing">
                <span /><span /><span />
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="ai-input-area">
          <!-- 自动补全 -->
          <Transition name="ai-suggest">
            <ul v-if="chat.suggestions.value.length" class="ai-suggest" role="listbox">
              <li
                v-for="s in chat.suggestions.value"
                :key="s"
                class="ai-suggest__item"
                role="option"
                @mousedown.prevent="applySuggestion(s)"
              >{{ s }}</li>
            </ul>
          </Transition>

          <!-- 图片上传预览 -->
          <div v-if="pendingImage" class="ai-pending-image">
            <img :src="pendingImage" alt="待发送图片" />
            <button class="ai-pending-image__remove" @click="removePendingImage" title="取消">✕</button>
          </div>

          <div class="ai-input-row">
            <!-- 图片上传按钮 -->
            <label class="ai-upload-btn" title="上传图片">
              📎
              <input
                type="file"
                accept="image/*"
                style="display:none"
                @change="handleImageUpload"
              />
            </label>

            <textarea
              ref="inputEl"
              v-model="inputText"
              class="ai-input"
              :placeholder="inputPlaceholder"
              rows="1"
              :disabled="chat.loading.value"
              maxlength="500"
              @keydown.enter.exact.prevent="handleSend"
              @keydown.enter.shift.exact="() => {}"
              @input="handleInput"
            />
            <button
              class="ai-send-btn"
              :disabled="chat.loading.value || (!inputText.trim() && !pendingImage)"
              @click="handleSend"
            >
              <span v-if="!chat.loading.value">发送</span>
              <span v-else class="ai-send-spinner" />
            </button>
          </div>
          <div class="ai-input-hint">Enter 发送 · Shift+Enter 换行 · 📎 上传图片</div>
        </div>

      </div>
    </Transition>

    <!-- 最小化时的恢复气泡 -->
    <Transition name="ai-fab">
      <button
        v-if="minimized && !open"
        class="ai-fab ai-fab--minimized"
        title="展开 AI 助手"
        @click="minimized = false; openDrawer()"
      >
        <span class="ai-fab__icon">🤖</span>
        <span class="ai-fab__mini-label">{{ moduleName }}</span>
        <span v-if="unread > 0" class="ai-fab__badge">{{ unread > 9 ? '9+' : unread }}</span>
      </button>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { useAiChat } from '@/composables/useAiChat'

// ── Props ─────────────────────────────────────────────────
const props = withDefaults(defineProps<{
  module?: string
  customerId?: number
  greeting?: string
  defaultTypewriter?: boolean
}>(), {
  module: 'selection',
  customerId: undefined,
  greeting: '你好！我是 AI 助手，直接告诉我你想做什么，例如：「我想做一件极简外套」',
  defaultTypewriter: true,
})

const emit = defineEmits<{
  (e: 'imageClick', url: string): void
  (e: 'done', sessionId: string): void
}>()

// ── Module names / placeholders ───────────────────────────
const MODULE_NAMES: Record<string, string> = {
  selection: '选款', design: '设计', product: '商品',
  inventory: '库存', finance: '财务', trend: '趋势', order: '订单',
}
const moduleName = computed(() => MODULE_NAMES[props.module] ?? '助手')

const PLACEHOLDERS: Record<string, string> = {
  selection: '例如：我想做欧美风极简外套…',
  design:    '例如：帮我设计一款宽松工装外套…',
  product:   '例如：外套定什么价合适？',
  inventory: '例如：外套库存还有多少？',
  finance:   '例如：这款的 ROI 是多少？',
  trend:     '例如：最近什么款最火？',
  order:     '例如：查一下最新订单状态',
}
const inputPlaceholder = computed(() => PLACEHOLDERS[props.module] ?? '请输入…')

// ── State ─────────────────────────────────────────────────
const open         = ref(false)
const minimized    = ref(false)
const unread       = ref(0)
const inputText    = ref('')
const pendingImage = ref<string | undefined>()
const typewriterOn = ref(props.defaultTypewriter)
const messagesEl   = ref<HTMLElement>()
const inputEl      = ref<HTMLTextAreaElement>()

// ── Chat composable ────────────────────────────────────────
const chat = useAiChat({
  module:     props.module,
  customerId: props.customerId,
  get typewriter() { return typewriterOn.value },
  scrollEl:   () => messagesEl.value,
  greeting:   props.greeting,
  persistKey: `ai_session_${props.module}`,
})

// ── Open / Close ──────────────────────────────────────────
function openDrawer() {
  open.value   = true
  unread.value = 0
  chat.showGreeting()
  nextTick(() => inputEl.value?.focus())
}

function closeDrawer() {
  open.value    = false
  minimized.value = false
}

async function handleClear() {
  await chat.endSession()
  chat.showGreeting()
}

// ── Send ──────────────────────────────────────────────────
async function handleSend() {
  const text = inputText.value.trim()
  if ((!text && !pendingImage.value) || chat.loading.value) return

  const msgText = text || (pendingImage.value ? '[图片]' : '')
  chat.clearSuggestions()

  // If there's a pending image, attach it to the user message
  if (pendingImage.value) {
    const imgUrl = pendingImage.value
    removePendingImage()
    chat.messages.value.push({
      id:            Date.now().toString(36),
      role:          'user',
      content:       msgText,
      uploadedImage: imgUrl,
    } as any)
    inputText.value = ''
    autoResize()
    // Send with image context
    await chat.send(`[用户上传了一张图片] ${msgText}`.trim())
    return
  }

  inputText.value = ''
  autoResize()
  await chat.send(msgText)
}

// ── Image upload ──────────────────────────────────────────
function handleImageUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    pendingImage.value = ev.target?.result as string
  }
  reader.readAsDataURL(file)
  // Reset input so same file can be re-selected
  ;(e.target as HTMLInputElement).value = ''
}

function removePendingImage() {
  pendingImage.value = undefined
}

// ── Input helpers ─────────────────────────────────────────
function handleInput() {
  chat.updateSuggestions(inputText.value.trim())
  autoResize()
}

function applySuggestion(s: string) {
  inputText.value = s
  chat.clearSuggestions()
  nextTick(() => inputEl.value?.focus())
}

function autoResize() {
  nextTick(() => {
    const el = inputEl.value
    if (!el) return
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 120) + 'px'
  })
}

// ── Unread badge ──────────────────────────────────────────
watch(
  () => chat.messages.value.length,
  (n, o) => { if (!open.value && n > o) unread.value++ }
)

// ── Done event ────────────────────────────────────────────
watch(
  () => chat.messages.value,
  (msgs) => {
    const last = [...msgs].reverse().find(m => m.role === 'ai')
    if (last && !last.streaming && last.images?.length && chat.sessionId.value) {
      emit('done', chat.sessionId.value)
    }
  },
  { deep: true }
)

// ── Text render ───────────────────────────────────────────
function renderText(text: string): string {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/\n/g, '<br />')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
}

onUnmounted(() => { chat.cancel() })
</script>

<style scoped>
/* ── FAB 浮动按钮 ─────────────────────────────────────── */
.ai-fab {
  position: fixed;
  bottom: 28px; right: 28px;
  z-index: 1200;
  display: flex; align-items: center; justify-content: center;
  width: 56px; height: 56px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff; border: none; border-radius: 50%; cursor: pointer;
  box-shadow: 0 4px 20px rgba(99,102,241,0.55);
  transition: transform 0.25s cubic-bezier(.34,1.56,.64,1), box-shadow 0.2s;
  user-select: none; outline: none;
}
.ai-fab:hover { transform: scale(1.12); box-shadow: 0 8px 28px rgba(99,102,241,0.65); }
.ai-fab:active { transform: scale(0.96); }
.ai-fab__icon { font-size: 26px; line-height: 1; }
.ai-fab__ring {
  position: absolute; inset: -4px; border-radius: 50%;
  border: 2px solid rgba(99,102,241,0.4);
  animation: ring-pulse 2.4s ease-out infinite;
}
@keyframes ring-pulse {
  0%   { transform: scale(1);   opacity: 0.6; }
  100% { transform: scale(1.5); opacity: 0; }
}
.ai-fab__badge {
  position: absolute; top: -2px; right: -2px;
  min-width: 20px; height: 20px; border-radius: 10px;
  background: #ef4444; color: #fff; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; padding: 0 4px;
  border: 2px solid #fff;
}
.ai-fab--minimized {
  width: auto; height: 40px; border-radius: 20px;
  padding: 0 14px; gap: 6px; font-size: 14px;
}
.ai-fab__mini-label { font-size: 13px; font-weight: 600; }

/* ── 蒙层 ─────────────────────────────────────────────── */
.ai-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.35);
  z-index: 1201; display: none;
}
@media (max-width: 560px) { .ai-overlay { display: block; } }

/* ── 抽屉 ─────────────────────────────────────────────── */
.ai-drawer {
  position: fixed; bottom: 0; right: 0;
  width: 400px; max-width: 100vw;
  height: min(640px, 94vh);
  display: flex; flex-direction: column;
  background: #fff; border-radius: 20px 20px 0 0;
  box-shadow: -2px -2px 40px rgba(0,0,0,0.18);
  z-index: 1202; overflow: hidden;
}
@media (max-width: 560px) {
  .ai-drawer { width: 100vw; height: 90vh; border-radius: 20px 20px 0 0; }
}

/* ── 标题栏 ─────────────────────────────────────────── */
.ai-drawer__header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff; flex-shrink: 0; gap: 8px;
}
.ai-drawer__header-left  { display: flex; align-items: center; gap: 10px; min-width: 0; }
.ai-drawer__header-right { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
.ai-avatar-dot {
  width: 36px; height: 36px; border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.ai-avatar-dot::before { content: '🤖'; }
.ai-drawer__title  { font-size: 14px; font-weight: 700; white-space: nowrap; }
.ai-drawer__status {
  font-size: 11px; color: rgba(255,255,255,0.8);
  display: flex; align-items: center; gap: 4px; margin-top: 1px;
}
.ai-status-dot {
  width: 6px; height: 6px; border-radius: 50%;
}
.ai-status-dot--online   { background: #4ade80; }
.ai-status-dot--thinking { background: #facc15; animation: blink 0.9s step-end infinite; }

.ai-icon-btn {
  background: rgba(255,255,255,0.15); border: none; color: #fff;
  width: 28px; height: 28px; border-radius: 8px; cursor: pointer;
  font-size: 13px; display: flex; align-items: center; justify-content: center;
  transition: background 0.15s; flex-shrink: 0;
}
.ai-icon-btn:hover     { background: rgba(255,255,255,0.3); }
.ai-icon-btn--close    { font-size: 15px; }

/* ── 消息区 ─────────────────────────────────────────── */
.ai-messages {
  flex: 1; overflow-y: auto; padding: 14px 12px;
  display: flex; flex-direction: column; gap: 12px;
  background: #f6f7fb;
}
.ai-messages::-webkit-scrollbar       { width: 3px; }
.ai-messages::-webkit-scrollbar-track { background: transparent; }
.ai-messages::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 3px; }

/* ── 消息行 ─────────────────────────────────────────── */
.ai-msg {
  display: flex; gap: 8px; align-items: flex-start;
  animation: msg-in 0.22s ease-out;
}
@keyframes msg-in { from { opacity: 0; transform: translateY(6px); } }
.ai-msg--user { flex-direction: row-reverse; }

.ai-msg__avatar {
  width: 32px; height: 32px; border-radius: 50%; background: #e0e7ff;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; flex-shrink: 0; margin-top: 2px;
}
.ai-msg__avatar--user { background: #dcfce7; }

.ai-msg__body {
  display: flex; flex-direction: column; gap: 6px;
  max-width: calc(100% - 48px); min-width: 0;
}

.ai-msg__bubble {
  background: #fff; padding: 9px 13px;
  border-radius: 4px 14px 14px 14px;
  font-size: 13.5px; line-height: 1.65; color: #1f2937;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  word-break: break-word; white-space: pre-wrap;
}
.ai-msg--user .ai-msg__bubble {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff; border-radius: 14px 4px 14px 14px;
  box-shadow: 0 2px 8px rgba(99,102,241,0.28);
}

/* 打字光标 */
.ai-cursor {
  display: inline-block; width: 2px; height: 1.1em; background: #6366f1;
  margin-left: 2px; vertical-align: text-bottom;
  animation: blink 0.75s step-end infinite;
}
@keyframes blink { 50% { opacity: 0; } }

/* 等待三点动画 */
.ai-typing {
  display: flex; align-items: center; gap: 4px; min-height: 32px; padding: 10px 14px;
}
.ai-typing span {
  width: 7px; height: 7px; background: #a5b4fc; border-radius: 50%;
  animation: bounce 1.2s infinite;
}
.ai-typing span:nth-child(2) { animation-delay: 0.2s; }
.ai-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,80%,100% { transform: translateY(0); } 40% { transform: translateY(-6px); } }

/* ── 图片卡片 ──────────────────────────────────────── */
.ai-msg__images { display: flex; flex-wrap: wrap; gap: 7px; max-width: 100%; }
.ai-msg__image-card {
  position: relative; width: 88px; height: 108px;
  border-radius: 10px; overflow: hidden; cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.15s, box-shadow 0.15s;
}
.ai-msg__image-card:hover { transform: scale(1.05); box-shadow: 0 4px 16px rgba(0,0,0,0.18); }
.ai-msg__image-card img   { width: 100%; height: 100%; object-fit: cover; display: block; }
.ai-msg__image-label {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: rgba(0,0,0,0.45); color: #fff;
  font-size: 11px; text-align: center; padding: 3px 0;
}

/* ── 快速回复 ──────────────────────────────────────── */
.ai-msg__quick { display: flex; flex-wrap: wrap; gap: 5px; }
.ai-quick-btn {
  background: #ede9fe; color: #5b21b6; border: 1px solid #c4b5fd;
  border-radius: 20px; padding: 5px 13px; font-size: 12.5px; cursor: pointer;
  transition: background 0.15s, transform 0.1s; white-space: nowrap;
}
.ai-quick-btn:hover:not(:disabled) { background: #ddd6fe; transform: translateY(-1px); }
.ai-quick-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── 上传图片预览 ─────────────────────────────────── */
.ai-msg__uploaded-img {
  width: 120px; height: 100px; object-fit: cover; border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}
.ai-pending-image {
  position: relative; display: inline-flex; margin-bottom: 6px;
}
.ai-pending-image img {
  width: 80px; height: 60px; object-fit: cover; border-radius: 8px;
  border: 2px solid #6366f1;
}
.ai-pending-image__remove {
  position: absolute; top: -6px; right: -6px;
  background: #ef4444; color: #fff; border: none;
  width: 18px; height: 18px; border-radius: 50%; font-size: 10px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
}

/* ── 输入区 ────────────────────────────────────────── */
.ai-input-area {
  padding: 8px 10px 10px; background: #fff;
  border-top: 1px solid #f0f0f4; flex-shrink: 0; position: relative;
}
.ai-input-row { display: flex; gap: 7px; align-items: flex-end; }
.ai-upload-btn {
  background: #f5f3ff; border: none; color: #6366f1; border-radius: 10px;
  width: 36px; height: 36px; font-size: 18px; cursor: pointer; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  align-self: flex-end; transition: background 0.15s;
}
.ai-upload-btn:hover { background: #ede9fe; }
.ai-input {
  flex: 1; border: 1.5px solid #e0e7ff; border-radius: 14px;
  padding: 9px 13px; font-size: 13.5px; line-height: 1.5; resize: none;
  outline: none; color: #1f2937; font-family: inherit;
  transition: border-color 0.2s; max-height: 120px; overflow-y: auto; background: #fafafa;
}
.ai-input:focus  { border-color: #6366f1; background: #fff; }
.ai-input:disabled { background: #f5f5f5; cursor: not-allowed; }
.ai-input-hint { font-size: 11px; color: #d1d5db; margin-top: 3px; padding-left: 2px; }
.ai-send-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff;
  border: none; border-radius: 14px; padding: 9px 16px;
  font-size: 13.5px; font-weight: 600; cursor: pointer; flex-shrink: 0;
  align-self: flex-end; min-width: 58px; min-height: 38px;
  display: flex; align-items: center; justify-content: center;
  transition: opacity 0.2s, transform 0.1s;
}
.ai-send-btn:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
.ai-send-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.ai-send-spinner {
  width: 15px; height: 15px;
  border: 2px solid rgba(255,255,255,0.4); border-top-color: #fff;
  border-radius: 50%; animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 自动补全 ──────────────────────────────────────── */
.ai-suggest {
  position: absolute; bottom: 100%; left: 10px; right: 10px;
  background: #fff; border: 1px solid #e0e7ff; border-radius: 14px;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.1);
  list-style: none; margin: 0 0 4px; padding: 5px 0;
  z-index: 10; max-height: 180px; overflow-y: auto;
}
.ai-suggest__item { padding: 8px 14px; font-size: 13px; color: #374151; cursor: pointer; }
.ai-suggest__item:hover { background: #f5f3ff; color: #5b21b6; }

/* ── 过渡动画 ──────────────────────────────────────── */
.ai-fab-enter-active, .ai-fab-leave-active { transition: transform 0.28s cubic-bezier(.34,1.56,.64,1), opacity 0.22s; }
.ai-fab-enter-from,   .ai-fab-leave-to    { transform: scale(0.5) translateY(12px); opacity: 0; }

.ai-overlay-enter-active, .ai-overlay-leave-active { transition: opacity 0.25s; }
.ai-overlay-enter-from,   .ai-overlay-leave-to     { opacity: 0; }

.ai-drawer-enter-active, .ai-drawer-leave-active {
  transition: transform 0.34s cubic-bezier(0.4,0,0.2,1), opacity 0.3s;
}
.ai-drawer-enter-from, .ai-drawer-leave-to { transform: translateY(100%); opacity: 0; }

.ai-suggest-enter-active, .ai-suggest-leave-active { transition: opacity 0.15s, transform 0.15s; }
.ai-suggest-enter-from,   .ai-suggest-leave-to     { opacity: 0; transform: translateY(5px); }
</style>
