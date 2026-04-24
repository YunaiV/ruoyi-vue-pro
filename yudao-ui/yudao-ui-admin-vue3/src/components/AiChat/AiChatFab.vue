<template>
  <!-- ===================================================================
    AiChatFab — 智能浮动 AI 助手图标
    状态: idle | attention | peeking | thinking | responding | voice
    特性:
      • 闲置检测 (30s) → 自动弹出"需要帮助吗？"气泡
      • 悬浮预览气泡 (hover peek)
      • 情境感知：不同模块显示不同提示语
      • 粒子爆炸（点击开启时）
      • 语音合成 TTS 图标状态
      • 响应 loading / streaming 状态切换梯度
  =================================================================== -->
  <div
    class="fab-root"
    :data-state="fabState"
    :style="cssVars"
  >
    <!-- 粒子层 -->
    <div class="fab-particles" aria-hidden="true">
      <span
        v-for="p in particles"
        :key="p.id"
        class="fab-particle"
        :style="p.style"
      />
    </div>

    <!-- 轨道环 1 / 2 / 3 -->
    <div class="fab-orbit fab-orbit--1" aria-hidden="true" />
    <div class="fab-orbit fab-orbit--2" aria-hidden="true" />
    <div class="fab-orbit fab-orbit--3" aria-hidden="true" />

    <!-- 外层光晕 -->
    <div class="fab-glow" aria-hidden="true" />

    <!-- 主按钮 -->
    <button
      class="fab-btn"
      :aria-label="open ? '关闭 AI 助手' : '打开 AI 助手'"
      @click="handleClick"
      @mouseenter="onMouseEnter"
      @mouseleave="onMouseLeave"
    >
      <!-- 呼吸圈（idle 状态） -->
      <span v-if="fabState === 'idle' && !open" class="fab-breath" aria-hidden="true" />

      <!-- 语音波（voice 状态） -->
      <span v-if="fabState === 'voice'" class="fab-voice-waves" aria-hidden="true">
        <span /><span /><span />
      </span>

      <!-- 状态图标 -->
      <Transition name="fab-icon" mode="out-in">
        <span v-if="open"                      key="x"  class="fab-icon">✕</span>
        <span v-else-if="fabState==='voice'"   key="v"  class="fab-icon">🎙</span>
        <span v-else-if="fabState==='thinking'" key="t" class="fab-icon fab-icon--spin">⚙️</span>
        <span v-else-if="fabState==='responding'" key="r" class="fab-icon">✨</span>
        <span v-else-if="fabState==='attention'" key="a" class="fab-icon fab-icon--bounce">👋</span>
        <span v-else                            key="i" class="fab-icon">🤖</span>
      </Transition>
    </button>

    <!-- 未读徽标 -->
    <Transition name="badge">
      <span v-if="unread > 0 && !open" class="fab-badge">{{ unread > 9 ? '9+' : unread }}</span>
    </Transition>

    <!-- ── 预览/问候气泡 ─────────────────────────────────────── -->
    <Transition name="peek">
      <div
        v-if="peekVisible && !open"
        class="fab-peek"
        role="status"
        :class="{ 'fab-peek--attention': fabState === 'attention' }"
      >
        <span class="fab-peek-dot" />
        <span class="fab-peek-text">{{ peekText }}</span>
        <button class="fab-peek-close" @click.stop="dismissPeek" aria-label="关闭提示">✕</button>
      </div>
    </Transition>

    <!-- ── 工具提示（简短，hover 时） ──────────────────────────── -->
    <Transition name="tooltip">
      <div v-if="tooltipVisible && !peekVisible && !open" class="fab-tooltip" aria-hidden="true">
        {{ moduleTooltip }}
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

// ── Props / Emits ─────────────────────────────────────────────
const props = defineProps<{
  open:        boolean
  loading:     boolean
  streaming?:  boolean
  ttsActive?:  boolean
  unread:      number
  module:      string
}>()

const emit = defineEmits<{
  (e: 'toggle'): void
  (e: 'voice-input', text: string): void
}>()

// ── Module data ────────────────────────────────────────────────

/** Short tooltip shown on hover */
const MODULE_TOOLTIP: Record<string, string> = {
  selection: '💬 AI 选款助手',
  design:    '🎨 AI 设计助手',
  product:   '📦 AI 商品助手',
  inventory: '🏭 AI 库存助手',
  finance:   '💰 AI 财务助手',
  trend:     '📈 AI 趋势助手',
  order:     '📋 AI 订单助手',
}
const moduleTooltip = computed(() => MODULE_TOOLTIP[props.module] ?? '🤖 AI 助手')

