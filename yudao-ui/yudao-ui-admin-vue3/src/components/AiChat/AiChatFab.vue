<template>
  <!-- ===================================================================
    AiChatFab — 智能角色化浮动 AI 助手图标
    ● 每个模块自动呈现不同角色（购物顾问/设计师/财务总监…）
    ● 闲置 idleTimeout 秒后自动弹出情境感知气泡提示
    ● 悬停显示角色工具提示
    ● 点击时粒子爆炸动画
    ● 状态驱动梯度渐变（idle/attention/thinking/responding/voice）
    ● 语音输入 via Web Speech API
  =================================================================== -->
  <div class="fab-root" :data-state="fabState" :style="cssVars">

    <!-- 粒子层 -->
    <div class="fab-particles" aria-hidden="true">
      <span v-for="p in particles" :key="p.id" class="fab-particle" :style="p.style" />
    </div>

    <!-- 轨道环 -->
    <div class="fab-orbit fab-orbit--1" aria-hidden="true" />
    <div class="fab-orbit fab-orbit--2" aria-hidden="true" />
    <div class="fab-orbit fab-orbit--3" aria-hidden="true" />

    <!-- 光晕 -->
    <div class="fab-glow" aria-hidden="true" />

    <!-- 主按钮 -->
    <button
      class="fab-btn"
      :aria-label="open ? '关闭 AI 助手' : `打开 ${role.roleName}`"
      @click="handleClick"
      @mouseenter="onHoverStart"
      @mouseleave="onHoverEnd"
    >
      <!-- 呼吸环 (idle) -->
      <span v-if="fabState==='idle' && !open" class="fab-breath" />

      <!-- 语音波形 (voice) -->
      <span v-if="fabState==='voice'" class="fab-voice-waves">
        <span /><span /><span />
      </span>

      <!-- 动态角色图标 -->
      <Transition name="fab-icon" mode="out-in">
        <!-- Close X -->
        <span v-if="open"                       key="x" class="fab-icon fab-icon--close">✕</span>
        <!-- Voice recording -->
        <span v-else-if="fabState==='voice'"    key="v" class="fab-icon">🎙</span>
        <!-- Thinking spinner -->
        <span v-else-if="fabState==='thinking'" key="t" class="fab-icon fab-icon--spin">⚙️</span>
        <!-- Responding sparkle -->
        <span v-else-if="fabState==='responding'" key="r" class="fab-icon fab-icon--sparkle">✨</span>
        <!-- Attention: show SVG role avatar bouncing -->
        <span v-else-if="fabState==='attention'" key="a" class="fab-icon-svg fab-icon-svg--bounce">
          <RoleAvatar :module="module" :size="38" :animated="false" />
        </span>
        <!-- Idle: show SVG role avatar -->
        <span v-else key="i" class="fab-icon-svg">
          <RoleAvatar :module="module" :size="38" :animated="true" />
        </span>
      </Transition>
    </button>

    <!-- 未读徽标 -->
    <Transition name="badge">
      <span v-if="unread > 0 && !open" class="fab-badge">{{ unread > 9 ? '9+' : unread }}</span>
    </Transition>

      <!-- 角色名称标签（悬浮时显示）-->
      <Transition name="tooltip">
        <div v-if="tooltipVisible && !peekVisible && !open" class="fab-tooltip">
          <RoleAvatar :module="module" :size="20" :animated="false" class="fab-tooltip-avatar" />
          {{ role.roleName }} · {{ role.tagline }}
        </div>
      </Transition>

    <!-- 情境感知气泡提示 -->
    <Transition name="peek">
      <div
        v-if="peekVisible && !open"
        class="fab-peek"
        :class="{ 'fab-peek--attention': fabState==='attention' }"
        role="status"
      >
        <!-- 角色头像 (SVG) -->
        <div class="fab-peek-avatar">
          <RoleAvatar :module="module" :size="36" :animated="false" />
        </div>
        <div class="fab-peek-content">
          <div class="fab-peek-name">{{ role.roleName }}</div>
          <div class="fab-peek-text">{{ peekText }}</div>
        </div>
        <button class="fab-peek-close" @click.stop="dismissPeek" aria-label="关闭提示">✕</button>
      </div>
    </Transition>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { getRoleConfig } from '@/config/aiRoles'
