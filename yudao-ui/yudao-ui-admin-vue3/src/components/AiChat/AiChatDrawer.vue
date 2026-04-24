<template>
  <Teleport to="body">
    <!-- ══════════════════════════════════════════════════════
         智能 FAB（抽屉关闭时显示）
    ══════════════════════════════════════════════════════ -->
    <AiChatFab
      v-if="!open"
      :open="open"
      :loading="chat.loading.value"
      :streaming="isStreaming"
      :tts-active="tts.speaking.value"
      :unread="unread"
      :module="module"
      ref="fabRef"
      @toggle="openDrawer"
      @voice-input="handleVoiceInput"
    />

    <!-- ══════════════════════════════════════════════════════
         半透明背景蒙层（移动端）
    ══════════════════════════════════════════════════════ -->
    <Transition name="overlay">
      <div v-if="open" class="backdrop" @click="closeDrawer" />
    </Transition>

    <!-- ══════════════════════════════════════════════════════
         聊天抽屉
    ══════════════════════════════════════════════════════ -->
    <Transition name="drawer">
      <div v-if="open" class="drawer">

        <!-- ── 标题栏 ──────────────────────────────────────── -->
        <header class="drawer-header" :class="`drawer-header--${state}`">
          <!-- 状态头像 -->
          <div class="header-avatar" :class="`header-avatar--${state}`">
            <Transition name="av" mode="out-in">
              <span v-if="state==='thinking'"   key="t" class="av-spin">⚙️</span>
              <span v-else-if="state==='responding'" key="r" class="av-wave">✨</span>
              <span v-else-if="tts.speaking.value"   key="s" class="av-speak">🔊</span>
              <span v-else                       key="i">🤖</span>
            </Transition>
          </div>

          <div class="header-info">
            <span class="header-title">AI 助手 · {{ moduleName }}</span>
            <span class="header-status">
              <span class="status-dot" :class="`status-dot--${state}`" />
              {{ statusLabel }}
            </span>
          </div>

          <!-- 工具栏 -->
          <div class="header-tools">
            <!-- 打字机切换 -->
            <button
              class="hbtn"
              :title="typewriterOn ? '⚡ 切换即时模式' : '⌨️ 切换打字机模式'"
              @click="typewriterOn = !typewriterOn"
            >{{ typewriterOn ? '⌨️' : '⚡' }}</button>

            <!-- TTS 主开关 -->
            <button
              class="hbtn" :class="{ 'hbtn--on': ttsEnabled }"
              :title="ttsEnabled ? '关闭语音播报' : '开启语音播报'"
              @click="toggleTts"
            >{{ ttsEnabled ? '🔊' : '🔇' }}</button>

            <!-- 语音输入 -->
            <button
              class="hbtn" :class="{ 'hbtn--voice': voiceActive }"
              title="语音输入（长按说话）"
              @mousedown="startVoice"
              @mouseup="stopVoice"
              @touchstart.prevent="startVoice"
              @touchend.prevent="stopVoice"
            >🎤</button>

            <!-- 清空 -->
            <button class="hbtn" title="清空对话" @click="handleClear">🗑</button>

            <!-- 最小化 -->
            <button class="hbtn" title="最小化" @click="open = false">－</button>

            <!-- 关闭 -->
            <button class="hbtn hbtn--close" title="关闭并结束会话" @click="closeDrawer">✕</button>
          </div>
        </header>

        <!-- ── 消息列表 ────────────────────────────────────── -->
        <div class="messages" ref="msgsEl">
          <template v-for="msg in chat.messages.value" :key="msg.id">

            <!-- AI 气泡 -->
            <div v-if="msg.role==='ai'" class="row row--ai">
              <div class="row-avatar">
                <Transition name="av" mode="out-in">
                  <span v-if="(msg as any).streaming" key="s" class="av-spin">✨</span>
                  <span v-else key="d">🤖</span>
                </Transition>
              </div>

              <div class="row-body">
                <div class="bubble bubble--ai">
                  <span v-html="renderText(msg.content)" />
                  <span v-if="(msg as any).streaming" class="cursor" />
                </div>

                <!-- TTS + 时间戳工具栏 -->
                <div v-if="!((msg as any).streaming) && msg.content" class="msg-tools">
                  <button
                    v-if="tts.supported.value"
                    class="tts-btn"
                    :class="{ 'tts-btn--playing': tts.speaking.value && currentTtsId === msg.id }"
                    :title="tts.speaking.value && currentTtsId === msg.id ? '停止播放' : '朗读此条回复'"
                    @click="toggleSpeakMsg(msg.id, msg.content)"
                  >
                    <span v-if="tts.speaking.value && currentTtsId === msg.id">⏹</span>
                    <span v-else>🔊</span>
                  </button>
                  <span class="msg-time">{{ formatTime(msg.id) }}</span>
                </div>

                <!-- 图片卡片 -->
                <div v-if="msg.images?.length" class="img-grid">
                  <div
                    v-for="(url,i) in msg.images" :key="i"
                    class="img-card"
                    @click="emit('imageClick', url)"
                    :title="`推荐款 ${i+1}`"
                  >
                    <img :src="url" :alt="`推荐款 ${i+1}`" loading="lazy" />
                    <div class="img-card-label">款 {{ i+1 }}</div>
                  </div>
                </div>

                <!-- 快速回复 -->
                <div v-if="msg.quickReplies?.length && !((msg as any).streaming)" class="quick-row">
                  <button
                    v-for="r in msg.quickReplies" :key="r"
                    class="qr-btn"
                    :disabled="chat.loading.value"
                    @click="chat.quickReply(r)"
                  >{{ r }}</button>
                </div>
              </div>
            </div>

            <!-- 用户气泡 -->
            <div v-else class="row row--user">
              <div class="row-body row-body--user">
                <img
                  v-if="(msg as any).uploadedImage"
                  :src="(msg as any).uploadedImage"
                  class="uploaded-img" alt="上传图片"
                />
                <div class="bubble bubble--user">{{ msg.content }}</div>
              </div>
              <div class="row-avatar row-avatar--user">👤</div>
            </div>

          </template>

          <!-- REST 等待动画 -->
          <div v-if="chat.loading.value && !typewriterOn" class="row row--ai">
            <div class="row-avatar">🤖</div>
            <div class="row-body">
              <div class="bubble bubble--ai typing">
                <span /><span /><span />
              </div>
            </div>
          </div>

          <!-- 底部锚点 -->
          <div ref="bottomAnchor" style="height:1px" />
        </div>

        <!-- ── 输入区 ──────────────────────────────────────── -->
        <div class="input-area">

          <!-- 自动补全浮层 -->
          <Transition name="suggest">
            <ul v-if="chat.suggestions.value.length" class="suggest" role="listbox">
              <li
                v-for="s in chat.suggestions.value" :key="s"
                class="suggest-item" role="option"
                @mousedown.prevent="applySuggestion(s)"
              >{{ s }}</li>
            </ul>
          </Transition>

          <!-- 待发送图片预览 -->
          <div v-if="pendingImg" class="pending-img">
            <img :src="pendingImg" alt="待发送" />
            <button @click="pendingImg=undefined" title="取消">✕</button>
          </div>

          <!-- 语音识别指示条 -->
          <Transition name="voice-bar">
            <div v-if="voiceActive" class="voice-bar">
              <div class="voice-waves">
                <span v-for="i in 5" :key="i" :style="{ animationDelay: `${(i-1)*0.1}s` }" />
              </div>
              <span>正在聆听… 松开停止</span>
            </div>
          </Transition>

          <div class="input-row">
            <!-- 图片上传 -->
            <label class="tool-btn" title="上传图片">
              <span>📎</span>
              <input type="file" accept="image/*" style="display:none" @change="onImgUpload" />
            </label>

            <textarea
              ref="inputEl"
              v-model="inputText"
              class="input"
              :placeholder="placeholder"
              rows="1"
              :disabled="chat.loading.value"
              maxlength="500"
              @keydown.enter.exact.prevent="handleSend"
              @keydown.enter.shift.exact="() => {}"
              @input="onInput"
            />

            <button
              class="send-btn"
              :class="{ 'send-btn--ready': canSend }"
              :disabled="!canSend || chat.loading.value"
              @click="handleSend"
            >
              <Transition name="send-icon" mode="out-in">
                <span v-if="chat.loading.value" key="l" class="spinner" />
                <span v-else key="s">发送</span>
              </Transition>
            </button>
          </div>

          <div class="input-hint">Enter 发送 · Shift+Enter 换行 · 📎 图片 · 🎤 长按语音</div>
        </div>

      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import AiChatFab from './AiChatFab.vue'
