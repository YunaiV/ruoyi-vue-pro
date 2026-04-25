<template>
  <div class="home-page">
    <!-- Hero -->
    <section class="hero dp-fade-up">
      <p class="hero-sub">设计师，你好 👋</p>
      <h1 class="hero-title">今天想做<br/>什么款式？</h1>
    </section>

    <!-- Feature Buttons -->
    <section class="section">
      <FeatureButtons @navigate="router.push($event)" />
    </section>

    <!-- Quick Templates -->
    <section class="section">
      <div class="section-header">
        <h2 class="dp-section-title">快速模板</h2>
        <button class="see-all" @click="router.push('/template-library')">查看全部</button>
      </div>
      <div class="template-scroll scrollbar-hide">
        <div
          v-for="(tpl, i) in templates"
          :key="tpl.name"
          class="tpl-card dp-card"
        >
          <div class="tpl-cover" :style="{ background: tpl.color }">
            <span class="tpl-emoji">{{ tpl.emoji }}</span>
          </div>
          <div class="tpl-info">
            <span class="tpl-name">{{ tpl.name }}</span>
            <span class="tpl-count">{{ tpl.count }}款</span>
          </div>
        </div>
      </div>
    </section>

    <!-- AI Tools Grid -->
    <section class="section">
      <h2 class="dp-section-title">AI工具</h2>
      <div class="tools-grid">
        <div
          v-for="tool in aiTools"
          :key="tool.title"
          class="tool-card dp-card"
          @click="router.push(tool.path)"
        >
          <div class="tool-icon">{{ tool.icon }}</div>
          <div class="tool-title">{{ tool.title }}</div>
          <div class="tool-desc">{{ tool.desc }}</div>
          <div v-if="tool.badge" class="dp-badge-new">{{ tool.badge }}</div>
        </div>
      </div>
    </section>

    <!-- Quick Links -->
    <section class="section">
      <h2 class="dp-section-title">快捷入口</h2>
      <div class="links-grid">
        <button
          v-for="link in quickLinks"
          :key="link.label"
          class="link-item dp-card"
          @click="router.push(link.path)"
        >
          <span class="link-icon">{{ link.icon }}</span>
          <span class="link-label">{{ link.label }}</span>
        </button>
      </div>
    </section>

    <!-- Bottom spacer for fixed input -->
    <div style="height: 80px;"></div>

    <!-- Fixed Input Bar -->
    <div class="bottom-input-bar">
      <button class="plus-btn" aria-label="添加">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
      </button>
      <input
        v-model="inputText"
        class="bottom-input dp-input"
        placeholder="描述你想要的款式…"
        @keydown.enter="openChatWithText"
      />
      <button
        class="bottom-send"
        :class="{ active: inputText.trim() }"
        @click="openChatWithText"
        aria-label="发送"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
          <path d="M22 2L11 13M22 2L15 22l-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import FeatureButtons from '@/components/FeatureButtons.vue'
import { useChatStore } from '@/store/index.js'

const router = useRouter()
const chatStore = useChatStore()
const inputText = ref('')

const templates = [
  { name: '极简风',  color: 'linear-gradient(135deg,#1a1a2e,#0f3460)', emoji: '🤍', count: 24 },
  { name: '街头潮',  color: 'linear-gradient(135deg,#2d1b69,#533483)', emoji: '🔥', count: 18 },
  { name: '高奢感',  color: 'linear-gradient(135deg,#1b1b1b,#c9a84c44)', emoji: '✨', count: 12 },
  { name: '运动系',  color: 'linear-gradient(135deg,#0f4c75,#1abc9c44)', emoji: '⚡', count: 30 },
  { name: '韩系甜',  color: 'linear-gradient(135deg,#3d0c11,#c93a5a44)', emoji: '🌸', count: 20 },
]

const aiTools = [
  { icon: '🎯', title: 'AI出款', desc: '描述款式，AI自动生成设计方案', path: '/', badge: 'NEW' },
  { icon: '🎭', title: '灵感图库', desc: '海量时尚灵感，一键收藏', path: '/image-library', badge: '' },
  { icon: '👗', title: '虚拟模特', desc: 'AI模特试穿，所见即所得', path: '/model-library', badge: '' },
  { icon: '📊', title: 'AI销售', desc: '市场分析+定价策略一站式', path: '/ai-sales', badge: 'HOT' },
]

