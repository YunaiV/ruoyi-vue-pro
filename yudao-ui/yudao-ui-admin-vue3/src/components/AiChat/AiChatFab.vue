<template>
  <!-- ================================================================
       AI Chat FAB — 智能浮动聊天按钮
       状态: idle / thinking / responding / voice
  ================================================================ -->
  <Teleport to="body">
    <div
      class="fab-root"
      :class="`fab-root--${fabState}`"
      :style="fabStyle"
    >
      <!-- 粒子层（点击时喷射） -->
      <div class="fab-particles" ref="particlesEl">
        <span
          v-for="p in particles"
          :key="p.id"
          class="fab-particle"
          :style="p.style"
        />
      </div>

      <!-- 轨道环 -->
      <div class="fab-orbit fab-orbit--1" />
      <div class="fab-orbit fab-orbit--2" />
      <div class="fab-orbit fab-orbit--3" />

      <!-- 外层光晕 -->
      <div class="fab-glow" />

      <!-- 主按钮 -->
      <button
        class="fab-btn"
        :class="{ 'fab-btn--active': open, 'fab-btn--voice': fabState === 'voice' }"
        :aria-label="open ? '关闭 AI 助手' : '打开 AI 助手'"
        @click="handleFabClick"
        @mouseenter="hovering = true"
        @mouseleave="hovering = false"
      >
        <!-- 状态图标 -->
        <Transition name="fab-icon" mode="out-in">
          <span v-if="fabState === 'voice'"  key="voice"     class="fab-icon">🎤</span>
          <span v-else-if="fabState === 'thinking'" key="think" class="fab-icon fab-icon--spin">⚙️</span>
          <span v-else-if="open"             key="close"    class="fab-icon">✕</span>
          <span v-else                        key="default"  class="fab-icon">🤖</span>
        </Transition>

        <!-- 语音波纹（voice 状态） -->
        <div v-if="fabState === 'voice'" class="fab-voice-waves">
          <span /><span /><span />
        </div>
      </button>

      <!-- 未读徽标 -->
      <Transition name="badge">
        <span v-if="unread > 0 && !open" class="fab-badge">
          {{ unread > 9 ? '9+' : unread }}
        </span>
      </Transition>

      <!-- 悬浮工具提示 -->
      <Transition name="tooltip">
        <div v-if="hovering && !open" class="fab-tooltip">
          {{ tooltipText }}
        </div>
      </Transition>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

// ── Props / Emits ─────────────────────────────────────────
const props = defineProps<{
  open:       boolean
  loading:    boolean
  streaming?: boolean
  unread:     number
  module:     string
}>()

const emit = defineEmits<{
  (e: 'toggle'): void
  (e: 'voice-input', text: string): void
}>()

// ── State ─────────────────────────────────────────────────
const hovering  = ref(false)
const particles = ref<Array<{ id: number; style: Record<string, string> }>>([])
let   particleId = 0

// ── FAB state machine ──────────────────────────────────────
// idle → thinking (loading) → responding (streaming) → idle
const fabState = computed<'idle' | 'thinking' | 'responding' | 'voice'>(() => {
  if (voiceActive.value) return 'voice'
  if (props.loading && !props.streaming) return 'thinking'
  if (props.streaming) return 'responding'
  return 'idle'
})

// ── Dynamic gradient based on state ───────────────────────
const STATE_GRADIENTS: Record<string, string> = {
  idle:       'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
  thinking:   'linear-gradient(135deg, #f59e0b 0%, #ef4444 100%)',
  responding: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)',
  voice:      'linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%)',
}
const fabStyle = computed(() => ({
  '--fab-gradient': STATE_GRADIENTS[fabState.value] || STATE_GRADIENTS.idle,
}))

// ── Tooltip ────────────────────────────────────────────────
const MODULE_TIPS: Record<string, string> = {
  selection: '💬 AI 选款助手', design: '🎨 AI 设计助手',
  product:   '📦 AI 商品助手', inventory: '🏭 AI 库存助手',
  finance:   '💰 AI 财务助手', trend: '📈 AI 趋势助手',
  order:     '📋 AI 订单助手',
}
const tooltipText = computed(() => MODULE_TIPS[props.module] ?? '🤖 AI 助手')