import { useAiChat }  from '@/composables/useAiChat'
import { useAiTts }   from '@/composables/useAiTts'
import type { ChatMessage } from '@/composables/useAiChat'

// ── Props ──────────────────────────────────────────────────────
const props = withDefaults(defineProps<{
  module?:            string
  customerId?:        number
  greeting?:          string
  defaultTypewriter?: boolean
}>(), {
  module:            'selection',
  customerId:        undefined,
  greeting:          '你好！我是 AI 助手 🤖\n直接告诉我你想做什么，例如：「我想做一件极简外套」',
  defaultTypewriter: true,
})

const emit = defineEmits<{
  (e: 'imageClick', url: string): void
  (e: 'done', sessionId: string): void
}>()

// ── Module maps ────────────────────────────────────────────────
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
const moduleName  = computed(() => MODULE_NAMES[props.module]  ?? '助手')
const placeholder = computed(() => PLACEHOLDERS[props.module] ?? '请输入…')

// ── Reactive state ─────────────────────────────────────────────
const open         = ref(false)
const unread       = ref(0)
const inputText    = ref('')
const pendingImg   = ref<string | undefined>()
const typewriterOn = ref(props.defaultTypewriter)
const ttsEnabled   = ref(false)
const voiceActive  = ref(false)
const currentTtsId = ref<string | undefined>()
const msgsEl       = ref<HTMLElement>()
const bottomAnchor = ref<HTMLElement>()
const inputEl      = ref<HTMLTextAreaElement>()
const fabRef       = ref<InstanceType<typeof AiChatFab>>()

