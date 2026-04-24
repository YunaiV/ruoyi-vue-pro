<template>
  <!-- ── 悬浮气泡按钮 ────────────────────────────────────────── -->
  <Teleport to="body">
    <Transition name="fab">
      <button
        v-if="!open"
        class="ai-fab"
        :title="`AI 助手 · ${moduleName}`"
        aria-label="打开 AI 助手"
        @click="openDrawer"
      >
        <span class="ai-fab__icon">🤖</span>
        <span class="ai-fab__label">AI 助手</span>
        <span v-if="unread" class="ai-fab__badge">{{ unread }}</span>
      </button>
    </Transition>

    <!-- ── 蒙层（移动端） ──────────────────────────────────────── -->
    <Transition name="overlay">
      <div v-if="open" class="ai-overlay" @click="closeDrawer" />
    </Transition>

    <!-- ── 对话抽屉 ─────────────────────────────────────────────── -->
    <Transition name="drawer">
      <div v-if="open" class="ai-drawer" role="dialog" :aria-label="`AI 助手 · ${moduleName}`">

        <!-- 标题栏 -->
        <header class="ai-header">
          <span class="ai-header__icon">🤖</span>
          <span class="ai-header__title">AI 助手 · {{ moduleName }}</span>
          <div class="ai-header__actions">
            <button
              class="ai-header__btn"
              :title="typewriterOn ? '关闭打字机效果（当前：开启）' : '开启打字机效果（当前：关闭）'"
              @click="typewriterOn = !typewriterOn"
            >{{ typewriterOn ? '⌨️' : '⚡' }}</button>
            <button class="ai-header__btn" title="清空对话" @click="handleClear">🗑</button>
            <button class="ai-header__btn ai-header__btn--close" title="关闭" @click="closeDrawer">✕</button>
          </div>
        </header>

        <!-- 消息列表 -->
        <div class="ai-messages" ref="messagesEl">
          <template v-for="msg in chat.messages.value" :key="msg.id">

            <!-- AI 气泡 -->
            <div v-if="msg.role === 'ai'" class="ai-bubble ai-bubble--ai">
              <div class="ai-bubble__avatar" aria-hidden="true">🤖</div>
              <div class="ai-bubble__body">
                <div class="ai-bubble__text">
                  <span v-html="renderText(msg.content)" />
                  <span v-if="msg.streaming" class="ai-cursor" aria-hidden="true" />
                </div>

                <!-- 图片卡片 -->
                <div v-if="msg.images && msg.images.length" class="ai-images">
                  <img
                    v-for="(url, i) in msg.images"
                    :key="i"
                    :src="url"
                    class="ai-images__item"
                    loading="lazy"
                    :alt="`推荐款 ${i + 1}`"
                    @click="emit('imageClick', url)"
                  />
                </div>

                <!-- 快速回复 -->
                <div v-if="msg.quickReplies && msg.quickReplies.length && !msg.streaming" class="ai-quick">
                  <button
                    v-for="r in msg.quickReplies"
                    :key="r"
                    class="ai-quick__btn"
                    :disabled="chat.loading.value"
                    @click="chat.quickReply(r)"
                  >{{ r }}</button>
                </div>
              </div>
            </div>

            <!-- 用户气泡 -->
            <div v-else class="ai-bubble ai-bubble--user">
              <div class="ai-bubble__body">
                <div class="ai-bubble__text">{{ msg.content }}</div>
              </div>
              <div class="ai-bubble__avatar" aria-hidden="true">👤</div>
            </div>
          </template>

          <!-- 等待动画（REST 非流式模式） -->
          <div v-if="chat.loading.value && !typewriterOn" class="ai-bubble ai-bubble--ai">
            <div class="ai-bubble__avatar">🤖</div>
            <div class="ai-bubble__body">
              <div class="ai-typing"><span /><span /><span /></div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="ai-input-area">
          <Transition name="suggest">
            <ul v-if="chat.suggestions.value.length" class="ai-suggest" role="listbox">
              <li
                v-for="s in chat.suggestions.value"
                :key="s"
                class="ai-suggest__item"
                role="option"
                @click="applySuggestion(s)"
              >{{ s }}</li>
            </ul>
          </Transition>

          <div class="ai-input-row">
            <textarea
              ref="inputEl"
              v-model="inputText"
              class="ai-input"
              :placeholder="inputPlaceholder"
              rows="1"
              :disabled="chat.loading.value"
              maxlength="500"
              @keydown.enter.exact.prevent="handleSend"
              @input="handleInput"
            />
            <button
              class="ai-send-btn"
              :disabled="chat.loading.value || !inputText.trim()"
              @click="handleSend"
            >
              <span v-if="!chat.loading.value">发送</span>
              <span v-else class="ai-send-btn__spinner" />
            </button>
          </div>
          <div class="ai-input-hint">Enter 发送 · Shift+Enter 换行</div>
        </div>

      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { useAiChat } from '@/composables/useAiChat'

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

