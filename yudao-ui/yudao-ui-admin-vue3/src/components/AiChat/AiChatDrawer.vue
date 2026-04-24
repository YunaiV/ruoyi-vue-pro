<template>
  <Teleport to="body">
    <!-- ═══════════════════════════════════════════════════════
         ANIMATED FAB  (only shown when drawer is closed)
    ═══════════════════════════════════════════════════════ -->
    <AiChatFab
      v-if="!open"
      :open="open"
      :loading="chat.loading.value"
      :streaming="isStreaming"
      :unread="unread"
      :module="module"
      ref="fabRef"
      @toggle="openDrawer"
      @voice-input="handleVoiceInput"
    />

    <!-- ═══════════════════════════════════════════════════════
         BACKDROP  (mobile only)
    ═══════════════════════════════════════════════════════ -->
    <Transition name="ai-overlay">
      <div v-if="open" class="ai-overlay" @click="closeDrawer" />
    </Transition>

    <!-- ═══════════════════════════════════════════════════════
         CHAT DRAWER
    ═══════════════════════════════════════════════════════ -->
    <Transition name="ai-drawer">
      <div v-if="open" class="ai-drawer">

        <!-- ── Header ──────────────────────────────────────── -->
        <header class="ai-header" :class="`ai-header--${drawerState}`">
          <div class="ai-header-left">
            <!-- Animated avatar -->
            <div class="ai-avatar" :class="`ai-avatar--${drawerState}`">
              <Transition name="av-icon" mode="out-in">
                <span v-if="drawerState==='thinking'"   key="t" class="av-spin">⚙️</span>
                <span v-else-if="drawerState==='responding'" key="r">✨</span>
                <span v-else                             key="i">🤖</span>
              </Transition>
            </div>
            <div class="ai-header-info">
              <span class="ai-header-title">AI 助手 · {{ moduleName }}</span>
              <span class="ai-header-status">
                <span class="ai-dot" :class="`ai-dot--${drawerState}`" />
                {{ statusText }}
              </span>
            </div>
          </div>

          <div class="ai-header-right">
            <!-- Typewriter toggle -->
            <button
              class="ai-hbtn"
              :title="typewriterOn ? '切换为⚡即时模式' : '切换为⌨️打字机模式'"
              @click="typewriterOn = !typewriterOn"
            >{{ typewriterOn ? '⌨️' : '⚡' }}</button>

            <!-- Voice input -->
            <button
              class="ai-hbtn"
              :class="{ 'ai-hbtn--active': voiceActive }"
              title="语音输入（长按说话）"
              @mousedown="startVoice"
              @mouseup="stopVoice"
              @touchstart.prevent="startVoice"
              @touchend.prevent="stopVoice"
            >🎤</button>

            <!-- Clear -->
            <button class="ai-hbtn" title="清空对话" @click="handleClear">🗑</button>

            <!-- Minimize -->
            <button class="ai-hbtn" title="最小化" @click="open = false">－</button>

            <!-- Close -->
            <button class="ai-hbtn ai-hbtn--close" title="关闭并结束会话" @click="closeDrawer">✕</button>
          </div>
        </header>

        <!-- ── Messages ────────────────────────────────────── -->
        <div class="ai-msgs" ref="msgsEl">
          <template v-for="msg in chat.messages.value" :key="msg.id">

            <!-- AI bubble -->
            <div v-if="msg.role==='ai'" class="ai-row ai-row--ai">
              <div class="ai-row-avatar">
                <Transition name="av-icon" mode="out-in">
                  <span v-if="msg.streaming" key="s" class="av-spin">✨</span>
                  <span v-else key="d">🤖</span>
                </Transition>
              </div>
              <div class="ai-row-body">
                <div class="ai-bubble ai-bubble--ai">
                  <span v-html="renderText(msg.content)" />
                  <span v-if="msg.streaming" class="ai-cursor" />
                </div>

                <!-- Image cards -->
                <div v-if="msg.images?.length" class="ai-img-grid">
                  <div
                    v-for="(url,i) in msg.images" :key="i"
                    class="ai-img-card"
                    @click="emit('imageClick', url)"
                    :title="`推荐款 ${i+1} — 点击查看`"
                  >
                    <img :src="url" :alt="`推荐款 ${i+1}`" loading="lazy" />
                    <div class="ai-img-card-label">款 {{ i+1 }}</div>
                  </div>
                </div>

                <!-- Quick replies -->
                <div v-if="msg.quickReplies?.length && !msg.streaming" class="ai-quick-row">
                  <button
                    v-for="r in msg.quickReplies" :key="r"
                    class="ai-qr-btn"
                    :disabled="chat.loading.value"
                    @click="chat.quickReply(r)"
                  >{{ r }}</button>
                </div>
              </div>
            </div>

            <!-- User bubble -->
            <div v-else class="ai-row ai-row--user">
              <div class="ai-row-body ai-row-body--user">
                <img
                  v-if="(msg as any).uploadedImage"
                  :src="(msg as any).uploadedImage"
                  class="ai-uploaded-img"
                  alt="上传图片"
                />
                <div class="ai-bubble ai-bubble--user">{{ msg.content }}</div>
              </div>
              <div class="ai-row-avatar ai-row-avatar--user">👤</div>
            </div>

          </template>

          <!-- REST loading dots -->
          <div v-if="chat.loading.value && !typewriterOn" class="ai-row ai-row--ai">
            <div class="ai-row-avatar">🤖</div>
            <div class="ai-row-body">
              <div class="ai-bubble ai-bubble--ai ai-typing">
                <span /><span /><span />
              </div>
            </div>
          </div>
        </div>

        <!-- ── Input area ──────────────────────────────────── -->
        <div class="ai-input-wrap">

          <!-- Autocomplete -->
          <Transition name="ai-suggest">
            <ul v-if="chat.suggestions.value.length" class="ai-suggest" role="listbox">
              <li
                v-for="s in chat.suggestions.value" :key="s"
                class="ai-suggest-item"
                role="option"
                @mousedown.prevent="applySuggestion(s)"
              >{{ s }}</li>
            </ul>
          </Transition>

          <!-- Pending image preview -->
          <div v-if="pendingImg" class="ai-pending-img">
            <img :src="pendingImg" alt="待发送图片" />
            <button @click="pendingImg=undefined" title="取消">✕</button>
          </div>

          <!-- Voice indicator -->
          <div v-if="voiceActive" class="ai-voice-bar">
            <span class="ai-voice-bar-wave"><span/><span/><span/><span/><span/></span>
            正在聆听… 松开停止
          </div>

          <div class="ai-input-row">
            <!-- Image upload -->
            <label class="ai-tool-btn" title="上传图片">
              <span>📎</span>
              <input type="file" accept="image/*" style="display:none" @change="onImgUpload" />
            </label>

            <textarea
              ref="inputEl"
              v-model="inputText"
              class="ai-input"
              :placeholder="placeholder"
              rows="1"
              :disabled="chat.loading.value"
              maxlength="500"
              @keydown.enter.exact.prevent="handleSend"
              @keydown.enter.shift.exact="() => {}"
              @input="onInput"
            />

            <button
              class="ai-send-btn"
              :class="{ 'ai-send-btn--ready': !chat.loading.value && (!!inputText.trim() || !!pendingImg) }"
              :disabled="chat.loading.value || (!inputText.trim() && !pendingImg)"
              @click="handleSend"
            >
              <Transition name="send-icon" mode="out-in">
                <span v-if="chat.loading.value" key="l" class="ai-spinner" />
                <span v-else key="s">发送</span>
              </Transition>
            </button>
          </div>
          <div class="ai-input-hint">Enter 发送 · Shift+Enter 换行 · 📎 图片 · 🎤 长按语音</div>
        </div>

      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import AiChatFab from './AiChatFab.vue'
