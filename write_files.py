import os

# FILE 1: AdvancedUI.vue
advanced_ui = r'''<template>
  <div class="advanced-ui-root">
    <!-- Particle Background -->
    <div class="particle-bg" ref="particleBgRef"></div>

    <!-- Neon Title -->
    <div class="neon-title-section">
      <h1 class="neon-title">DeeyPay AI Studio</h1>
    </div>

    <!-- 3D Hover Cards -->
    <div class="section-block">
      <h2 class="section-heading">3D 悬浮卡片</h2>
      <div class="cards-grid">
        <div
          v-for="(card, index) in threeDCards"
          :key="card.id"
          class="three-d-card"
          :style="{
            '--mouse-x': card.mouseX + 'px',
            '--mouse-y': card.mouseY + 'px',
            '--card-rotation-x': card.rotationX + 'deg',
            '--card-rotation-y': card.rotationY + 'deg',
            background: card.gradient
          }"
          @mousemove="handleCardMove($event, index)"
          @mouseleave="resetCard(index)"
        >
          <div class="card-content">
            <div class="card-icon">{{ card.icon }}</div>
            <h3 class="card-title">{{ card.title }}</h3>
            <p class="card-desc">{{ card.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Text Waterfall -->
    <div class="section-block">
      <h2 class="section-heading">文字瀑布</h2>
      <div class="waterfall-container">
        <span
          v-for="(word, index) in waterfallWords"
          :key="index"
          class="waterfall-word"
          :style="{ animationDelay: index * 0.1 + 's', color: getWordColor(word) }"
        >{{ word }}</span>
      </div>
    </div>

    <!-- Floating Buttons -->
    <div class="section-block">
      <h2 class="section-heading">动效按钮</h2>
      <div class="floating-btns">
        <button
          v-for="btn in floatingBtns"
          :key="btn.id"
          class="floating-btn"
          :class="btn.effect"
          :style="{ background: btn.color }"
          @click="handleButtonClick(btn.action)"
        >{{ btn.label }}</button>
      </div>
    </div>

    <!-- Particle Canvas Cards -->
    <div class="section-block">
      <h2 class="section-heading">粒子画布</h2>
      <div class="canvas-cards-grid">
        <div
          v-for="(card, index) in particleCardsList"
          :key="index"
          class="canvas-card"
          @mouseenter="triggerParticleEffect(index)"
        >
          <canvas :ref="el => setParticleCanvas(el, index)" class="particle-canvas" />
          <div class="canvas-overlay">
            <p class="canvas-card-label">{{ card.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Parallax Scroll -->
    <div class="section-block parallax-section" ref="parallaxSectionRef">
      <h2 class="section-heading">视差滚动</h2>
      <div class="parallax-container">
        <div
          v-for="(item, index) in parallaxItems"
          :key="index"
          class="parallax-layer"
          :data-speed="item.speed"
          :style="{ fontSize: item.size + 'px', left: item.left + '%' }"
        >{{ item.emoji }}</div>
      </div>
    </div>

    <!-- Wave SVG Background -->
    <div class="wave-section">
      <svg class="wave-svg" viewBox="0 0 1440 120" preserveAspectRatio="none">
        <path class="wave-path" d="M0,60 C360,120 1080,0 1440,60 L1440,120 L0,120 Z" />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

// ─── 3D Cards ────────────────────────────────────────────────────────────────
interface ThreeDCard {
  id: number
  title: string
  description: string
  icon: string
  gradient: string
  mouseX: number
  mouseY: number
  rotationX: number
  rotationY: number
}

const threeDCards = ref<ThreeDCard[]>([
  { id: 1, title: 'AI 设计', description: '智能生成创意设计方案', icon: '🎨', gradient: 'linear-gradient(135deg,#667eea,#764ba2)', mouseX: 50, mouseY: 50, rotationX: 0, rotationY: 0 },
  { id: 2, title: '模特展示', description: '专业模特试衣展示系统', icon: '👗', gradient: 'linear-gradient(135deg,#f093fb,#f5576c)', mouseX: 50, mouseY: 50, rotationX: 0, rotationY: 0 },
  { id: 3, title: '数据分析', description: '销售数据智能分析', icon: '📊', gradient: 'linear-gradient(135deg,#4facfe,#00f2fe)', mouseX: 50, mouseY: 50, rotationX: 0, rotationY: 0 },
  { id: 4, title: '模板库', description: '海量电商设计模板', icon: '🖼️', gradient: 'linear-gradient(135deg,#43e97b,#38f9d7)', mouseX: 50, mouseY: 50, rotationX: 0, rotationY: 0 },
])

function handleCardMove(event: MouseEvent, index: number) {
  const card = (event.currentTarget as HTMLElement)
  const rect = card.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  const rotX = ((y - centerY) / centerY) * -15
  const rotY = ((x - centerX) / centerX) * 15
  threeDCards.value[index].rotationX = rotX
  threeDCards.value[index].rotationY = rotY
  threeDCards.value[index].mouseX = x
  threeDCards.value[index].mouseY = y
}

function resetCard(index: number) {
  threeDCards.value[index].rotationX = 0
  threeDCards.value[index].rotationY = 0
  threeDCards.value[index].mouseX = 50
  threeDCards.value[index].mouseY = 50
}

// ─── Text Waterfall ───────────────────────────────────────────────────────────
const waterfallWords = ref([
  '创意', '设计', '智能', '模特', '时尚', '科技', '美丽', '灵感',
  '电商', '品牌', '艺术', '创新', '数字', '视觉', '潮流', '未来',
  '像素', '渲染', '色彩', '光影',
])

const wordColors = ['#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#c77dff', '#ff9a3c', '#00bbf0', '#f72585']

function getWordColor(word: string): string {
  return wordColors[word.length % wordColors.length]
}

// ─── Floating Buttons ─────────────────────────────────────────────────────────
const floatingBtns = ref([
  { id: 1, label: '🎆 粒子爆炸', color: 'linear-gradient(135deg,#ff6b6b,#ee5a24)', effect: 'btn-ripple', action: 'explosion' },
  { id: 2, label: '🎇 烟花绽放', color: 'linear-gradient(135deg,#ffd93d,#f9ca24)', effect: 'btn-pulse', action: 'fireworks' },
  { id: 3, label: '⚡ 闪电效果', color: 'linear-gradient(135deg,#6bcb77,#009432)', effect: 'btn-shine', action: 'lightning' },
  { id: 4, label: '🔄 旋转卡片', color: 'linear-gradient(135deg,#4d96ff,#0652dd)', effect: 'btn-ripple', action: 'rotate' },
  { id: 5, label: '🌈 渐变切换', color: 'linear-gradient(135deg,#c77dff,#6c5ce7)', effect: 'btn-pulse', action: 'gradient' },
])

function handleButtonClick(action: string) {
  switch (action) {
    case 'explosion': createParticleExplosion(); break
    case 'fireworks': createFireworks(); break
    case 'lightning': createLightning(); break
    case 'rotate': rotateElements(); break
    case 'gradient': cycleGradients(); break
  }
}

function createParticleExplosion() {
  const container = document.querySelector('.advanced-ui-root') as HTMLElement
  if (!container) return
  const rect = container.getBoundingClientRect()
  const cx = rect.width / 2
  const cy = rect.height / 2
  for (let i = 0; i < 50; i++) {
    const p = document.createElement('div')
    p.style.cssText = `
      position:fixed; width:8px; height:8px; border-radius:50%;
      background:${wordColors[i % wordColors.length]};
      left:${cx}px; top:${cy + window.scrollY}px;
      pointer-events:none; z-index:9999;
      transition: transform 1s ease-out, opacity 1s ease-out;
    `
    document.body.appendChild(p)
    const angle = (i / 50) * Math.PI * 2
    const distance = 100 + Math.random() * 200
    const tx = Math.cos(angle) * distance
    const ty = Math.sin(angle) * distance
    setTimeout(() => {
      p.style.transform = `translate(${tx}px, ${ty}px)`
      p.style.opacity = '0'
    }, 10)
    setTimeout(() => p.remove(), 1100)
  }
}

function createFireworks() {
  const rect = document.querySelector('.advanced-ui-root')?.getBoundingClientRect()
  if (!rect) return
  for (let i = 0; i < 5; i++) {
    setTimeout(() => {
      createFirework(Math.random() * rect.width, Math.random() * rect.height)
    }, i * 200)
  }
}

function createFirework(x: number, y: number) {
  for (let i = 0; i < 60; i++) {
    const p = document.createElement('div')
    const color = wordColors[Math.floor(Math.random() * wordColors.length)]
    p.style.cssText = `
      position:fixed; width:5px; height:5px; border-radius:50%;
      background:${color}; left:${x}px; top:${y}px;
      pointer-events:none; z-index:9999;
      transition: transform 0.8s ease-out, opacity 0.8s ease-out;
    `
    document.body.appendChild(p)
    const angle = (i / 60) * Math.PI * 2
    const d = 50 + Math.random() * 100
    setTimeout(() => {
      p.style.transform = `translate(${Math.cos(angle) * d}px, ${Math.sin(angle) * d}px)`
      p.style.opacity = '0'
    }, 10)
    setTimeout(() => p.remove(), 900)
  }
}

function createLightning() {
  const flash = document.createElement('div')
  flash.style.cssText = `
    position:fixed; inset:0; background:rgba(255,255,255,0.9);
    pointer-events:none; z-index:9999;
    animation: lightning-flash 0.3s ease-out forwards;
  `
  document.body.appendChild(flash)
  const style = document.createElement('style')
  style.textContent = `@keyframes lightning-flash { 0%{opacity:0.9} 50%{opacity:0.1} 100%{opacity:0} }`
  document.head.appendChild(style)
  setTimeout(() => { flash.remove(); style.remove() }, 400)
}

function rotateElements() {
  const cards = document.querySelectorAll<HTMLElement>('.three-d-card')
  cards.forEach((card, i) => {
    card.style.transition = 'transform 0.5s ease'
    card.style.transform = `perspective(1000px) rotateY(${(i % 2 === 0 ? 1 : -1) * 360}deg)`
    setTimeout(() => { card.style.transform = '' }, 600)
  })
}

function cycleGradients() {
  const gradients = [
    'linear-gradient(135deg,#667eea,#764ba2)',
    'linear-gradient(135deg,#f093fb,#f5576c)',
    'linear-gradient(135deg,#4facfe,#00f2fe)',
    'linear-gradient(135deg,#43e97b,#38f9d7)',
  ]
  threeDCards.value.forEach((card, i) => {
    card.gradient = gradients[(i + 1) % gradients.length]
  })
}

// ─── Particle Canvas Cards ────────────────────────────────────────────────────
const particleCardsList = ref([
  { label: '粒子网络 A' },
  { label: '粒子网络 B' },
])

const particleCanvases = ref<(HTMLCanvasElement | null)[]>([null, null])
const animFrames: number[] = []

function setParticleCanvas(el: any, index: number) {
  particleCanvases.value[index] = el as HTMLCanvasElement
  if (el) {
    // Defer so DOM has dimensions
    setTimeout(() => initParticleCanvas(el as HTMLCanvasElement, index), 100)
  }
}

function getRandomColor(): string {
  return wordColors[Math.floor(Math.random() * wordColors.length)]
}

function createParticle(w: number, h: number) {
  return {
    x: Math.random() * w,
    y: Math.random() * h,
    size: Math.random() * 3 + 1,
    speedX: Math.random() * 2 - 1,
    speedY: Math.random() * 2 - 1,
    color: getRandomColor(),
    update(width: number, height: number) {
      this.x += this.speedX
      this.y += this.speedY
      if (this.x < 0 || this.x > width) this.speedX *= -1
      if (this.y < 0 || this.y > height) this.speedY *= -1
    },
    draw(ctx: CanvasRenderingContext2D) {
      ctx.beginPath()
      ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
      ctx.fillStyle = this.color
      ctx.fill()
    }
  }
}

function initParticleCanvas(canvas: HTMLCanvasElement, index: number) {
  canvas.width = canvas.parentElement?.offsetWidth || 300
  canvas.height = canvas.parentElement?.offsetHeight || 200
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  const particles = Array.from({ length: 40 }, () => createParticle(canvas.width, canvas.height))

  function loop() {
    ctx!.clearRect(0, 0, canvas.width, canvas.height)
    particles.forEach(p => {
      p.update(canvas.width, canvas.height)
      p.draw(ctx!)
    })
    // Draw connections
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < 80) {
          ctx!.beginPath()
          ctx!.strokeStyle = `rgba(100,150,255,${1 - dist / 80})`
          ctx!.lineWidth = 0.5
          ctx!.moveTo(particles[i].x, particles[i].y)
          ctx!.lineTo(particles[j].x, particles[j].y)
          ctx!.stroke()
        }
      }
    }
    animFrames[index] = requestAnimationFrame(loop)
  }
  loop()
}

function createParticleEffect(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement) {
  const burst = Array.from({ length: 20 }, () => {
    const angle = Math.random() * Math.PI * 2
    const speed = Math.random() * 4 + 1
    return {
      x: canvas.width / 2, y: canvas.height / 2,
      vx: Math.cos(angle) * speed, vy: Math.sin(angle) * speed,
      life: 1, color: getRandomColor()
    }
  })
  function animate() {
    burst.forEach(p => {
      p.x += p.vx; p.y += p.vy; p.life -= 0.02
      ctx.beginPath(); ctx.arc(p.x, p.y, 3, 0, Math.PI * 2)
      ctx.fillStyle = p.color; ctx.globalAlpha = p.life; ctx.fill()
    })
    ctx.globalAlpha = 1
    if (burst.some(p => p.life > 0)) requestAnimationFrame(animate)
  }
  animate()
}

function triggerParticleEffect(index: number) {
  const canvas = particleCanvases.value[index]
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (ctx) createParticleEffect(ctx, canvas)
}

// ─── Particle Background ──────────────────────────────────────────────────────
const particleBgRef = ref<HTMLElement | null>(null)

function initParticleBackground() {
  const container = particleBgRef.value
  if (!container) return
  for (let i = 0; i < 50; i++) {
    const p = document.createElement('div')
    const size = Math.random() * 6 + 2
    p.className = 'bg-particle'
    p.style.cssText = `
      position:absolute;
      width:${size}px; height:${size}px;
      border-radius:50%;
      background:${getRandomColor()};
      left:${Math.random() * 100}%;
      top:${Math.random() * 100}%;
      opacity:${Math.random() * 0.5 + 0.1};
      animation: float-particle ${4 + Math.random() * 6}s ease-in-out infinite alternate;
      animation-delay:${Math.random() * 4}s;
    `
    container.appendChild(p)
  }
}

// ─── Parallax ─────────────────────────────────────────────────────────────────
const parallaxItems = ref([
  { emoji: '🌟', speed: 0.3, size: 32, left: 10 },
  { emoji: '🚀', speed: 0.5, size: 40, left: 25 },
  { emoji: '💎', speed: 0.2, size: 28, left: 50 },
  { emoji: '🎨', speed: 0.4, size: 36, left: 70 },
  { emoji: '✨', speed: 0.6, size: 24, left: 88 },
])

let scrollHandler: (() => void) | null = null

function initParallax() {
  scrollHandler = () => {
    const layers = document.querySelectorAll<HTMLElement>('.parallax-layer')
    layers.forEach(layer => {
      const speed = parseFloat(layer.dataset.speed || '0.3')
      const offset = window.scrollY * speed
      layer.style.transform = `translateY(${offset}px)`
    })
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })
}

// ─── Lifecycle ────────────────────────────────────────────────────────────────
onMounted(() => {
  initParticleBackground()
  initParallax()
})

onUnmounted(() => {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
  animFrames.forEach(id => cancelAnimationFrame(id))
})
</script>

<style scoped>
.advanced-ui-root {
  position: relative;
  overflow: hidden;
  padding: 24px 16px 48px;
  background: linear-gradient(180deg, #0a0a1a 0%, #1a0a2e 50%, #0d1b2a 100%);
  min-height: 100vh;
}

/* Particle background */
.particle-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

@keyframes float-particle {
  from { transform: translateY(0) scale(1); }
  to   { transform: translateY(-30px) scale(1.2); }
}

/* Neon Title */
.neon-title-section {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px 0 32px;
}

.neon-title {
  font-size: clamp(28px, 6vw, 56px);
  font-weight: 900;
  color: #fff;
  text-shadow:
    0 0 10px #c77dff,
    0 0 20px #c77dff,
    0 0 40px #c77dff,
    0 0 80px #7b2fff;
  animation: neon-breathe 3s ease-in-out infinite;
  letter-spacing: 0.05em;
}

@keyframes neon-breathe {
  0%, 100% {
    text-shadow:
      0 0 10px #c77dff,
      0 0 20px #c77dff,
      0 0 40px #c77dff,
      0 0 80px #7b2fff;
    opacity: 1;
  }
  50% {
    text-shadow:
      0 0 5px #c77dff,
      0 0 10px #c77dff,
      0 0 20px #c77dff,
      0 0 40px #7b2fff;
    opacity: 0.8;
  }
}

/* Sections */
.section-block {
  position: relative;
  z-index: 1;
  margin-bottom: 48px;
}

.section-heading {
  font-size: 20px;
  font-weight: 700;
  color: rgba(255,255,255,0.9);
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid #c77dff;
}

/* 3D Cards */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.three-d-card {
  position: relative;
  border-radius: 24px;
  padding: 24px;
  cursor: pointer;
  transform-style: preserve-3d;
  transform: perspective(1000px) rotateX(var(--card-rotation-x, 0deg)) rotateY(var(--card-rotation-y, 0deg));
  transition: transform 0.15s ease;
  box-shadow: 0 20px 60px rgba(0,0,0,0.4);
  overflow: hidden;
}

.three-d-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  opacity: 0;
  transition: opacity 0.3s;
  background: radial-gradient(
    circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
    rgba(255,255,255,0.2) 0%,
    transparent 50%
  );
}

.three-d-card:hover::before {
  opacity: 1;
}

.card-content {
  position: relative;
  z-index: 1;
}

.card-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px;
}

.card-desc {
  font-size: 13px;
  color: rgba(255,255,255,0.75);
  margin: 0;
}

/* Waterfall */
.waterfall-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 16px;
  background: rgba(255,255,255,0.05);
  border-radius: 16px;
}

.waterfall-word {
  font-size: 18px;
  font-weight: 700;
  opacity: 0;
  animation: word-fall 0.6s ease forwards;
}

@keyframes word-fall {
  from { opacity: 0; transform: translateY(-20px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Floating Buttons */
.floating-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.floating-btn {
  padding: 12px 20px;
  border: none;
  border-radius: 50px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0,0,0,0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}

.floating-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 28px rgba(0,0,0,0.4);
}

.floating-btn:active {
  transform: translateY(0);
}

.btn-ripple::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255,255,255,0.3);
  border-radius: 50px;
  transform: scale(0);
  transition: transform 0.4s ease;
}

.btn-ripple:active::after {
  transform: scale(2);
  opacity: 0;
}

.btn-pulse {
  animation: btn-pulse-anim 2s ease-in-out infinite;
}

@keyframes btn-pulse-anim {
  0%, 100% { box-shadow: 0 6px 20px rgba(0,0,0,0.3); }
  50%       { box-shadow: 0 6px 30px rgba(200,150,255,0.6); }
}

.btn-shine::before {
  content: '';
  position: absolute;
  top: 0; left: -100%;
  width: 60%; height: 100%;
  background: linear-gradient(120deg, transparent, rgba(255,255,255,0.3), transparent);
  animation: btn-shine-anim 2s linear infinite;
}

@keyframes btn-shine-anim {
  to { left: 150%; }
}

/* Canvas Cards */
.canvas-cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.canvas-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  height: 180px;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.1);
  cursor: pointer;
}

.particle-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.canvas-overlay {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  padding: 8px 12px;
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
}

.canvas-card-label {
  margin: 0;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

/* Parallax */
.parallax-section {
  min-height: 200px;
}

.parallax-container {
  position: relative;
  height: 180px;
  background: rgba(255,255,255,0.03);
  border-radius: 16px;
  overflow: hidden;
}

.parallax-layer {
  position: absolute;
  top: 30%;
  will-change: transform;
  transition: transform 0.05s linear;
  user-select: none;
}

/* Wave SVG */
.wave-section {
  position: relative;
  z-index: 1;
  margin-top: 32px;
}

.wave-svg {
  width: 100%;
  height: 80px;
  display: block;
}

.wave-path {
  fill: rgba(199, 125, 255, 0.2);
  animation: wave-move 4s ease-in-out infinite;
  transform-origin: center;
}

@keyframes wave-move {
  0%, 100% { d: path("M0,60 C360,120 1080,0 1440,60 L1440,120 L0,120 Z"); }
  50%       { d: path("M0,60 C360,0 1080,120 1440,60 L1440,120 L0,120 Z"); }
}
</style>
'''