import RoleAvatar from './RoleAvatar.vue'

// ── Props / Emits ──────────────────────────────────────────────
const props = withDefaults(defineProps<{
  open:         boolean
  loading:      boolean
  streaming?:   boolean
  ttsActive?:   boolean
  unread:       number
  module:       string
  /** Milliseconds before idle auto-peek fires. Default 30 000 */
  idleTimeout?: number
}>(), {
  idleTimeout: 30_000,
})

const emit = defineEmits<{
  (e: 'toggle'): void
  (e: 'voice-input', text: string): void
}>()

// ── Role config (reactive to module prop) ──────────────────────
const role = computed(() => getRoleConfig(props.module))

// ── State machine ──────────────────────────────────────────────
type FabState = 'idle' | 'attention' | 'thinking' | 'responding' | 'voice'

const _voiceActive = ref(false)
const idleAttention = ref(false)

const fabState = computed<FabState>(() => {
  if (_voiceActive.value)                              return 'voice'
  if (props.loading && !props.streaming)               return 'thinking'
  if (props.streaming)                                 return 'responding'
  if (idleAttention.value && !props.open)              return 'attention'
  return 'idle'
})

// ── CSS variables (state + role driven) ───────────────────────
const STATE_GRADS: Record<FabState, string | null> = {
  idle:       null,           // use role.gradient
  attention:  'linear-gradient(135deg, #f59e0b 0%, #fb923c 100%)',
  thinking:   'linear-gradient(135deg, #ef4444 0%, #f97316 100%)',
  responding: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)',
  voice:      'linear-gradient(135deg, #ec4899 0%, #a855f7 100%)',
}
const cssVars = computed(() => ({
  '--fab-grad': STATE_GRADS[fabState.value] ?? role.value.gradient,
}))

// ── Idle detection ─────────────────────────────────────────────
let idleTimer:   ReturnType<typeof setTimeout> | null = null
let peekTimer:   ReturnType<typeof setTimeout> | null = null
let tooltipTimer: ReturnType<typeof setTimeout> | null = null

function resetIdleTimer() {
  idleAttention.value = false
  dismissPeek()
  if (idleTimer) clearTimeout(idleTimer)
  idleTimer = setTimeout(() => {
    if (!props.open) { idleAttention.value = true; showAutoPeek() }
  }, props.idleTimeout)
}

// ── Peek bubble ────────────────────────────────────────────────
const peekVisible = ref(false)
const peekText    = ref('')
let   peekIndex   = 0

function showAutoPeek() {
  const hints = role.value.idleHints
  peekText.value   = hints[peekIndex % hints.length]
  peekIndex++
  peekVisible.value = true
  if (peekTimer) clearTimeout(peekTimer)
  peekTimer = setTimeout(dismissPeek, 9_000)
}

function dismissPeek() {
  peekVisible.value   = false
  idleAttention.value = false
  if (peekTimer) { clearTimeout(peekTimer); peekTimer = null }
}

// ── Tooltip on hover ───────────────────────────────────────────
const tooltipVisible = ref(false)

function onHoverStart() {
  if (tooltipTimer) clearTimeout(tooltipTimer)
  tooltipTimer = setTimeout(() => { tooltipVisible.value = true }, 450)
}
function onHoverEnd() {
  if (tooltipTimer) { clearTimeout(tooltipTimer); tooltipTimer = null }
  tooltipVisible.value = false
}

// ── Click ──────────────────────────────────────────────────────
function handleClick() {
  resetIdleTimer()
  dismissPeek()
  tooltipVisible.value = false
  if (!props.open) spawnParticles()
  emit('toggle')
}

// ── Particle burst ─────────────────────────────────────────────
interface Particle { id: number; style: Record<string, string> }
const particles = ref<Particle[]>([])
let pid = 0