import { useAiChat } from '@/composables/useAiChat'
import type { ChatMessage } from '@/composables/useAiChat'

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

// ── Module helpers ────────────────────────────────────────
const MODULE_NAMES: Record<string,string> = {
  selection:'选款', design:'设计', product:'商品',
  inventory:'库存', finance:'财务', trend:'趋势', order:'订单',
}
const PLACEHOLDERS: Record<string,string> = {
  selection:'例如：我想做欧美风极简外套…',
  design:   '例如：帮我设计一款宽松工装外套…',
  product:  '例如：外套定什么价合适？',
  inventory:'例如：外套库存还有多少？',
  finance:  '例如：这款的 ROI 是多少？',
  trend:    '例如：最近什么款最火？',
  order:    '例如：查一下最新订单状态',
}
const moduleName = computed(() => MODULE_NAMES[props.module] ?? '助手')
const placeholder = computed(() => PLACEHOLDERS[props.module] ?? '请输入…')

// ── State ─────────────────────────────────────────────────
const open         = ref(false)
const unread       = ref(0)
const inputText    = ref('')
const pendingImg   = ref<string | undefined>()
const typewriterOn = ref(props.defaultTypewriter)
const msgsEl       = ref<HTMLElement>()
const inputEl      = ref<HTMLTextAreaElement>()
const fabRef       = ref<InstanceType<typeof AiChatFab>>()
const voiceActive  = ref(false)