# FILE 2: Home.vue
home_vue = r'''<template>
  <div class="home-page">
    <!-- Hero Stats -->
    <section class="hero-section">
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-value">128</span>
          <span class="stat-label">模特</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">5240</span>
          <span class="stat-label">模板</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">89%</span>
          <span class="stat-label">准确率</span>
        </div>
      </div>
    </section>

    <!-- Quick Actions -->
    <section class="section">
      <h2 class="section-title">快速入口</h2>
      <div class="quick-actions-grid">
        <div
          v-for="(action, index) in quickActions"
          :key="action.id"
          class="action-card"
          :style="{ background: action.gradient }"
          @click="router.push(action.path)"
        >
          <component :is="ICONS[index]" class="action-icon" />
          <p class="action-title">{{ action.title }}</p>
          <p class="action-desc">{{ action.description }}</p>
        </div>
      </div>
    </section>

    <!-- Recent Works -->
    <section class="section">
      <h2 class="section-title">近期作品</h2>
      <div class="works-grid">
        <div
          v-for="work in recentWorks"
          :key="work.id"
          class="work-card"
        >
          <div class="work-thumb" :style="{ background: work.color }">
            <span class="work-emoji">{{ work.emoji }}</span>
          </div>
          <div class="work-overlay">
            <p class="work-title">{{ work.title }}</p>
            <p class="work-date">{{ work.date }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- AI Recommendations -->
    <section class="section">
      <h2 class="section-title">AI 推荐</h2>
      <div class="recs-list">
        <div
          v-for="rec in aiRecs"
          :key="rec.id"
          class="rec-card"
        >
          <div class="rec-icon-wrap">
            <span class="rec-emoji">{{ rec.icon }}</span>
          </div>
          <div class="rec-body">
            <p class="rec-title">{{ rec.title }}</p>
            <p class="rec-desc">{{ rec.description }}</p>
          </div>
          <button class="rec-btn" @click="router.push(rec.path)">查看</button>
        </div>
      </div>
    </section>

    <!-- Advanced Animations Section -->
    <AdvancedUI />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  PhotoIcon,
  UsersIcon,
  RectangleGroupIcon,
  SparklesIcon,
  BookmarkSquareIcon,
  ChartBarIcon,
} from '@heroicons/vue/24/outline'
import AdvancedUI from '@/components/AdvancedUI.vue'

const router = useRouter()

const ICONS = [PhotoIcon, UsersIcon, RectangleGroupIcon, SparklesIcon, BookmarkSquareIcon, ChartBarIcon]

const quickActions = [
  { id: 1, gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', title: '图库',   description: '浏览海量设计灵感',   path: '/image-library' },
  { id: 2, gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', title: '模特库', description: '专业模特试衣展示',   path: '/model-library' },
  { id: 3, gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', title: '模板库', description: '电商设计模板',       path: '/template-library' },
  { id: 4, gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', title: 'AI设计', description: '智能生成新设计',     path: '/image-library' },
  { id: 5, gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', title: '设计库', description: '您的设计作品',       path: '/design-library' },
  { id: 6, gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)', title: 'AI销售', description: '销售数据分析',       path: '/ai-sales' },
]

const recentWorks = [
  { id: 1, title: '夏季新款海报', date: '2024-01-15', color: 'linear-gradient(135deg,#667eea,#764ba2)', emoji: '🖼️' },
  { id: 2, title: '模特展示图', date: '2024-01-14', color: 'linear-gradient(135deg,#f093fb,#f5576c)', emoji: '👗' },
  { id: 3, title: '电商主图设计', date: '2024-01-13', color: 'linear-gradient(135deg,#4facfe,#00f2fe)', emoji: '🛒' },
]

const aiRecs = [
  { id: 1, icon: '🤖', title: 'AI 模特推荐', description: '根据您的风格偏好，为您推荐最合适的模特', path: '/model-library' },
  { id: 2, icon: '✨', title: '智能设计建议', description: '基于当前流行趋势，为您提供设计灵感', path: '/image-library' },
]
</script>

<style scoped>
.home-page {
  background: var(--dp-bg, #f8fafc);
  min-height: 100vh;
  padding-bottom: 120px;
}

/* Hero */
.hero-section {
  padding: 20px 16px 16px;
}

.stats-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: var(--dp-surface, #fff);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--dp-shadow, 0 2px 12px rgba(0,0,0,0.08));
  border: 1px solid var(--dp-card-border, #e5e7eb);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--dp-accent, #6c5ce7);
}

.stat-label {
  font-size: 13px;
  color: var(--dp-text-sub, #6b7280);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: var(--dp-card-border, #e5e7eb);
}

/* Section */
.section {
  padding: 0 16px 8px;
  margin-top: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--dp-text-bright, #111827);
  margin: 0 0 12px;
}

/* Quick Actions */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.action-card {
  border-radius: 16px;
  padding: 16px 10px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  transition: transform 0.2s, box-shadow 0.2s;
  -webkit-tap-highlight-color: transparent;
}

.action-card:active {
  transform: scale(0.96);
}

.action-icon {
  width: 28px;
  height: 28px;
  color: #fff;
  flex-shrink: 0;
}

.action-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
}

.action-desc {
  margin: 0;
  font-size: 11px;
  color: rgba(255,255,255,0.8);
  text-align: center;
  line-height: 1.3;
}

/* Recent Works */
.works-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.work-card {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  aspect-ratio: 3/4;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.work-thumb {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.work-emoji {
  font-size: 36px;
}

.work-overlay {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  padding: 8px;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
}

.work-title {
  margin: 0;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.work-date {
  margin: 0;
  font-size: 10px;
  color: rgba(255,255,255,0.7);
}

/* AI Recommendations */
.recs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rec-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: var(--dp-surface, #fff);
  border-radius: 16px;
  padding: 16px;
  box-shadow: var(--dp-shadow, 0 2px 12px rgba(0,0,0,0.08));
  border: 1px solid var(--dp-card-border, #e5e7eb);
}

.rec-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #0d9488, #14b8a6);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.rec-emoji {
  font-size: 24px;
}

.rec-body {
  flex: 1;
  min-width: 0;
}

.rec-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
  color: var(--dp-text-bright, #111827);
}

.rec-desc {
  margin: 0;
  font-size: 12px;
  color: var(--dp-text-sub, #6b7280);
  line-height: 1.4;
}

.rec-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #0d9488, #14b8a6);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.15s;
}

.rec-btn:active {
  transform: scale(0.95);
}
</style>
'''

out_dir = '/home/runner/work/deepaystudio/deepaystudio/deepay-pwa/src'

with open(f'{out_dir}/components/AdvancedUI.vue', 'w', encoding='utf-8') as f:
    f.write(advanced_ui)

with open(f'{out_dir}/views/Home.vue', 'w', encoding='utf-8') as f:
    f.write(home_vue)

print('Files written successfully.')