// ── Composables ────────────────────────────────────────────────
const chat = useAiChat({
  module:     props.module,
  customerId: props.customerId,
  get typewriter() { return typewriterOn.value },
  scrollEl:   () => msgsEl.value,
  greeting:   props.greeting,
  persistKey: `ai_session_${props.module}`,
})
const tts = useAiTts({ lang: 'zh-CN', rate: 1.05 })

// ── Derived state ──────────────────────────────────────────────
const isStreaming = computed(() =>
  chat.messages.value.some(m => (m as any).streaming)
)
type DrawerState = 'idle'|'thinking'|'responding'
const state = computed<DrawerState>(() => {
  if (chat.loading.value && !isStreaming.value) return 'thinking'
  if (isStreaming.value)                         return 'responding'
  return 'idle'
})
const statusLabel = computed(() => ({
  thinking:   'AI 正在思考…',
  responding: 'AI 正在回复…',
  idle:       '在线',
}[state.value]))

const canSend = computed(() =>
  !chat.loading.value && (!!inputText.value.trim() || !!pendingImg.value)
)

// ── Open / Close ───────────────────────────────────────────────
function openDrawer() {
  open.value   = true
  unread.value = 0
  chat.showGreeting()
  nextTick(() => {
    inputEl.value?.focus()
    scrollBottom()
  })
}
function closeDrawer() {
  open.value = false
}
async function handleClear() {
  tts.stop()
  await chat.endSession()
  chat.showGreeting()
}