// ── Chat composable ────────────────────────────────────────
const chat = useAiChat({
  module:     props.module,
  customerId: props.customerId,
  get typewriter() { return typewriterOn.value },
  scrollEl:   () => msgsEl.value,
  greeting:   props.greeting,
  persistKey: `ai_session_${props.module}`,
})

// ── Derived drawer state ───────────────────────────────────
const isStreaming = computed(() =>
  chat.messages.value.some(m => m.role === 'ai' && (m as any).streaming)
)
const drawerState = computed<'idle'|'thinking'|'responding'>(() => {
  if (chat.loading.value && !isStreaming.value) return 'thinking'
  if (isStreaming.value) return 'responding'
  return 'idle'
})
const statusText = computed(() => {
  if (drawerState.value === 'thinking')   return 'AI 正在思考…'
  if (drawerState.value === 'responding') return 'AI 正在回复…'
  return '在线'
})

// ── Open / Close ──────────────────────────────────────────
function openDrawer() {
  open.value   = true
  unread.value = 0
  chat.showGreeting()
  nextTick(() => inputEl.value?.focus())
}

function closeDrawer() {
  open.value = false
}

async function handleClear() {
  await chat.endSession()
  chat.showGreeting()
}

// ── Send ──────────────────────────────────────────────────
async function handleSend() {
  const text = inputText.value.trim()
  if ((!text && !pendingImg.value) || chat.loading.value) return
  chat.clearSuggestions()

  if (pendingImg.value) {
    const imgData = pendingImg.value
    pendingImg.value = undefined
    const fullText = text ? `[图片] ${text}` : '[用户上传了一张图片，请根据图片内容给出建议]'
    // Attach image to user message visually
    chat.messages.value.push({
      id: Date.now().toString(36), role: 'user',
      content: text || '[图片]',
      uploadedImage: imgData,
    } as ChatMessage & { uploadedImage: string })
    inputText.value = ''
    autoResize()
    await chat.send(fullText)
    return
  }

  inputText.value = ''
  autoResize()
  await chat.send(text)
}

// ── Voice ─────────────────────────────────────────────────
function startVoice() { fabRef.value?.startVoice(); voiceActive.value = true }
function stopVoice()  { fabRef.value?.stopVoice();  voiceActive.value = false }

function handleVoiceInput(text: string) {
  voiceActive.value = false
  inputText.value = text
  nextTick(() => handleSend())
}

// ── Image upload ──────────────────────────────────────────
function onImgUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = ev => { pendingImg.value = ev.target?.result as string }
  reader.readAsDataURL(file)
  ;(e.target as HTMLInputElement).value = ''
}