const quickLinks = [
  { icon: '📐', label: '设计稿', path: '/design-library' },
  { icon: '📋', label: '模板库', path: '/template-library' },
  { icon: '👗', label: '模特库', path: '/model-library' },
  { icon: '🖼️', label: '图库',   path: '/image-library' },
  { icon: '📊', label: 'AI销售', path: '/ai-sales' },
  { icon: '⚙️', label: '设置',   path: '/settings' },
]

function openChatWithText() {
  const t = inputText.value.trim()
  if (!t) { chatStore.isOpen = true; return }
  if (!chatStore.activeId) chatStore.newSession()
  chatStore.addMessage('user', t)
  inputText.value = ''
  chatStore.isOpen = true
}
</script>

<style scoped>
.home-page {
  padding: 24px 16px 16px;
  max-width: 800px; margin: 0 auto;
}
.hero { margin-bottom: 28px; }
.hero-sub {
  font-size: 15px; color: var(--dp-text-sub); font-weight: 500; margin: 0 0 6px;
}
.hero-title {
  font-size: 32px; font-weight: 900; line-height: 1.15;
  letter-spacing: -0.03em; color: var(--dp-text-bright); margin: 0;
}
.section { margin-bottom: 28px; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.see-all { font-size: 13px; color: var(--dp-accent); background: none; border: none; cursor: pointer; font-weight: 500; }

/* Templates */
.template-scroll {
  display: flex; gap: 12px; overflow-x: auto; padding: 4px 2px;
}
.tpl-card { flex-shrink: 0; width: 120px; padding: 0; overflow: hidden; cursor: pointer; }
.tpl-cover {
  height: 160px; display: flex; align-items: center; justify-content: center;
  border-radius: 14px 14px 0 0;
}
.tpl-emoji { font-size: 40px; }
.tpl-info { padding: 8px 10px 10px; }
.tpl-name { display: block; font-size: 13px; font-weight: 600; color: var(--dp-text); }
.tpl-count { font-size: 11px; color: var(--dp-text-muted); }

/* AI Tools */
.tools-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.tool-card {
  padding: 18px 16px; cursor: pointer; position: relative;
}
.tool-icon { font-size: 28px; margin-bottom: 10px; }
.tool-title { font-size: 15px; font-weight: 700; color: var(--dp-text-bright); margin-bottom: 4px; }
.tool-desc { font-size: 12px; color: var(--dp-text-sub); line-height: 1.4; }
.dp-badge-new {
  position: absolute; top: 12px; right: 12px;
  background: #1abc9c; color: #fff; border-radius: 999px;
  font-size: 10px; font-weight: 700; padding: 2px 7px;
}

/* Quick Links */
.links-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.link-item {
  padding: 16px 8px; display: flex; flex-direction: column; align-items: center;
  gap: 8px; cursor: pointer; background: var(--dp-card);
  border: 1px solid var(--dp-card-border); border-radius: 14px;
  transition: all 0.2s; color: var(--dp-text);
}
.link-item:hover { border-color: rgba(26,188,156,0.35); color: #1abc9c; }
.link-icon { font-size: 24px; }
.link-label { font-size: 12px; font-weight: 600; }

/* Bottom input bar */
.bottom-input-bar {
  position: fixed;
  bottom: calc(64px + env(safe-area-inset-bottom));
  left: 0; right: 0;
  padding: 10px 16px;
  background: var(--dp-header-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--dp-border);
  display: flex; align-items: center; gap: 8px;
  z-index: 100;
}
@media (min-width: 1024px) {
  .bottom-input-bar { left: 240px; bottom: 0; }
}
.plus-btn {
  width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0;
  background: var(--dp-chip-bg); border: 1px solid var(--dp-chip-border);
  color: var(--dp-text-sub); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.plus-btn:hover { background: var(--dp-accent-bg); color: #1abc9c; border-color: rgba(26,188,156,0.4); }
.bottom-input {
  flex: 1; height: 40px; padding: 0 14px;
  border-radius: 20px;
  background: var(--dp-input-bg);
}
.bottom-send {
  width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0;
  background: var(--dp-chip-bg); border: 1px solid var(--dp-chip-border);
  color: var(--dp-text-muted); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.bottom-send.active {
  background: linear-gradient(135deg, #1abc9c, #16a085);
  color: white; border-color: transparent;
  box-shadow: 0 4px 14px rgba(26,188,156,0.4);
}
</style>