// ── Send ───────────────────────────────────────────────────────
async function handleSend() {
  if (!canSend.value) return
  const text = inputText.value.trim()
  chat.clearSuggestions()
  tts.stop()

  if (pendingImg.value) {
    const imgData = pendingImg.value
    pendingImg.value = undefined
    chat.messages.value.push({
      id: uid(), role: 'user',
      content: text || '[图片]',
      uploadedImage: imgData,
    } as ChatMessage & { uploadedImage: string })
    inputText.value = ''
    autoResize()
    await chat.send(text ? `[图片] ${text}` : '[用户上传了图片，请根据图片内容给出选款/设计建议]')
    return
  }

  inputText.value = ''
  autoResize()
  await chat.send(text)
}

// ── TTS ────────────────────────────────────────────────────────
function toggleTts() {
  ttsEnabled.value = !ttsEnabled.value
  if (!ttsEnabled.value) tts.stop()
}

function toggleSpeakMsg(id: string, content: string) {
  if (tts.speaking.value && currentTtsId.value === id) {
    tts.stop()
    currentTtsId.value = undefined
  } else {
    tts.stop()
    currentTtsId.value = id
    tts.speak(content)
  }
}

// Auto-read new AI messages when TTS is enabled
watch(() => chat.messages.value, (msgs) => {
  if (!ttsEnabled.value) return
  const last = [...msgs].reverse().find(m => m.role === 'ai')
  if (last && !(last as any).streaming && last.id !== currentTtsId.value) {
    currentTtsId.value = last.id
    tts.speak(last.content)
  }
}, { deep: true })

// ── Voice input ────────────────────────────────────────────────
function startVoice() {
  fabRef.value?.startVoice()
  voiceActive.value = true
}
function stopVoice() {
  fabRef.value?.stopVoice()
  voiceActive.value = false
}
function handleVoiceInput(text: string) {
  voiceActive.value = false
  inputText.value   = text
  nextTick(handleSend)
}

// ── Image upload ───────────────────────────────────────────────
function onImgUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = ev => { pendingImg.value = ev.target?.result as string }
  reader.readAsDataURL(file)
  ;(e.target as HTMLInputElement).value = ''
}