/** Context-aware hints shown in the peek bubble */
const MODULE_HINTS: Record<string, string[]> = {
  selection: [
    '👋 需要帮忙选款吗？告诉我你的目标市场！',
    '💡 最近「极简外套」搜索量暴涨，要看看吗？',
    '🛍️ 说出你的风格，我帮你找到最火的款式~',
  ],
  design: [
    '🎨 需要设计灵感吗？我来帮你出图！',
    '✏️ 告诉我风格，AI 帮你生成完整设计方案~',
    '🌟 「极简轻奢」风现在很流行，要试试吗？',
  ],
  product: [
    '📦 想知道哪些商品卖得最好吗？',
    '💵 不确定定什么价？我来帮你分析均价~',
    '🔍 说出品类，我帮你找到热销商品！',
  ],
  inventory: [
    '📊 库存有点紧了？我帮你预测补货量！',
    '⚠️ 想了解哪个品类的库存状况？',
    '🔄 告诉我链码，立刻查看实时库存~',
  ],
  finance: [
    '💰 想了解这款商品的 ROI 吗？',
    '📈 我帮你分析利润率和定价区间~',
    '💡 查看同品类均价，做出最优定价决策！',
  ],
  trend: [
    '🔥 最近什么款最火？问我就知道！',
    '📈 趋势分析出炉了，要看看吗？',
    '🌏 不同市场趋势不一样，告诉我你的目标！',
  ],
  order: [
    '📋 想查询订单状态？直接告诉我！',
    '🚚 发货进度、支付状态一句话搞定~',
    '📊 需要帮你对账分析吗？',
  ],
}

/** Generic hints for any module */
const GENERIC_HINTS = [
  '👋 嗨！需要我帮忙吗？',
  '🤖 有任何问题，直接问我吧~',
  '💬 我在这里，随时为你服务！',
]

// ── Fab state machine ───────────────────────────────────────────
type FabState = 'idle' | 'attention' | 'thinking' | 'responding' | 'voice'

const _voiceInternal = ref(false)
const fabState = computed<FabState>(() => {
  if (_voiceInternal.value)                              return 'voice'
  if (props.loading && !props.streaming)                 return 'thinking'
  if (props.streaming)                                   return 'responding'
  if (idleAttention.value && !props.open)                return 'attention'
  return 'idle'
})

// ── CSS variables (state-driven gradient) ──────────────────────
const STATE_GRAD: Record<string, string> = {
  idle:       'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
  attention:  'linear-gradient(135deg, #f59e0b 0%, #fb923c 100%)',
  thinking:   'linear-gradient(135deg, #ef4444 0%, #f97316 100%)',
  responding: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)',
  voice:      'linear-gradient(135deg, #ec4899 0%, #a855f7 100%)',
}
const cssVars = computed(() => ({
  '--fab-grad': STATE_GRAD[fabState.value] ?? STATE_GRAD.idle,
}))

// ── Idle detection (auto-peek after 30 s of no click) ──────────
const IDLE_TIMEOUT_MS = 30_000
const idleAttention = ref(false)
let idleTimer: ReturnType<typeof setTimeout> | null = null
let peekTimer:  ReturnType<typeof setTimeout> | null = null

function resetIdleTimer() {
  idleAttention.value = false
  dismissPeek()
  if (idleTimer) clearTimeout(idleTimer)
  idleTimer = setTimeout(() => {
    if (!props.open) {
      idleAttention.value = true
      showAutoPeek()
    }
  }, IDLE_TIMEOUT_MS)
}

function showAutoPeek() {
  const hints = MODULE_HINTS[props.module] ?? GENERIC_HINTS
  peekText.value = hints[Math.floor(Math.random() * hints.length)]
  peekVisible.value = true
  // Auto-dismiss after 8 seconds
  if (peekTimer) clearTimeout(peekTimer)
  peekTimer = setTimeout(dismissPeek, 8_000)
}

// ── Peek bubble ─────────────────────────────────────────────────
const peekVisible = ref(false)
const peekText    = ref('')

function dismissPeek() {
  peekVisible.value = false
  idleAttention.value = false
  if (peekTimer) { clearTimeout(peekTimer); peekTimer = null }
}

// ── Tooltip ─────────────────────────────────────────────────────
const tooltipVisible = ref(false)
let tooltipTimer: ReturnType<typeof setTimeout> | null = null