function spawnParticles() {
  const COLORS = ['#a5b4fc','#c4b5fd','#fbcfe8','#6ee7b7','#fcd34d','#f9a8d4','#bfdbfe']
  const N = 14
  for (let i = 0; i < N; i++) {
    const angle = (i / N) * 360
    const dist  = 46 + Math.random() * 26
    const rad   = (angle * Math.PI) / 180
    const id    = ++pid
    const size  = 4 + Math.random() * 5
    particles.value.push({
      id,
      style: {
        '--tx':     `${Math.cos(rad) * dist}px`,
        '--ty':     `${Math.sin(rad) * dist}px`,
        background: COLORS[i % COLORS.length],
        width:      `${size}px`,
        height:     `${size}px`,
        left:       '50%', top: '50%',
        animationDelay: `${i * 16}ms`,
      },
    })
    setTimeout(() => { particles.value = particles.value.filter(p => p.id !== id) }, 750)
  }
}

// ── Voice (Web Speech API) ────────────────────────────────────
let recognition: any = null

function startVoice() {
  const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  if (!SR) { return }
  recognition = new SR()
  recognition.lang = 'zh-CN'
  recognition.interimResults  = false
  recognition.maxAlternatives = 1
  _voiceActive.value = true
  recognition.onresult = (e: any) => {
    _voiceActive.value = false
    emit('voice-input', e.results[0][0].transcript)
  }
  recognition.onerror = () => { _voiceActive.value = false }
  recognition.onend   = () => { _voiceActive.value = false }
  recognition.start()
}

function stopVoice() {
  recognition?.stop()
  _voiceActive.value = false
}

// ── Lifecycle ──────────────────────────────────────────────────
onMounted(() => {
  resetIdleTimer()
  const reset = () => resetIdleTimer()
  document.addEventListener('click',     reset, { passive: true })
  document.addEventListener('keydown',   reset, { passive: true })
  document.addEventListener('mousemove', reset, { passive: true })
  window.speechSynthesis?.getVoices?.()

  onUnmounted(() => {
    document.removeEventListener('click',     reset)
    document.removeEventListener('keydown',   reset)
    document.removeEventListener('mousemove', reset)
    if (idleTimer)    clearTimeout(idleTimer)
    if (peekTimer)    clearTimeout(peekTimer)
    if (tooltipTimer) clearTimeout(tooltipTimer)
    recognition?.abort()
  })
})

watch(() => props.open, v => { if (v) dismissPeek() })

// Reset peek index when module changes so hints cycle from start
watch(() => props.module, () => { peekIndex = 0 })

defineExpose({ startVoice, stopVoice })
</script>