// ── Input helpers ─────────────────────────────────────────
function onInput() {
  chat.updateSuggestions(inputText.value.trim())
  autoResize()
}
function applySuggestion(s: string) {
  inputText.value = s; chat.clearSuggestions()
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
watch(() => chat.messages.value.length, (n, o) => {
  if (!open.value && n > o) unread.value++
})

// ── Done event ────────────────────────────────────────────
watch(() => chat.messages.value, msgs => {
  const last = [...msgs].reverse().find(m => m.role==='ai')
  if (last && !(last as any).streaming && last.images?.length && chat.sessionId.value)
    emit('done', chat.sessionId.value)
}, { deep: true })

// ── Text renderer ─────────────────────────────────────────
function renderText(t: string): string {
  if (!t) return ''
  return t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
          .replace(/\n/g,'<br />')
          .replace(/\*\*(.+?)\*\*/g,'<strong>$1</strong>')
}

onUnmounted(() => { chat.cancel() })
</script>

<style scoped>
/* ── Overlay ───────────────────────────────────────────── */
.ai-overlay {
  position:fixed; inset:0; background:rgba(0,0,0,0.4);
  z-index:1201; display:none;
}
@media(max-width:580px){ .ai-overlay{display:block;} }

/* ── Drawer ────────────────────────────────────────────── */
.ai-drawer {
  position:fixed; bottom:0; right:0;
  width:420px; max-width:100vw;
  height:min(650px,94vh);
  display:flex; flex-direction:column;
  background:#fff; border-radius:22px 22px 0 0;
  box-shadow:-2px -2px 48px rgba(0,0,0,0.2);
  z-index:1202; overflow:hidden;
}
@media(max-width:580px){ .ai-drawer{width:100vw;height:92vh;} }

/* ── Header ────────────────────────────────────────────── */
.ai-header {
  display:flex; align-items:center; justify-content:space-between;
  padding:12px 14px; flex-shrink:0; color:#fff;
  transition:background 0.5s;
}
.ai-header--idle       { background:linear-gradient(135deg,#6366f1 0%,#8b5cf6 100%); }
.ai-header--thinking   { background:linear-gradient(135deg,#f59e0b 0%,#ef4444 100%); }
.ai-header--responding { background:linear-gradient(135deg,#10b981 0%,#06b6d4 100%); }

.ai-header-left  { display:flex; align-items:center; gap:10px; }
.ai-header-right { display:flex; align-items:center; gap:4px; }

/* Animated header avatar */
.ai-avatar {
  width:38px; height:38px; border-radius:50%;
  background:rgba(255,255,255,0.2);
  display:flex; align-items:center; justify-content:center;
  font-size:20px; flex-shrink:0;
  box-shadow:0 0 0 0 rgba(255,255,255,0.4);
}
.ai-avatar--thinking   { animation:avatar-ring 0.9s ease-in-out infinite; }
.ai-avatar--responding { animation:avatar-glow 1.2s ease-in-out infinite alternate; }
@keyframes avatar-ring {
  0%,100%{ box-shadow:0 0 0 0   rgba(255,255,255,0.5); }
  50%    { box-shadow:0 0 0 10px rgba(255,255,255,0);   }
}
@keyframes avatar-glow {
  from{ box-shadow:0 0 6px  rgba(255,255,255,0.3); }
  to  { box-shadow:0 0 18px rgba(255,255,255,0.7); }
}

.av-spin  { display:inline-block; animation:icon-spin 1.2s linear infinite; }
@keyframes icon-spin{ to{transform:rotate(360deg);} }

.ai-header-title  { font-size:14px; font-weight:700; }
.ai-header-status { font-size:11px; color:rgba(255,255,255,0.8); display:flex; align-items:center; gap:4px; }
.ai-dot {
  width:6px; height:6px; border-radius:50%;
}
.ai-dot--idle       { background:#4ade80; }
.ai-dot--thinking   { background:#fbbf24; animation:blink 0.8s step-end infinite; }
.ai-dot--responding { background:#34d399; animation:blink 0.5s step-end infinite; }
@keyframes blink{ 50%{opacity:0;} }

.ai-hbtn {
  background:rgba(255,255,255,0.15); border:none; color:#fff;
  width:28px; height:28px; border-radius:8px; cursor:pointer;
  font-size:13px; display:flex; align-items:center; justify-content:center;
  transition:background 0.15s;
}
.ai-hbtn:hover         { background:rgba(255,255,255,0.3); }
.ai-hbtn--active       { background:rgba(236,72,153,0.6); animation:voice-ring 0.6s infinite alternate; }
.ai-hbtn--close        { font-size:15px; }
@keyframes voice-ring{ to{box-shadow:0 0 0 4px rgba(236,72,153,0.3);} }

/* ── Messages ──────────────────────────────────────────── */
.ai-msgs {
  flex:1; overflow-y:auto; padding:14px 12px;
  display:flex; flex-direction:column; gap:12px;
  background:#f6f7fb;
}
.ai-msgs::-webkit-scrollbar       { width:3px; }
.ai-msgs::-webkit-scrollbar-track { background:transparent; }
.ai-msgs::-webkit-scrollbar-thumb { background:#d1d5db; border-radius:3px; }

/* ── Message rows ──────────────────────────────────────── */
.ai-row {
  display:flex; gap:8px; align-items:flex-start;
  animation:row-in 0.24s ease-out;
}
@keyframes row-in{ from{opacity:0;transform:translateY(8px);} }
.ai-row--user { flex-direction:row-reverse; }

.ai-row-avatar {
  width:32px; height:32px; border-radius:50%; background:#e0e7ff;
  display:flex; align-items:center; justify-content:center;
  font-size:16px; flex-shrink:0; margin-top:2px;
}
.ai-row-avatar--user { background:#dcfce7; }

.ai-row-body { display:flex; flex-direction:column; gap:6px; max-width:calc(100% - 44px); }
.ai-row-body--user { align-items:flex-end; }

/* ── Bubbles ───────────────────────────────────────────── */
.ai-bubble {
  padding:9px 13px; border-radius:4px 14px 14px 14px;
  font-size:13.5px; line-height:1.65; word-break:break-word; white-space:pre-wrap;
}
.ai-bubble--ai {
  background:#fff; color:#1f2937;
  box-shadow:0 1px 4px rgba(0,0,0,0.07);
}
.ai-bubble--user {
  background:linear-gradient(135deg,#6366f1,#8b5cf6); color:#fff;
  border-radius:14px 4px 14px 14px;
  box-shadow:0 2px 10px rgba(99,102,241,0.3);
}

/* Typewriter cursor */
.ai-cursor {
  display:inline-block; width:2px; height:1.05em;
  background:#6366f1; margin-left:2px; vertical-align:text-bottom;
  animation:blink 0.75s step-end infinite;
}

/* Loading dots */
.ai-typing {
  display:flex; align-items:center; gap:4px; padding:12px 14px; min-height:38px;
}
.ai-typing span {
  width:7px; height:7px; background:#a5b4fc; border-radius:50%;
  animation:bounce 1.2s infinite;
}
.ai-typing span:nth-child(2){ animation-delay:0.2s; }
.ai-typing span:nth-child(3){ animation-delay:0.4s; }
@keyframes bounce{ 0%,80%,100%{transform:translateY(0);} 40%{transform:translateY(-7px);} }

/* ── Image grid ────────────────────────────────────────── */
.ai-img-grid { display:flex; flex-wrap:wrap; gap:7px; max-width:100%; }
.ai-img-card {
  position:relative; width:86px; height:106px;
  border-radius:10px; overflow:hidden; cursor:pointer;
  box-shadow:0 2px 8px rgba(0,0,0,0.1);
  transition:transform 0.15s,box-shadow 0.15s;
}
.ai-img-card:hover { transform:scale(1.06); box-shadow:0 4px 18px rgba(0,0,0,0.18); }
.ai-img-card img   { width:100%; height:100%; object-fit:cover; display:block; }
.ai-img-card-label {
  position:absolute; bottom:0; left:0; right:0;
  background:rgba(0,0,0,0.45); color:#fff;
  font-size:11px; text-align:center; padding:3px 0;
}

/* ── Quick replies ─────────────────────────────────────── */
.ai-quick-row { display:flex; flex-wrap:wrap; gap:5px; }
.ai-qr-btn {
  background:#ede9fe; color:#5b21b6; border:1px solid #c4b5fd;
  border-radius:20px; padding:5px 13px; font-size:12.5px; cursor:pointer;
  transition:background 0.15s,transform 0.1s; white-space:nowrap;
}
.ai-qr-btn:hover:not(:disabled){ background:#ddd6fe; transform:translateY(-1px); }
.ai-qr-btn:disabled{ opacity:0.4; cursor:not-allowed; }

/* ── Uploaded image ────────────────────────────────────── */
.ai-uploaded-img {
  width:120px; height:90px; object-fit:cover; border-radius:10px;
  box-shadow:0 2px 8px rgba(0,0,0,0.12);
}

/* ── Input area ────────────────────────────────────────── */
.ai-input-wrap {
  padding:8px 10px 10px; background:#fff;
  border-top:1px solid #f0f0f4; flex-shrink:0; position:relative;
}
.ai-input-row { display:flex; gap:6px; align-items:flex-end; }

.ai-tool-btn {
  background:#f5f3ff; border:none; color:#6366f1; border-radius:10px;
  width:36px; height:36px; font-size:17px; cursor:pointer; flex-shrink:0;
  display:flex; align-items:center; justify-content:center; align-self:flex-end;
  transition:background 0.15s;
}
.ai-tool-btn:hover{ background:#ede9fe; }

.ai-input {
  flex:1; border:1.5px solid #e0e7ff; border-radius:14px;
  padding:9px 12px; font-size:13.5px; line-height:1.5; resize:none; outline:none;
  color:#1f2937; font-family:inherit; background:#fafafa;
  transition:border-color 0.2s; max-height:120px; overflow-y:auto;
}
.ai-input:focus   { border-color:#6366f1; background:#fff; }
.ai-input:disabled{ background:#f5f5f5; cursor:not-allowed; }
.ai-input-hint { font-size:11px; color:#d1d5db; margin-top:3px; padding-left:2px; }

.ai-send-btn {
  background:#e0e7ff; color:#6366f1; border:none; border-radius:14px;
  padding:9px 15px; font-size:13.5px; font-weight:600; cursor:pointer;
  flex-shrink:0; align-self:flex-end; min-width:56px; min-height:38px;
  display:flex; align-items:center; justify-content:center;
  transition:background 0.2s,transform 0.1s,box-shadow 0.2s;
}
.ai-send-btn--ready {
  background:linear-gradient(135deg,#6366f1,#8b5cf6); color:#fff;
  box-shadow:0 3px 12px rgba(99,102,241,0.35);
}
.ai-send-btn--ready:hover{ transform:translateY(-1px); box-shadow:0 5px 18px rgba(99,102,241,0.45); }
.ai-send-btn:disabled{ opacity:0.45; cursor:not-allowed; }

.ai-spinner {
  width:15px; height:15px;
  border:2px solid rgba(255,255,255,0.4); border-top-color:#fff;
  border-radius:50%; animation:spin 0.7s linear infinite;
}
@keyframes spin{ to{transform:rotate(360deg);} }

/* ── Pending image ─────────────────────────────────────── */
.ai-pending-img {
  position:relative; display:inline-flex; margin-bottom:6px;
}
.ai-pending-img img {
  width:72px; height:54px; object-fit:cover; border-radius:8px; border:2px solid #6366f1;
}
.ai-pending-img button {
  position:absolute; top:-6px; right:-6px; background:#ef4444; color:#fff;
  border:none; width:18px; height:18px; border-radius:50%; font-size:10px;
  cursor:pointer; display:flex; align-items:center; justify-content:center;
}

/* ── Voice bar ─────────────────────────────────────────── */
.ai-voice-bar {
  display:flex; align-items:center; gap:10px; margin-bottom:6px;
  background:#fdf4ff; border:1px solid #e9d5ff; border-radius:10px;
  padding:7px 12px; font-size:12.5px; color:#7c3aed;
}
.ai-voice-bar-wave { display:flex; align-items:center; gap:2px; }
.ai-voice-bar-wave span {
  width:3px; border-radius:3px; background:#8b5cf6;
  animation:wave 0.5s ease-in-out infinite alternate;
}
.ai-voice-bar-wave span:nth-child(1){ height:8px;  animation-delay:0s; }
.ai-voice-bar-wave span:nth-child(2){ height:16px; animation-delay:0.1s; }
.ai-voice-bar-wave span:nth-child(3){ height:22px; animation-delay:0.2s; }
.ai-voice-bar-wave span:nth-child(4){ height:16px; animation-delay:0.3s; }
.ai-voice-bar-wave span:nth-child(5){ height:8px;  animation-delay:0.4s; }
@keyframes wave{ to{transform:scaleY(1.8);} }

/* ── Autocomplete ──────────────────────────────────────── */
.ai-suggest {
  position:absolute; bottom:100%; left:10px; right:10px;
  background:#fff; border:1px solid #e0e7ff; border-radius:14px;
  box-shadow:0 -4px 20px rgba(0,0,0,0.1);
  list-style:none; margin:0 0 4px; padding:5px 0;
  z-index:10; max-height:180px; overflow-y:auto;
}
.ai-suggest-item { padding:8px 14px; font-size:13px; color:#374151; cursor:pointer; }
.ai-suggest-item:hover{ background:#f5f3ff; color:#5b21b6; }

/* ── Transitions ───────────────────────────────────────── */
.ai-overlay-enter-active,.ai-overlay-leave-active{ transition:opacity 0.25s; }
.ai-overlay-enter-from,.ai-overlay-leave-to      { opacity:0; }

.ai-drawer-enter-active,.ai-drawer-leave-active{
  transition:transform 0.34s cubic-bezier(0.4,0,0.2,1),opacity 0.3s;
}
.ai-drawer-enter-from,.ai-drawer-leave-to{ transform:translateY(100%); opacity:0; }

.ai-suggest-enter-active,.ai-suggest-leave-active{ transition:opacity 0.15s,transform 0.15s; }
.ai-suggest-enter-from,.ai-suggest-leave-to      { opacity:0; transform:translateY(5px); }

.send-icon-enter-active,.send-icon-leave-active{ transition:opacity 0.15s,transform 0.12s; }
.send-icon-enter-from,.send-icon-leave-to      { opacity:0; transform:scale(0.6); }

.av-icon-enter-active,.av-icon-leave-active{ transition:opacity 0.18s,transform 0.18s; }
.av-icon-enter-from  { opacity:0; transform:scale(0.5) rotate(-30deg); }
.av-icon-leave-to    { opacity:0; transform:scale(0.5) rotate(30deg); }
</style>