// ── Input helpers ──────────────────────────────────────────────
function onInput() {
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
function scrollBottom() {
  nextTick(() => {
    bottomAnchor.value?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  })
}

// ── Unread badge ───────────────────────────────────────────────
watch(() => chat.messages.value.length, (n, o) => {
  if (!open.value && n > o) unread.value++
})

// ── Done event ─────────────────────────────────────────────────
watch(() => chat.messages.value, msgs => {
  const last = [...msgs].reverse().find(m => m.role === 'ai')
  if (last && !(last as any).streaming && last.images?.length && chat.sessionId.value)
    emit('done', chat.sessionId.value)
}, { deep: true })

// ── Helpers ────────────────────────────────────────────────────
function renderText(t: string): string {
  if (!t) return ''
  return t
    .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
    .replace(/\n/g,'<br />')
    .replace(/\*\*(.+?)\*\*/g,'<strong>$1</strong>')
}

/** Format message ID (base-36 timestamp) into HH:MM */
function formatTime(id: string): string {
  try {
    const ts = parseInt(id.slice(0, 8), 36)
    if (isNaN(ts)) return ''
    const d = new Date(ts)
    return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
  } catch { return '' }
}

function uid(): string {
  return Date.now().toString(36) + Math.random().toString(36).slice(2,6)
}

onUnmounted(() => { chat.cancel(); tts.stop() })
</script>

<style scoped>
/* ─── BACKDROP ─────────────────────────────────────────────── */
.backdrop {
  position:fixed; inset:0; background:rgba(0,0,0,0.38);
  z-index:1201; display:none;
}
@media(max-width:600px){ .backdrop{display:block;} }

/* ─── DRAWER ──────────────────────────────────────────────── */
.drawer {
  position:fixed; bottom:0; right:0;
  width:420px; max-width:100vw;
  height:min(660px,94vh);
  display:flex; flex-direction:column;
  background:#fff; border-radius:22px 22px 0 0;
  box-shadow:-2px -2px 50px rgba(0,0,0,0.22);
  z-index:1202; overflow:hidden;
}
/* iOS safe area */
@supports(padding-bottom:env(safe-area-inset-bottom)){
  .drawer{ padding-bottom:env(safe-area-inset-bottom); }
}
@media(max-width:600px){ .drawer{width:100vw;height:93vh;} }

/* ─── HEADER ──────────────────────────────────────────────── */
.drawer-header {
  display:flex; align-items:center; gap:10px;
  padding:12px 14px; flex-shrink:0; color:#fff;
  transition:background 0.5s cubic-bezier(0.4,0,0.2,1);
}
.drawer-header--idle       { background:linear-gradient(135deg,#6366f1 0%,#8b5cf6 100%); }
.drawer-header--thinking   { background:linear-gradient(135deg,#f59e0b 0%,#ef4444 100%); }
.drawer-header--responding { background:linear-gradient(135deg,#10b981 0%,#06b6d4 100%); }

.header-avatar {
  width:38px; height:38px; border-radius:50%;
  background:rgba(255,255,255,0.2); flex-shrink:0;
  display:flex; align-items:center; justify-content:center; font-size:20px;
  transition:box-shadow 0.4s;
}
.header-avatar--thinking   { animation:hdr-ring  0.9s ease-in-out infinite; }
.header-avatar--responding { animation:hdr-glow  1.2s ease-in-out infinite alternate; }
@keyframes hdr-ring {
  0%,100%{ box-shadow:0 0 0 0   rgba(255,255,255,0.5); }
  50%    { box-shadow:0 0 0 12px rgba(255,255,255,0);   }
}
@keyframes hdr-glow {
  from{ box-shadow:0 0 4px  rgba(255,255,255,0.3); }
  to  { box-shadow:0 0 20px rgba(255,255,255,0.7); }
}

.header-info { flex:1; min-width:0; }
.header-title  { display:block; font-size:14px; font-weight:700; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.header-status { display:flex; align-items:center; gap:4px; font-size:11px; color:rgba(255,255,255,0.8); margin-top:1px; }
.status-dot {
  width:6px; height:6px; border-radius:50%; flex-shrink:0;
}
.status-dot--idle       { background:#4ade80; }
.status-dot--thinking   { background:#fbbf24; animation:blink 0.8s step-end infinite; }
.status-dot--responding { background:#34d399; animation:blink 0.5s step-end infinite; }
@keyframes blink{ 50%{opacity:0;} }

.header-tools { display:flex; align-items:center; gap:3px; flex-shrink:0; }
.hbtn {
  background:rgba(255,255,255,0.15); border:none; color:#fff;
  width:28px; height:28px; border-radius:8px; cursor:pointer;
  font-size:13px; display:flex; align-items:center; justify-content:center;
  transition:background 0.15s, box-shadow 0.2s; flex-shrink:0;
}
.hbtn:hover         { background:rgba(255,255,255,0.3); }
.hbtn--on           { background:rgba(255,255,255,0.35); box-shadow:0 0 0 2px rgba(255,255,255,0.5); }
.hbtn--voice        { background:rgba(236,72,153,0.7); animation:hbtn-voice 0.6s infinite alternate; }
.hbtn--close        { font-size:15px; }
@keyframes hbtn-voice{ to{box-shadow:0 0 0 5px rgba(236,72,153,0.2);} }

/* ─── MESSAGES ────────────────────────────────────────────── */
.messages {
  flex:1; overflow-y:auto; padding:14px 12px 8px;
  display:flex; flex-direction:column; gap:12px;
  background:linear-gradient(180deg, #f4f5fb 0%, #f8f9fc 100%);
  scroll-behavior:smooth;
}
.messages::-webkit-scrollbar       { width:3px; }
.messages::-webkit-scrollbar-track { background:transparent; }
.messages::-webkit-scrollbar-thumb { background:#d1d5db; border-radius:3px; }

/* ─── ROWS ────────────────────────────────────────────────── */
.row {
  display:flex; gap:8px; align-items:flex-start;
  animation:row-in 0.26s cubic-bezier(0.34,1.2,0.64,1);
}
@keyframes row-in{ from{opacity:0;transform:translateY(10px) scale(0.97);} }
.row--user { flex-direction:row-reverse; }

.row-avatar {
  width:32px; height:32px; border-radius:50%; background:#e0e7ff;
  display:flex; align-items:center; justify-content:center;
  font-size:16px; flex-shrink:0; margin-top:2px;
}
.row-avatar--user { background:#dcfce7; }

.row-body { display:flex; flex-direction:column; gap:6px; max-width:calc(100% - 44px); }
.row-body--user { align-items:flex-end; }

/* ─── BUBBLES ─────────────────────────────────────────────── */
.bubble {
  padding:9px 13px; border-radius:4px 14px 14px 14px;
  font-size:13.5px; line-height:1.7; word-break:break-word; white-space:pre-wrap;
}
.bubble--ai {
  background:#fff; color:#1f2937;
  box-shadow:0 1px 4px rgba(0,0,0,0.07), 0 0 0 1px rgba(0,0,0,0.03);
}
.bubble--user {
  background:linear-gradient(135deg,#6366f1,#8b5cf6); color:#fff;
  border-radius:14px 4px 14px 14px;
  box-shadow:0 3px 12px rgba(99,102,241,0.32);
}

/* Typewriter cursor */
.cursor {
  display:inline-block; width:2px; height:1.05em; background:#6366f1;
  margin-left:2px; vertical-align:text-bottom;
  animation:blink 0.75s step-end infinite;
}

/* Typing dots */
.typing {
  display:flex; align-items:center; gap:4px;
  padding:12px 16px; min-height:40px;
}
.typing span {
  width:7px; height:7px; background:#a5b4fc; border-radius:50%;
  animation:bounce 1.2s infinite;
}
.typing span:nth-child(2){ animation-delay:0.2s; }
.typing span:nth-child(3){ animation-delay:0.4s; }
@keyframes bounce{ 0%,80%,100%{transform:translateY(0);} 40%{transform:translateY(-7px);} }

/* ─── MESSAGE TOOLS ───────────────────────────────────────── */
.msg-tools {
  display:flex; align-items:center; gap:8px; padding-left:2px;
}
.tts-btn {
  background:none; border:none; cursor:pointer;
  font-size:14px; opacity:0.45; transition:opacity 0.15s, transform 0.15s; padding:0;
  line-height:1;
}
.tts-btn:hover        { opacity:0.9; transform:scale(1.15); }
.tts-btn--playing     { opacity:1; animation:tts-spin 1.5s linear infinite; }
@keyframes tts-spin   { to{transform:rotate(360deg);} }
.msg-time { font-size:11px; color:#c4c9d4; }

/* ─── IMAGE GRID ──────────────────────────────────────────── */
.img-grid { display:flex; flex-wrap:wrap; gap:7px; max-width:100%; }
.img-card {
  position:relative; width:86px; height:106px;
  border-radius:10px; overflow:hidden; cursor:pointer;
  box-shadow:0 2px 8px rgba(0,0,0,0.1);
  transition:transform 0.17s cubic-bezier(.34,1.56,.64,1), box-shadow 0.17s;
}
.img-card:hover { transform:scale(1.07) translateY(-2px); box-shadow:0 5px 20px rgba(0,0,0,0.18); }
.img-card img   { width:100%; height:100%; object-fit:cover; display:block; }
.img-card-label {
  position:absolute; bottom:0; left:0; right:0;
  background:rgba(0,0,0,0.45); color:#fff;
  font-size:11px; text-align:center; padding:3px 0;
}

/* ─── QUICK REPLIES ───────────────────────────────────────── */
.quick-row { display:flex; flex-wrap:wrap; gap:5px; }
.qr-btn {
  background:#ede9fe; color:#5b21b6; border:1px solid #c4b5fd;
  border-radius:20px; padding:5px 13px; font-size:12.5px; cursor:pointer;
  transition:background 0.15s, transform 0.12s; white-space:nowrap;
}
.qr-btn:hover:not(:disabled) { background:#ddd6fe; transform:translateY(-1px); }
.qr-btn:disabled { opacity:0.4; cursor:not-allowed; }

/* ─── UPLOADED IMAGE ──────────────────────────────────────── */
.uploaded-img {
  width:120px; height:90px; object-fit:cover; border-radius:10px;
  box-shadow:0 2px 8px rgba(0,0,0,0.12);
  animation:row-in 0.2s ease-out;
}

/* ─── INPUT AREA ──────────────────────────────────────────── */
.input-area {
  padding:8px 10px 10px; background:#fff;
  border-top:1px solid rgba(0,0,0,0.06);
  flex-shrink:0; position:relative;
}
.input-row { display:flex; gap:6px; align-items:flex-end; }

.tool-btn {
  background:#f5f3ff; border:none; color:#6366f1; border-radius:10px;
  width:36px; height:36px; font-size:17px; cursor:pointer; flex-shrink:0;
  display:flex; align-items:center; justify-content:center; align-self:flex-end;
  transition:background 0.15s, transform 0.12s;
}
.tool-btn:hover { background:#ede9fe; transform:scale(1.08); }

.input {
  flex:1; border:1.5px solid #e0e7ff; border-radius:14px;
  padding:9px 12px; font-size:13.5px; line-height:1.5; resize:none; outline:none;
  color:#1f2937; font-family:inherit; background:#fafafa;
  transition:border-color 0.2s, box-shadow 0.2s; max-height:120px; overflow-y:auto;
}
.input:focus {
  border-color:#6366f1; background:#fff;
  box-shadow:0 0 0 3px rgba(99,102,241,0.12);
}
.input:disabled { background:#f5f5f5; cursor:not-allowed; }
.input-hint { font-size:11px; color:#d1d5db; margin-top:3px; padding-left:2px; }

.send-btn {
  background:#e0e7ff; color:#6366f1; border:none; border-radius:14px;
  padding:9px 15px; font-size:13.5px; font-weight:600; cursor:pointer;
  flex-shrink:0; align-self:flex-end; min-width:56px; min-height:38px;
  display:flex; align-items:center; justify-content:center;
  transition:background 0.2s, transform 0.12s, box-shadow 0.2s;
}
.send-btn--ready {
  background:linear-gradient(135deg,#6366f1,#8b5cf6); color:#fff;
  box-shadow:0 3px 14px rgba(99,102,241,0.38);
}
.send-btn--ready:hover { transform:translateY(-1px); box-shadow:0 6px 20px rgba(99,102,241,0.48); }
.send-btn:disabled { opacity:0.42; cursor:not-allowed; transform:none !important; }

.spinner {
  width:15px; height:15px;
  border:2px solid rgba(255,255,255,0.35); border-top-color:#fff;
  border-radius:50%; animation:spin 0.7s linear infinite;
}
@keyframes spin{ to{transform:rotate(360deg);} }

/* ─── PENDING IMAGE ───────────────────────────────────────── */
.pending-img {
  position:relative; display:inline-flex; margin-bottom:6px;
}
.pending-img img {
  width:72px; height:54px; object-fit:cover; border-radius:8px; border:2px solid #6366f1;
}
.pending-img button {
  position:absolute; top:-6px; right:-6px; background:#ef4444; color:#fff;
  border:none; width:18px; height:18px; border-radius:50%; font-size:10px;
  cursor:pointer; display:flex; align-items:center; justify-content:center;
}

/* ─── VOICE BAR ───────────────────────────────────────────── */
.voice-bar {
  display:flex; align-items:center; gap:10px; margin-bottom:6px;
  background:#fdf4ff; border:1px solid #e9d5ff; border-radius:10px;
  padding:7px 12px; font-size:12.5px; color:#7c3aed;
}
.voice-waves {
  display:flex; align-items:center; gap:2px;
}
.voice-waves span {
  width:3px; border-radius:3px; background:#8b5cf6;
  animation:wave 0.5s ease-in-out infinite alternate;
}
.voice-waves span:nth-child(1){ height:7px; }
.voice-waves span:nth-child(2){ height:14px; }
.voice-waves span:nth-child(3){ height:20px; }
.voice-waves span:nth-child(4){ height:14px; }
.voice-waves span:nth-child(5){ height:7px; }
@keyframes wave{ to{transform:scaleY(1.9);} }

/* ─── AUTOCOMPLETE ────────────────────────────────────────── */
.suggest {
  position:absolute; bottom:100%; left:10px; right:10px;
  background:#fff; border:1px solid #e0e7ff; border-radius:14px;
  box-shadow:0 -6px 24px rgba(0,0,0,0.1);
  list-style:none; margin:0 0 4px; padding:5px 0;
  z-index:10; max-height:180px; overflow-y:auto;
}
.suggest-item { padding:8px 14px; font-size:13px; color:#374151; cursor:pointer; }
.suggest-item:hover { background:#f5f3ff; color:#5b21b6; }

/* ─── ICON ANIMATIONS ─────────────────────────────────────── */
.av-spin  { display:inline-block; animation:icon-spin 1.2s linear infinite; }
.av-wave  { display:inline-block; animation:icon-wave 0.8s ease-in-out infinite alternate; }
.av-speak { display:inline-block; animation:icon-speak 0.9s ease-in-out infinite; }
@keyframes icon-spin  { to{transform:rotate(360deg);} }
@keyframes icon-wave  { to{transform:scale(1.25) rotate(10deg);} }
@keyframes icon-speak { 0%,100%{transform:scale(1);} 50%{transform:scale(1.2);} }

/* ─── TRANSITIONS ─────────────────────────────────────────── */
.overlay-enter-active,.overlay-leave-active{ transition:opacity 0.25s; }
.overlay-enter-from,.overlay-leave-to { opacity:0; }

.drawer-enter-active,.drawer-leave-active{
  transition:transform 0.36s cubic-bezier(0.4,0,0.2,1), opacity 0.3s;
}
.drawer-enter-from,.drawer-leave-to{ transform:translateY(100%); opacity:0; }

.suggest-enter-active,.suggest-leave-active{ transition:opacity 0.15s,transform 0.15s; }
.suggest-enter-from,.suggest-leave-to{ opacity:0; transform:translateY(6px); }

.send-icon-enter-active,.send-icon-leave-active{ transition:opacity 0.14s,transform 0.12s; }
.send-icon-enter-from,.send-icon-leave-to{ opacity:0; transform:scale(0.6); }

.voice-bar-enter-active,.voice-bar-leave-active{ transition:opacity 0.2s,transform 0.2s; }
.voice-bar-enter-from,.voice-bar-leave-to{ opacity:0; transform:translateY(-4px); }

.av-enter-active,.av-leave-active{ transition:opacity 0.18s,transform 0.18s; }
.av-enter-from{ opacity:0; transform:scale(0.4) rotate(-45deg); }
.av-leave-to  { opacity:0; transform:scale(0.4) rotate(45deg); }
</style>