const MODULE_NAMES: Record<string, string> = {
  selection: '选款', design: '设计', product: '商品',
  inventory: '库存', finance: '财务', trend: '趋势', order: '订单',
}
const moduleName = computed(() => MODULE_NAMES[props.module] ?? '助手')

const INPUT_PLACEHOLDERS: Record<string, string> = {
  selection: '例如：我想做欧美风极简外套…',
  design:    '例如：帮我设计一款宽松工装外套…',
  product:   '例如：外套定什么价合适？',
  inventory: '例如：外套库存还有多少？',
  finance:   '例如：这款的 ROI 是多少？',
  trend:     '例如：最近什么款最火？',
  order:     '例如：查一下最新订单状态',
}
const inputPlaceholder = computed(() => INPUT_PLACEHOLDERS[props.module] ?? '请输入…')

const open         = ref(false)
const unread       = ref(0)
const inputText    = ref('')
const typewriterOn = ref(props.defaultTypewriter)
const messagesEl   = ref<HTMLElement>()
const inputEl      = ref<HTMLTextAreaElement>()

const chat = useAiChat({
  module:     props.module,
  customerId: props.customerId,
  get typewriter() { return typewriterOn.value },
  scrollEl:   () => messagesEl.value,
})

function openDrawer() {
  open.value   = true
  unread.value = 0
  if (chat.messages.value.length === 0) {
    chat.messages.value.push({ id: 'greeting', role: 'ai', content: props.greeting })
  }
  nextTick(() => inputEl.value?.focus())
}

function closeDrawer() { open.value = false }

async function handleClear() { await chat.endSession() }

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || chat.loading.value) return
  chat.clearSuggestions()
  inputText.value = ''
  autoResizeInput()
  await chat.send(text)
}

function handleInput() {
  chat.updateSuggestions(inputText.value.trim())
  autoResizeInput()
}

function applySuggestion(s: string) {
  inputText.value = s
  chat.clearSuggestions()
  inputEl.value?.focus()
}

function autoResizeInput() {
  nextTick(() => {
    const el = inputEl.value
    if (!el) return
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 120) + 'px'
  })
}

watch(
  () => chat.messages.value.length,
  (n, o) => { if (!open.value && n > o) unread.value++ }
)

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
.ai-fab {
  position: fixed; bottom: 32px; right: 32px; z-index: 1200;
  display: flex; align-items: center; gap: 6px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff; border: none; border-radius: 50px; cursor: pointer;
  box-shadow: 0 4px 24px rgba(99,102,241,0.5);
  font-size: 14px; font-weight: 600; user-select: none;
  transition: transform 0.2s, box-shadow 0.2s;
}
.ai-fab:hover { transform: translateY(-3px); box-shadow: 0 8px 32px rgba(99,102,241,0.6); }
.ai-fab:active { transform: translateY(0); }
.ai-fab__icon  { font-size: 20px; }
.ai-fab__badge {
  position: absolute; top: -4px; right: -4px;
  min-width: 20px; height: 20px; background: #ef4444; color: #fff;
  border-radius: 10px; font-size: 11px;
  display: flex; align-items: center; justify-content: center; padding: 0 4px;
}

.ai-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.35);
  z-index: 1201; display: none;
}
@media (max-width: 520px) { .ai-overlay { display: block; } }

.ai-drawer {
  position: fixed; bottom: 0; right: 0;
  width: 420px; max-width: 100vw;
  height: min(620px, 92vh);
  display: flex; flex-direction: column;
  background: #fff; border-radius: 20px 20px 0 0;
  box-shadow: -4px 0 48px rgba(0,0,0,0.16);
  z-index: 1202; overflow: hidden;
}
@media (max-width: 520px) { .ai-drawer { width: 100vw; } }

.ai-header {
  display: flex; align-items: center; gap: 8px; padding: 14px 16px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff; flex-shrink: 0;
}
.ai-header__icon  { font-size: 20px; }
.ai-header__title { flex: 1; font-size: 15px; font-weight: 700; }
.ai-header__actions { display: flex; gap: 4px; }
.ai-header__btn {
  background: rgba(255,255,255,0.15); border: none; color: #fff;
  width: 30px; height: 30px; border-radius: 8px; cursor: pointer;
  font-size: 14px; display: flex; align-items: center; justify-content: center;
  transition: background 0.15s;
}
.ai-header__btn:hover { background: rgba(255,255,255,0.3); }
.ai-header__btn--close { font-size: 16px; }

.ai-messages {
  flex: 1; overflow-y: auto; padding: 16px 14px;
  display: flex; flex-direction: column; gap: 14px;
  background: #f8f9fc; scroll-behavior: smooth;
}
.ai-messages::-webkit-scrollbar { width: 4px; }
.ai-messages::-webkit-scrollbar-track { background: transparent; }
.ai-messages::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 4px; }

.ai-bubble { display: flex; gap: 10px; max-width: 88%; align-items: flex-start; }
.ai-bubble--user { align-self: flex-end; flex-direction: row-reverse; }