// ── Click: particle burst + toggle ────────────────────────
function handleFabClick() {
  if (!props.open) spawnParticles()
  emit('toggle')
}

function spawnParticles() {
  const count = 12
  for (let i = 0; i < count; i++) {
    const angle  = (i / count) * 360
    const dist   = 40 + Math.random() * 30
    const rad    = (angle * Math.PI) / 180
    const tx     = Math.cos(rad) * dist
    const ty     = Math.sin(rad) * dist
    const colors = ['#a5b4fc', '#c4b5fd', '#fbcfe8', '#6ee7b7', '#fcd34d']
    const color  = colors[i % colors.length]
    const id     = ++particleId

    particles.value.push({
      id,
      style: {
        '--tx': `${tx}px`,
        '--ty': `${ty}px`,
        background: color,
        width:  `${4 + Math.random() * 5}px`,
        height: `${4 + Math.random() * 5}px`,
        left:   '50%',
        top:    '50%',
        animationDelay: `${i * 20}ms`,
      },
    })
    setTimeout(() => {
      particles.value = particles.value.filter(p => p.id !== id)
    }, 700)
  }
}

// ── Voice input (Web Speech API) ──────────────────────────
const voiceActive = ref(false)
let   recognition: any = null

function startVoice() {
  const SpeechRecognition =
    (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  if (!SpeechRecognition) return

  recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.interimResults = false
  recognition.maxAlternatives = 1
  voiceActive.value = true

  recognition.onresult = (e: any) => {
    const transcript = e.results[0][0].transcript
    voiceActive.value = false
    emit('voice-input', transcript)
  }
  recognition.onerror = () => { voiceActive.value = false }
  recognition.onend   = () => { voiceActive.value = false }
  recognition.start()
}

function stopVoice() {
  recognition?.stop()
  voiceActive.value = false
}

// Long-press → voice
let pressTimer: ReturnType<typeof setTimeout> | null = null
// (exposed via defineExpose for parent to trigger)

onUnmounted(() => {
  if (pressTimer) clearTimeout(pressTimer)
  recognition?.abort()
})

defineExpose({ startVoice, stopVoice })
</script>

<style scoped>
/* ── Root container ────────────────────────────────────── */
.fab-root {
  position: fixed;
  bottom: 28px; right: 28px;
  z-index: 1200;
  width: 60px; height: 60px;
  display: flex; align-items: center; justify-content: center;
  --fab-gradient: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

/* ── Orbital rings ────────────────────────────────────── */
.fab-orbit {
  position: absolute; border-radius: 50%;
  border: 1.5px solid rgba(99, 102, 241, 0.25);
  pointer-events: none;
}
.fab-orbit--1 { width: 80px;  height: 80px;  animation: orbit-spin 8s  linear infinite; }
.fab-orbit--2 { width: 100px; height: 100px; animation: orbit-spin 14s linear infinite reverse; }
.fab-orbit--3 { width: 120px; height: 120px; animation: orbit-spin 20s linear infinite; opacity: 0.5; }
@keyframes orbit-spin { to { transform: rotate(360deg); } }

/* State overrides for rings */
.fab-root--thinking  .fab-orbit { border-color: rgba(245, 158, 11, 0.35); animation-duration: 2s, 4s, 6s; }
.fab-root--responding .fab-orbit { border-color: rgba(16, 185, 129, 0.35); }
.fab-root--voice     .fab-orbit { border-color: rgba(236, 72, 153, 0.4); }

/* ── Outer glow ───────────────────────────────────────── */
.fab-glow {
  position: absolute; inset: -8px; border-radius: 50%;
  background: var(--fab-gradient);
  opacity: 0.18; filter: blur(12px);
  animation: glow-pulse 3s ease-in-out infinite;
  pointer-events: none;
}
@keyframes glow-pulse {
  0%, 100% { opacity: 0.18; transform: scale(1);    }
  50%      { opacity: 0.32; transform: scale(1.12); }
}
.fab-root--thinking  .fab-glow { animation-duration: 0.8s; }
.fab-root--responding .fab-glow { opacity: 0.28; }

/* ── Main button ──────────────────────────────────────── */
.fab-btn {
  position: relative; z-index: 2;
  width: 56px; height: 56px; border-radius: 50%;
  border: none; cursor: pointer; outline: none;
  background: var(--fab-gradient);
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.5);
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.28s cubic-bezier(.34,1.56,.64,1),
              box-shadow 0.2s,
              background 0.4s;
  overflow: hidden;
}
.fab-btn::before {
  content: '';
  position: absolute; inset: 0; border-radius: 50%;
  background: linear-gradient(135deg, rgba(255,255,255,0.25) 0%, transparent 60%);
}
.fab-btn:hover         { transform: scale(1.12) translateY(-2px); box-shadow: 0 8px 32px rgba(99,102,241,0.65); }
.fab-btn:active        { transform: scale(0.94); }
.fab-btn--active       { transform: scale(1.05); }
.fab-btn--voice        { animation: voice-pulse 0.6s ease-in-out infinite alternate; }
@keyframes voice-pulse { to { transform: scale(1.14); box-shadow: 0 0 0 8px rgba(236,72,153,0.2); } }

/* ── State icon ───────────────────────────────────────── */
.fab-icon { font-size: 26px; line-height: 1; user-select: none; transition: all 0.2s; }
.fab-icon--spin { animation: icon-spin 1.2s linear infinite; }
@keyframes icon-spin { to { transform: rotate(360deg); } }

/* ── Voice waves ──────────────────────────────────────── */
.fab-voice-waves {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; gap: 3px;
}
.fab-voice-waves span {
  width: 3px; background: rgba(255,255,255,0.8); border-radius: 3px;
  animation: wave 0.5s ease-in-out infinite alternate;
}
.fab-voice-waves span:nth-child(1) { height: 10px; animation-delay: 0s; }
.fab-voice-waves span:nth-child(2) { height: 18px; animation-delay: 0.15s; }
.fab-voice-waves span:nth-child(3) { height: 10px; animation-delay: 0.3s; }
@keyframes wave { to { transform: scaleY(2); } }

/* ── Particles ────────────────────────────────────────── */
.fab-particles { position: absolute; inset: 0; pointer-events: none; z-index: 3; }
.fab-particle {
  position: absolute; border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: particle-burst 0.65s ease-out forwards;
}
@keyframes particle-burst {
  0%   { transform: translate(-50%, -50%) scale(1); opacity: 1; }
  100% { transform: translate(calc(-50% + var(--tx)), calc(-50% + var(--ty))) scale(0.2); opacity: 0; }
}

/* ── Unread badge ─────────────────────────────────────── */
.fab-badge {
  position: absolute; top: -2px; right: -2px; z-index: 4;
  min-width: 20px; height: 20px; border-radius: 10px;
  background: #ef4444; color: #fff; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; padding: 0 4px;
  border: 2px solid #fff;
  animation: badge-pop 0.3s cubic-bezier(.34,1.56,.64,1);
}
@keyframes badge-pop { from { transform: scale(0); } }

/* ── Tooltip ──────────────────────────────────────────── */
.fab-tooltip {
  position: absolute; right: calc(100% + 12px); top: 50%;
  transform: translateY(-50%);
  background: rgba(17, 24, 39, 0.92); color: #fff;
  font-size: 12.5px; padding: 6px 12px; border-radius: 8px; white-space: nowrap;
  pointer-events: none;
}
.fab-tooltip::after {
  content: ''; position: absolute; left: 100%; top: 50%; transform: translateY(-50%);
  border: 5px solid transparent; border-left-color: rgba(17,24,39,0.92);
}

/* ── Transitions ──────────────────────────────────────── */
.fab-icon-enter-active, .fab-icon-leave-active { transition: opacity 0.18s, transform 0.18s; }
.fab-icon-enter-from   { opacity: 0; transform: scale(0.5) rotate(-30deg); }
.fab-icon-leave-to     { opacity: 0; transform: scale(0.5) rotate(30deg); }

.badge-enter-active, .badge-leave-active { transition: transform 0.2s, opacity 0.2s; }
.badge-enter-from, .badge-leave-to { transform: scale(0); opacity: 0; }

.tooltip-enter-active, .tooltip-leave-active { transition: opacity 0.15s, transform 0.15s; }
.tooltip-enter-from, .tooltip-leave-to { opacity: 0; transform: translateY(-50%) translateX(4px); }
</style>