function onMouseEnter() {
  if (tooltipTimer) clearTimeout(tooltipTimer)
  tooltipTimer = setTimeout(() => { tooltipVisible.value = true }, 400)
}
function onMouseLeave() {
  if (tooltipTimer) { clearTimeout(tooltipTimer); tooltipTimer = null }
  tooltipVisible.value = false
}

// ── Click: particle burst + toggle ─────────────────────────────
function handleClick() {
  resetIdleTimer()
  dismissPeek()
  if (!props.open) spawnParticles()
  emit('toggle')
}

// ── Particles ───────────────────────────────────────────────────
interface Particle { id: number; style: Record<string, string> }
const particles = ref<Particle[]>([])
let   pid = 0

function spawnParticles() {
  const COLORS = ['#a5b4fc', '#c4b5fd', '#fbcfe8', '#6ee7b7', '#fcd34d', '#f9a8d4']
  const COUNT  = 14
  for (let i = 0; i < COUNT; i++) {
    const angle = (i / COUNT) * 360
    const dist  = 44 + Math.random() * 28
    const rad   = (angle * Math.PI) / 180
    const id    = ++pid
    const size  = 4 + Math.random() * 5

    particles.value.push({
      id,
      style: {
        '--tx':   `${Math.cos(rad) * dist}px`,
        '--ty':   `${Math.sin(rad) * dist}px`,
        background: COLORS[i % COLORS.length],
        width:  `${size}px`,
        height: `${size}px`,
        left:   '50%', top: '50%',
        animationDelay: `${i * 18}ms`,
      },
    })
    setTimeout(() => { particles.value = particles.value.filter(p => p.id !== id) }, 700)
  }
}

// ── Voice input (Web Speech API) ───────────────────────────────
let recognition: any = null

function startVoice() {
  const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  if (!SR) { alert('当前浏览器不支持语音识别，请使用 Chrome 或 Edge'); return }

  recognition         = new SR()
  recognition.lang    = 'zh-CN'
  recognition.interimResults  = false
  recognition.maxAlternatives = 1
  _voiceInternal.value = true

  recognition.onresult = (e: any) => {
    const text = e.results[0][0].transcript
    _voiceInternal.value = false
    emit('voice-input', text)
  }
  recognition.onerror = () => { _voiceInternal.value = false }
  recognition.onend   = () => { _voiceInternal.value = false }
  recognition.start()
}

function stopVoice() {
  recognition?.stop()
  _voiceInternal.value = false
}

// ── Lifecycle ──────────────────────────────────────────────────
onMounted(() => {
  resetIdleTimer()
  // Listen for any user activity to reset idle timer
  const reset = () => resetIdleTimer()
  document.addEventListener('click',     reset, { passive: true })
  document.addEventListener('keydown',   reset, { passive: true })
  document.addEventListener('mousemove', reset, { passive: true })

  // Load TTS voices (Chrome needs this trigger)
  window.speechSynthesis?.getVoices()

  onUnmounted(() => {
    document.removeEventListener('click',     reset)
    document.removeEventListener('keydown',   reset)
    document.removeEventListener('mousemove', reset)
    if (idleTimer)   clearTimeout(idleTimer)
    if (peekTimer)   clearTimeout(peekTimer)
    if (tooltipTimer) clearTimeout(tooltipTimer)
    recognition?.abort()
  })
})

// Close peek when drawer opens
watch(() => props.open, (v) => { if (v) dismissPeek() })

defineExpose({ startVoice, stopVoice })
</script>