.ai-bubble__avatar {
  width: 34px; height: 34px; border-radius: 50%; background: #e0e7ff;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px; flex-shrink: 0; margin-top: 2px;
}
.ai-bubble--user .ai-bubble__avatar { background: #dcfce7; }

.ai-bubble__body { display: flex; flex-direction: column; gap: 8px; min-width: 0; }

.ai-bubble__text {
  background: #fff; padding: 10px 14px;
  border-radius: 4px 14px 14px 14px;
  font-size: 14px; line-height: 1.65; color: #1f2937;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  word-break: break-word; white-space: pre-wrap; position: relative;
}
.ai-bubble--user .ai-bubble__text {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff; border-radius: 14px 4px 14px 14px;
  box-shadow: 0 2px 8px rgba(99,102,241,0.3);
}

.ai-cursor {
  display: inline-block; width: 2px; height: 1em; background: #6366f1;
  margin-left: 2px; vertical-align: text-bottom;
  animation: blink 0.8s step-end infinite;
}
@keyframes blink { 50% { opacity: 0; } }

.ai-images { display: flex; flex-wrap: wrap; gap: 8px; max-width: 320px; }
.ai-images__item {
  width: 96px; height: 116px; object-fit: cover; border-radius: 10px;
  cursor: pointer; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.15s, box-shadow 0.15s;
}
.ai-images__item:hover { transform: scale(1.05); box-shadow: 0 4px 16px rgba(0,0,0,0.18); }

.ai-quick { display: flex; flex-wrap: wrap; gap: 6px; }
.ai-quick__btn {
  background: #ede9fe; color: #5b21b6; border: 1px solid #c4b5fd;
  border-radius: 20px; padding: 5px 14px; font-size: 13px; cursor: pointer;
  transition: background 0.15s, transform 0.1s; white-space: nowrap;
}
.ai-quick__btn:hover:not(:disabled) { background: #ddd6fe; transform: translateY(-1px); }
.ai-quick__btn:disabled { opacity: 0.45; cursor: not-allowed; }

.ai-typing {
  display: flex; align-items: center; gap: 5px; padding: 12px 16px;
  background: #fff; border-radius: 4px 14px 14px 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.ai-typing span {
  width: 7px; height: 7px; background: #a5b4fc; border-radius: 50%;
  animation: bounce 1.2s infinite;
}
.ai-typing span:nth-child(2) { animation-delay: 0.2s; }
.ai-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,80%,100% { transform: translateY(0); } 40% { transform: translateY(-7px); } }

.ai-input-area {
  padding: 10px 12px 12px; background: #fff;
  border-top: 1px solid #f0f0f4; flex-shrink: 0; position: relative;
}
.ai-input-row { display: flex; gap: 8px; align-items: flex-end; }
.ai-input {
  flex: 1; border: 1.5px solid #e0e7ff; border-radius: 14px;
  padding: 10px 14px; font-size: 14px; line-height: 1.5;
  resize: none; outline: none; color: #1f2937; font-family: inherit;
  transition: border-color 0.2s; max-height: 120px; overflow-y: auto; background: #fafafa;
}
.ai-input:focus { border-color: #6366f1; background: #fff; }
.ai-input:disabled { background: #f5f5f5; cursor: not-allowed; }
.ai-input-hint { font-size: 11px; color: #d1d5db; margin-top: 4px; padding-left: 4px; }

.ai-send-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff; border: none; border-radius: 14px;
  padding: 10px 18px; font-size: 14px; font-weight: 600; cursor: pointer;
  flex-shrink: 0; align-self: flex-end;
  transition: opacity 0.2s, transform 0.1s;
  min-width: 62px; min-height: 42px;
  display: flex; align-items: center; justify-content: center;
}
.ai-send-btn:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
.ai-send-btn:disabled { opacity: 0.45; cursor: not-allowed; }
.ai-send-btn__spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.4); border-top-color: #fff;
  border-radius: 50%; animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.ai-suggest {
  position: absolute; bottom: 100%; left: 12px; right: 12px;
  background: #fff; border: 1px solid #e0e7ff; border-radius: 14px;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.1);
  list-style: none; margin: 0 0 4px; padding: 6px 0;
  z-index: 10; max-height: 180px; overflow-y: auto;
}
.ai-suggest__item { padding: 9px 16px; font-size: 13px; color: #374151; cursor: pointer; }
.ai-suggest__item:hover { background: #f5f3ff; color: #5b21b6; }

.fab-enter-active, .fab-leave-active { transition: transform 0.25s, opacity 0.25s; }
.fab-enter-from, .fab-leave-to { transform: scale(0.7) translateY(10px); opacity: 0; }

.overlay-enter-active, .overlay-leave-active { transition: opacity 0.25s; }
.overlay-enter-from, .overlay-leave-to { opacity: 0; }

.drawer-enter-active, .drawer-leave-active {
  transition: transform 0.32s cubic-bezier(0.4,0,0.2,1), opacity 0.32s;
}
.drawer-enter-from, .drawer-leave-to { transform: translateY(100%); opacity: 0; }

.suggest-enter-active, .suggest-leave-active { transition: opacity 0.15s, transform 0.15s; }
.suggest-enter-from, .suggest-leave-to { opacity: 0; transform: translateY(4px); }
</style>