<style scoped>
/* ── Root ─────────────────────────────────────────────────── */
.fab-root {
  position:fixed; bottom:28px; right:28px; z-index:1200;
  width:60px; height:60px;
  display:flex; align-items:center; justify-content:center;
  --fab-grad: linear-gradient(135deg,#6366f1 0%,#8b5cf6 100%);
}

/* ── Orbital rings ────────────────────────────────────────── */
.fab-orbit {
  position:absolute; border-radius:50%;
  border:1.5px solid rgba(99,102,241,0.2);
  pointer-events:none; transition:border-color 0.5s;
}
.fab-orbit--1 { width:82px;  height:82px;  animation:orbit 8s  linear infinite; }
.fab-orbit--2 { width:104px; height:104px; animation:orbit 14s linear infinite reverse; }
.fab-orbit--3 { width:126px; height:126px; animation:orbit 22s linear infinite; opacity:0.5; }
@keyframes orbit { to{transform:rotate(360deg);} }

[data-state="attention"]  .fab-orbit { border-color:rgba(245,158,11,0.4);  }
[data-state="thinking"]   .fab-orbit { border-color:rgba(239,68,68,0.4);   }
[data-state="responding"] .fab-orbit { border-color:rgba(16,185,129,0.4);  }
[data-state="voice"]      .fab-orbit { border-color:rgba(236,72,153,0.45); }

/* ── Glow ─────────────────────────────────────────────────── */
.fab-glow {
  position:absolute; inset:-10px; border-radius:50%;
  background:var(--fab-grad); opacity:0.15; filter:blur(14px);
  animation:glow 3s ease-in-out infinite; pointer-events:none; transition:opacity 0.5s;
}
@keyframes glow { 0%,100%{opacity:0.15;transform:scale(1);} 50%{opacity:0.28;transform:scale(1.14);} }
[data-state="attention"]  .fab-glow { animation-duration:0.9s; opacity:0.3; }
[data-state="thinking"]   .fab-glow { animation-duration:0.7s; }
[data-state="responding"] .fab-glow { opacity:0.26; }

/* ── Button ───────────────────────────────────────────────── */
.fab-btn {
  position:relative; z-index:2;
  width:56px; height:56px; border-radius:50%; border:none; cursor:pointer; outline:none;
  background:var(--fab-grad);
  box-shadow:0 4px 20px rgba(0,0,0,0.25);
  display:flex; align-items:center; justify-content:center; overflow:visible;
  transition:transform 0.3s cubic-bezier(.34,1.56,.64,1), box-shadow 0.25s, background 0.5s;
}
.fab-btn::after {
  content:''; position:absolute; inset:0; border-radius:50%;
  background:linear-gradient(135deg,rgba(255,255,255,0.22) 0%,transparent 60%);
  pointer-events:none;
}
.fab-btn:hover  { transform:scale(1.12) translateY(-3px); box-shadow:0 10px 28px rgba(0,0,0,0.3); }
.fab-btn:active { transform:scale(0.93); }

/* ── Breathing ring ───────────────────────────────────────── */
.fab-breath {
  position:absolute; inset:-5px; border-radius:50%;
  border:2px solid rgba(255,255,255,0.35);
  animation:breath 2.8s ease-in-out infinite; pointer-events:none;
}
@keyframes breath { 0%,100%{transform:scale(1);opacity:0.5;} 50%{transform:scale(1.35);opacity:0;} }

/* State overrides */
[data-state="attention"] .fab-btn { animation:attention-shake 0.5s ease-in-out 3; }
@keyframes attention-shake {
  0%,100%{transform:rotate(0);} 20%{transform:rotate(-9deg) scale(1.06);}
  40%{transform:rotate(9deg) scale(1.09);} 60%{transform:rotate(-5deg);} 80%{transform:rotate(5deg);}
}
[data-state="voice"] .fab-btn { animation:voice-pulse 0.6s ease-in-out infinite alternate; }
@keyframes voice-pulse { to{transform:scale(1.12); box-shadow:0 0 0 12px rgba(236,72,153,0.12);} }

/* ── Voice waves ──────────────────────────────────────────── */
.fab-voice-waves {
  position:absolute; inset:0; display:flex; align-items:center; justify-content:center; gap:3px; z-index:1;
}
.fab-voice-waves span {
  width:3px; background:rgba(255,255,255,0.85); border-radius:3px;
  animation:wave-bar 0.55s ease-in-out infinite alternate;
}
.fab-voice-waves span:nth-child(1){ height:8px;  animation-delay:0s; }
.fab-voice-waves span:nth-child(2){ height:18px; animation-delay:0.15s; }
.fab-voice-waves span:nth-child(3){ height:8px;  animation-delay:0.3s; }
@keyframes wave-bar { to{transform:scaleY(2.2);} }

/* ── Icons ────────────────────────────────────────────────── */
.fab-icon { font-size:26px; line-height:1; user-select:none; position:relative; z-index:2; }
.fab-icon--close   { font-size:22px; font-weight:700; color:#fff; }
.fab-icon--spin    { animation:icon-spin 1.2s linear infinite; }
.fab-icon--sparkle { animation:icon-sparkle 0.7s ease-in-out infinite alternate; }
.fab-icon--bounce  { animation:icon-bounce 0.65s ease-in-out infinite alternate; }

/* SVG avatar wrapper inside button */
.fab-icon-svg {
  display:flex; align-items:center; justify-content:center;
  position:relative; z-index:2;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.25));
}
.fab-icon-svg--bounce { animation:icon-bounce 0.65s ease-in-out infinite alternate; }

@keyframes icon-spin    { to{transform:rotate(360deg);} }
@keyframes icon-sparkle { to{transform:scale(1.2) rotate(10deg);} }
@keyframes icon-bounce  { to{transform:translateY(-5px) scale(1.18);} }

/* ── Particles ────────────────────────────────────────────── */
.fab-particles { position:absolute; inset:0; pointer-events:none; z-index:3; }
.fab-particle  {
  position:absolute; border-radius:50%;
  transform:translate(-50%,-50%);
  animation:particle 0.65s ease-out forwards;
}
@keyframes particle {
  0%   { transform:translate(-50%,-50%) scale(1); opacity:1; }
  100% { transform:translate(calc(-50% + var(--tx)), calc(-50% + var(--ty))) scale(0.1); opacity:0; }
}