<style scoped>
/* ── Root ────────────────────────────────────────────────────── */
.fab-root {
  position: fixed;
  bottom: 28px; right: 28px;
  z-index: 1200;
  width: 60px; height: 60px;
  display: flex; align-items: center; justify-content: center;
  --fab-grad: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

/* ── Orbital rings ──────────────────────────────────────────── */
.fab-orbit {
  position: absolute; border-radius: 50%;
  border: 1.5px solid rgba(99, 102, 241, 0.2);
  pointer-events: none; transition: border-color 0.5s;
}
.fab-orbit--1 { width:  82px; height:  82px; animation: orbit 8s  linear infinite; }
.fab-orbit--2 { width: 104px; height: 104px; animation: orbit 14s linear infinite reverse; }
.fab-orbit--3 { width: 126px; height: 126px; animation: orbit 22s linear infinite; opacity: 0.5; }
@keyframes orbit { to { transform: rotate(360deg); } }

/* State ring colours */
[data-state="attention"] .fab-orbit  { border-color: rgba(245,158,11,0.4);  animation-duration: 3s, 6s, 9s; }
[data-state="thinking"]  .fab-orbit  { border-color: rgba(239,68,68,0.35);  animation-duration: 2s, 4s, 6s; }
[data-state="responding"] .fab-orbit { border-color: rgba(16,185,129,0.35); }
[data-state="voice"]     .fab-orbit  { border-color: rgba(236,72,153,0.4);  animation-duration: 1s, 2s, 3s; }

/* ── Glow ──────────────────────────────────────────────────── */
.fab-glow {
  position: absolute; inset: -10px; border-radius: 50%;
  background: var(--fab-grad);
  opacity: 0.15; filter: blur(14px);
  animation: glow 3s ease-in-out infinite;
  pointer-events: none; transition: opacity 0.5s;
}
@keyframes glow {
  0%,100% { opacity: 0.15; transform: scale(1); }
  50%     { opacity: 0.28; transform: scale(1.15); }
}
[data-state="attention"]  .fab-glow { animation-duration: 0.9s; opacity: 0.3; }
[data-state="thinking"]   .fab-glow { animation-duration: 0.7s; }
[data-state="responding"] .fab-glow { opacity: 0.25; }

/* ── Main button ───────────────────────────────────────────── */
.fab-btn {
  position: relative; z-index: 2;
  width: 56px; height: 56px; border-radius: 50%;
  border: none; cursor: pointer; outline: none;
  background: var(--fab-grad);
  box-shadow: 0 4px 20px rgba(99,102,241,0.5);
  display: flex; align-items: center; justify-content: center;
  transition:
    transform  0.3s cubic-bezier(.34,1.56,.64,1),
    box-shadow 0.25s,
    background 0.5s;
  overflow: visible;
}
.fab-btn::after {
  content: '';
  position: absolute; inset: 0; border-radius: 50%;
  background: linear-gradient(135deg, rgba(255,255,255,0.22) 0%, transparent 60%);
  pointer-events: none;
}
.fab-btn:hover  { transform: scale(1.12) translateY(-3px); box-shadow: 0 10px 30px rgba(99,102,241,0.6); }
.fab-btn:active { transform: scale(0.94); }

/* ── Breathing ring (idle) ──────────────────────────────────── */
.fab-breath {
  position: absolute; inset: -4px; border-radius: 50%;
  border: 2px solid rgba(99,102,241,0.4);
  animation: breath 2.8s ease-in-out infinite;
  pointer-events: none;
}
@keyframes breath {
  0%,100% { transform: scale(1);   opacity: 0.5; }
  50%     { transform: scale(1.35); opacity: 0; }
}

/* Attention shake */
[data-state="attention"] .fab-btn {
  animation: attention-shake 0.5s ease-in-out 3;
}
@keyframes attention-shake {
  0%,100% { transform: rotate(0); }
  20%     { transform: rotate(-8deg) scale(1.05); }
  40%     { transform: rotate(8deg)  scale(1.08); }
  60%     { transform: rotate(-5deg); }
  80%     { transform: rotate(5deg); }
}

/* Voice pulse */
[data-state="voice"] .fab-btn {
  animation: voice-pulse 0.6s ease-in-out infinite alternate;
}
@keyframes voice-pulse {
  to { transform: scale(1.12); box-shadow: 0 0 0 10px rgba(236,72,153,0.15); }
}

/* ── Voice waves ───────────────────────────────────────────── */
.fab-voice-waves {
  position: absolute; inset: 0; display: flex; align-items: center;
  justify-content: center; gap: 3px; z-index: 1;
}
.fab-voice-waves span {
  width: 3px; background: rgba(255,255,255,0.85); border-radius: 3px;
  animation: wave-bar 0.55s ease-in-out infinite alternate;
}
.fab-voice-waves span:nth-child(1) { height: 8px;  animation-delay: 0s; }
.fab-voice-waves span:nth-child(2) { height: 18px; animation-delay: 0.15s; }
.fab-voice-waves span:nth-child(3) { height: 8px;  animation-delay: 0.3s; }
@keyframes wave-bar { to { transform: scaleY(2.2); } }

/* ── Icons ─────────────────────────────────────────────────── */
.fab-icon {
  font-size: 25px; line-height: 1; user-select: none;
  position: relative; z-index: 2;
}
.fab-icon--spin   { animation: icon-spin 1.2s linear infinite; }
.fab-icon--bounce { animation: icon-bounce 0.6s ease-in-out infinite alternate; }
@keyframes icon-spin   { to { transform: rotate(360deg); } }
@keyframes icon-bounce { to { transform: translateY(-5px) scale(1.15); } }

/* ── Particles ─────────────────────────────────────────────── */
.fab-particles { position: absolute; inset: 0; pointer-events: none; z-index: 3; }
.fab-particle {
  position: absolute; border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: particle 0.65s ease-out forwards;
}
@keyframes particle {
  0%   { transform: translate(-50%,-50%) scale(1);   opacity: 1; }
  100% { transform: translate(calc(-50% + var(--tx)), calc(-50% + var(--ty))) scale(0.1); opacity: 0; }
}

/* ── Unread badge ──────────────────────────────────────────── */
.fab-badge {
  position: absolute; top: -3px; right: -3px; z-index: 4;
  min-width: 20px; height: 20px; border-radius: 10px;
  background: #ef4444; color: #fff; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; padding: 0 4px;
  border: 2px solid #fff;
}

/* ── Peek bubble ───────────────────────────────────────────── */
.fab-peek {
  position: absolute;
  bottom: calc(100% + 14px);
  right: 0;
  width: 240px;
  background: #fff;
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.14), 0 2px 8px rgba(0,0,0,0.08);
  padding: 12px 14px 12px 12px;
  display: flex; align-items: flex-start; gap: 8px;
  border: 1px solid rgba(99,102,241,0.12);
}
.fab-peek--attention {
  border-color: rgba(245,158,11,0.3);
  box-shadow: 0 8px 32px rgba(245,158,11,0.18), 0 2px 8px rgba(0,0,0,0.08);
  animation: peek-bounce 0.4s cubic-bezier(.34,1.56,.64,1);
}
@keyframes peek-bounce {
  from { transform: scale(0.85) translateY(8px); opacity: 0; }
  to   { transform: scale(1)    translateY(0);   opacity: 1; }
}
/* Arrow pointing down-right */
.fab-peek::after {
  content: '';
  position: absolute; bottom: -8px; right: 20px;
  width: 0; height: 0;
  border-left: 8px solid transparent;
  border-right: 0 solid transparent;
  border-top: 8px solid #fff;
  filter: drop-shadow(0 2px 2px rgba(0,0,0,0.06));
}

.fab-peek-dot {
  flex-shrink: 0; width: 8px; height: 8px; border-radius: 50%;
  background: #6366f1; margin-top: 4px;
  animation: dot-pulse 1.5s ease-in-out infinite;
}
@keyframes dot-pulse {
  0%,100% { box-shadow: 0 0 0 0   rgba(99,102,241,0.5); }
  50%     { box-shadow: 0 0 0 6px rgba(99,102,241,0); }
}
.fab-peek--attention .fab-peek-dot { background: #f59e0b; }

.fab-peek-text {
  flex: 1; font-size: 13px; line-height: 1.55; color: #374151;
}
.fab-peek-close {
  flex-shrink: 0; background: none; border: none; cursor: pointer;
  color: #9ca3af; font-size: 12px; padding: 0; line-height: 1;
  transition: color 0.15s;
}
.fab-peek-close:hover { color: #6b7280; }

/* ── Tooltip ───────────────────────────────────────────────── */
.fab-tooltip {
  position: absolute;
  right: calc(100% + 12px); top: 50%;
  transform: translateY(-50%);
  background: rgba(17,24,39,0.9); color: #fff;
  font-size: 12px; padding: 5px 10px; border-radius: 8px;
  white-space: nowrap; pointer-events: none;
}
.fab-tooltip::after {
  content: '';
  position: absolute; left: 100%; top: 50%; transform: translateY(-50%);
  border: 5px solid transparent; border-left-color: rgba(17,24,39,0.9);
}

/* ── Transitions ───────────────────────────────────────────── */
.fab-icon-enter-active, .fab-icon-leave-active { transition: opacity 0.18s, transform 0.18s; }
.fab-icon-enter-from { opacity: 0; transform: scale(0.4) rotate(-45deg); }
.fab-icon-leave-to   { opacity: 0; transform: scale(0.4) rotate(45deg); }

.badge-enter-active, .badge-leave-active { transition: transform 0.2s, opacity 0.2s; }
.badge-enter-from, .badge-leave-to { transform: scale(0); opacity: 0; }

.peek-enter-active, .peek-leave-active { transition: opacity 0.24s, transform 0.24s; }
.peek-enter-from { opacity: 0; transform: translateY(8px) scale(0.92); }
.peek-leave-to   { opacity: 0; transform: translateY(6px) scale(0.94); }

.tooltip-enter-active, .tooltip-leave-active { transition: opacity 0.15s, transform 0.15s; }
.tooltip-enter-from, .tooltip-leave-to { opacity: 0; transform: translateY(-50%) translateX(4px); }
</style>