/* ── Badge ────────────────────────────────────────────────── */
.fab-badge {
  position:absolute; top:-3px; right:-3px; z-index:4;
  min-width:20px; height:20px; border-radius:10px;
  background:#ef4444; color:#fff; font-size:11px; font-weight:700;
  display:flex; align-items:center; justify-content:center; padding:0 4px;
  border:2px solid #fff; animation:badge-pop 0.3s cubic-bezier(.34,1.56,.64,1);
}
@keyframes badge-pop { from{transform:scale(0);} }

/* ── Tooltip ──────────────────────────────────────────────── */
.fab-tooltip {
  position:absolute; right:calc(100% + 14px); top:50%; transform:translateY(-50%);
  background:rgba(17,24,39,0.92); color:#fff;
  font-size:12.5px; padding:6px 12px; border-radius:10px; white-space:nowrap;
  pointer-events:none; display:flex; align-items:center; gap:7px;
  box-shadow:0 4px 16px rgba(0,0,0,0.2);
}
.fab-tooltip::after {
  content:''; position:absolute; left:100%; top:50%; transform:translateY(-50%);
  border:5px solid transparent; border-left-color:rgba(17,24,39,0.92);
}
.fab-tooltip-avatar { border-radius:50%; overflow:hidden; flex-shrink:0; }

/* ── Peek bubble ──────────────────────────────────────────── */
.fab-peek {
  position:absolute; bottom:calc(100% + 16px); right:0;
  width:260px;
  background:#fff; border-radius:16px 16px 4px 16px;
  box-shadow:0 8px 32px rgba(0,0,0,0.14), 0 2px 8px rgba(0,0,0,0.07);
  padding:12px;
  display:flex; align-items:flex-start; gap:10px;
  border:1px solid rgba(0,0,0,0.06);
}
.fab-peek::after {
  content:''; position:absolute; bottom:-8px; right:22px;
  width:0; height:0;
  border-left:8px solid transparent; border-right:0 solid transparent;
  border-top:8px solid #fff;
  filter:drop-shadow(0 2px 2px rgba(0,0,0,0.06));
}
.fab-peek--attention {
  animation:peek-in 0.38s cubic-bezier(.34,1.56,.64,1);
  border-color:rgba(245,158,11,0.25);
  box-shadow:0 8px 32px rgba(245,158,11,0.16), 0 2px 8px rgba(0,0,0,0.07);
}
@keyframes peek-in { from{transform:scale(0.85) translateY(10px);opacity:0;} }

.fab-peek-avatar {
  flex-shrink:0; width:36px; height:36px; border-radius:50%; overflow:hidden;
}
.fab-peek-content { flex:1; min-width:0; }
.fab-peek-name { font-size:11px; font-weight:600; color:#6366f1; margin-bottom:3px; }
.fab-peek-text { font-size:13px; line-height:1.5; color:#374151; }
.fab-peek-close {
  flex-shrink:0; background:none; border:none; cursor:pointer;
  color:#9ca3af; font-size:12px; padding:0; line-height:1; transition:color 0.15s;
}
.fab-peek-close:hover { color:#6b7280; }

/* ── Transitions ──────────────────────────────────────────── */
.fab-icon-enter-active,.fab-icon-leave-active { transition:opacity 0.18s,transform 0.18s; }
.fab-icon-enter-from { opacity:0; transform:scale(0.4) rotate(-45deg); }
.fab-icon-leave-to   { opacity:0; transform:scale(0.4) rotate(45deg); }

.badge-enter-active,.badge-leave-active { transition:transform 0.2s,opacity 0.2s; }
.badge-enter-from,.badge-leave-to       { transform:scale(0); opacity:0; }

.peek-enter-active,.peek-leave-active { transition:opacity 0.24s,transform 0.24s; }
.peek-enter-from  { opacity:0; transform:translateY(10px) scale(0.9); }
.peek-leave-to    { opacity:0; transform:translateY(6px)  scale(0.94); }

.tooltip-enter-active,.tooltip-leave-active { transition:opacity 0.15s,transform 0.15s; }
.tooltip-enter-from,.tooltip-leave-to       { opacity:0; transform:translateY(-50%) translateX(5px); }
</style>
