<!-- pages/index/index.vue — 首页（Gemini风格） -->
<template>
  <view class="page" :class="theme.isDark ? 'dp-dark' : 'dp-light'">

    <!-- ── 状态栏占位 ───────────────────────────── -->
    <view :style="{ height: statusBarHeight + 'px' }" />

    <!-- ── 顶部导航 ─────────────────────────────── -->
    <view class="header">
      <view class="header-left">
        <view class="logo">D</view>
        <text class="logo-text">Deepay</text>
      </view>
      <view class="header-right">
        <!-- 主题切换 -->
        <view class="icon-btn" @tap="theme.toggle()">
          <text class="icon-btn-icon">{{ theme.isDark ? '☀️' : '🌙' }}</text>
        </view>
        <!-- 配额 -->
        <view
          class="quota-badge"
          :style="{
            background: totalRemain <= 0 ? 'rgba(239,68,68,0.15)' :
                        totalRemain <= 1 ? 'rgba(245,158,11,0.15)' : 'rgba(26,188,156,0.15)',
            color: totalRemain <= 0 ? '#ef4444' : totalRemain <= 1 ? '#f59e0b' : '#1abc9c'
          }"
        >
          {{ totalRemain <= 0 ? '已用完' : `剩余 ${totalRemain} 次` }}
        </view>
      </view>
    </view>

    <!-- ── 主内容区（可滚动）────────────────────── -->
    <scroll-view
      class="scroll"
      scroll-y
      :style="{ height: scrollHeight + 'px' }"
    >

      <!-- Hero 打招呼 -->
      <view class="hero fade-up">
        <text class="hero-sub">设计师，你好 👋</text>
        <text class="hero-title">今天想做{{ '\n' }}什么款式？</text>
      </view>

      <!-- 功能芯片横滑（Gemini风格） -->
      <scroll-view class="chips-scroll" scroll-x>
        <view class="chips-row">
          <view
            v-for="chip in FEATURE_CHIPS"
            :key="chip.label"
            class="chip-feature"
            @tap="navigateTo(chip.path)"
          >
            <text>{{ chip.icon }}</text>
            <text class="chip-label">{{ chip.label }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 模板横滑卡片 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">快速开店模板</text>
          <text class="section-more" @tap="navigateTo('template/index')">全部 →</text>
        </view>
        <scroll-view class="templates-scroll" scroll-x>
          <view class="templates-row">
            <view
              v-for="tpl in TEMPLATES"
              :key="tpl.id"
              class="template-card"
              @tap="navigateTo('template/detail?id=' + tpl.id)"
            >
              <view class="template-cover" :style="{ background: tpl.gradient }">
                <text class="template-tag">{{ tpl.tag }}</text>
              </view>
              <view class="template-info">
                <text class="template-name">{{ tpl.name }}</text>
                <text class="template-count">{{ tpl.count }} 款商品</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- AI 设计工具 2×2 -->
      <view class="section">
        <text class="section-title" style="padding-left: 32rpx; display: block; margin-bottom: 20rpx;">AI 设计系统</text>
        <view class="tools-grid">
          <view
            v-for="tool in AI_TOOLS"
            :key="tool.label"
            class="tool-card card-glass fade-up"
            @tap="navigateTo(tool.path)"
          >
            <text class="tool-icon">{{ tool.icon }}</text>
            <text class="tool-label">{{ tool.label }}</text>
            <text class="tool-desc" :style="{ color: tool.color }">{{ tool.desc }}</text>
          </view>
        </view>
      </view>

      <!-- 快捷入口 3列 -->
      <view class="section">
        <text class="section-title" style="padding-left: 32rpx; display: block; margin-bottom: 20rpx;">快捷入口</text>
        <view class="quick-grid">
          <view
            v-for="link in QUICK_LINKS"
            :key="link.label"
            class="quick-card card-glass"
            @tap="navigateTo(link.path)"
          >
            <text class="quick-icon">{{ link.icon }}</text>
            <text class="quick-label">{{ link.label }}</text>
          </view>
        </view>
      </view>

      <view style="height: 40rpx;" />
    </scroll-view>

    <!-- ── 底部 ChatGPT 风格输入栏 ──────────────── -->
    <view class="input-bar" :style="{ bottom: tabbarHeight + 'px' }">
      <view class="input-inner">
        <view class="input-add" @tap="navigateTo('generate/index')">
          <text class="input-add-icon">＋</text>
        </view>
        <input
          v-model="prompt"
          class="input-field"
          placeholder="描述你想要的款式…"
          :placeholder-style="'color:' + (theme.isDark ? '#555' : '#bbb')"
          confirm-type="send"
          @confirm="goGenerate"
        />
        <view
          class="input-send"
          :style="{ background: prompt.trim() ? 'linear-gradient(135deg,#1abc9c,#16a085)' : (theme.isDark ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.06)') }"
          @tap="goGenerate"
        >
          <text
            class="input-send-icon"
            :style="{ color: prompt.trim() ? '#fff' : (theme.isDark ? '#555' : '#bbb') }"
          >→</text>
        </view>
      </view>
    </view>

    <!-- ── 底部 TabBar ──────────────────────────── -->
    <view class="dp-tabbar">
      <view class="dp-tabbar-item active" @tap="switchTab('index/index')">
        <text class="dp-tabbar-icon">🏠</text>
        <text>主页</text>
      </view>
      <view class="dp-tabbar-item" @tap="switchTab('history/history')">
        <text class="dp-tabbar-icon">🕐</text>
        <text>历史</text>
      </view>
      <view class="dp-tabbar-item" @tap="switchTab('settings/settings')">
        <text class="dp-tabbar-icon">⚙️</text>
        <text>设置</text>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useThemeStore } from '@/store/theme'
import { initUserId } from '@/utils/index'
import { designApi } from '@/api/index'

const theme = useThemeStore()

// ── 尺寸计算 ─────────────────────────────────────────────
const systemInfo     = uni.getSystemInfoSync()
const statusBarHeight = systemInfo.statusBarHeight || 44
const windowHeight   = systemInfo.windowHeight
const tabbarHeight   = 50 + (systemInfo.safeAreaInsets?.bottom || 0)
const inputBarHeight = 64
const scrollHeight   = computed(() => windowHeight - statusBarHeight - 56 - inputBarHeight - tabbarHeight)

// ── 配额 ─────────────────────────────────────────────────
const remainFree  = ref(3)
const remainPaid  = ref(0)
const totalRemain = computed(() => remainFree.value + remainPaid.value)
const prompt      = ref('')

onShow(async () => {
  try {
    const uid  = initUserId()
    const info = await designApi.getQuota(uid)
    remainFree.value = info?.remainFree ?? 3
    remainPaid.value = info?.remainPaid ?? 0
  } catch (_) {}
})

function goGenerate() {
  const q = prompt.value.trim()
  uni.navigateTo({
    url: '/pages/generate/index' + (q ? '?prompt=' + encodeURIComponent(q) : '')
  })
}

function navigateTo(path) {
  uni.navigateTo({ url: '/pages/' + path })
}

function switchTab(path) {
  uni.switchTab({ url: '/pages/' + path })
}

// ── 数据 ─────────────────────────────────────────────────
const FEATURE_CHIPS = [
  { icon: '🎯', label: 'AI出款',   path: 'generate/index' },
  { icon: '��', label: '灵感库',   path: 'inspiration/index' },
  { icon: '🌿', label: '整季系列', path: 'season/index' },
  { icon: '🔧', label: '改款工具', path: 'redesign/index' },
  { icon: '📐', label: '设计稿',   path: 'techpack/index' },
]

const TEMPLATES = [
  { id: 'minimal', name: '极简黑', tag: '极简', count: 1, gradient: 'linear-gradient(135deg,#0f0f0f,#2a2a2a)' },
  { id: 'street',  name: '街头风', tag: '街头', count: 2, gradient: 'linear-gradient(135deg,#1a0a0a,#3d1a1a)' },
  { id: 'luxury',  name: '高奢风', tag: '高端', count: 1, gradient: 'linear-gradient(135deg,#0a0a1a,#1a1a3d)' },
  { id: 'sport',   name: '运动系',  tag: '运动', count: 3, gradient: 'linear-gradient(135deg,#0a1a0f,#1a3d20)' },
  { id: 'korean',  name: '韩系甜', tag: '韩系', count: 2, gradient: 'linear-gradient(135deg,#1a0a14,#3d1a2a)' },
]

const AI_TOOLS = [
  { icon: '🎯', label: 'AI出款',   desc: '可控出款，系列生成', color: '#1abc9c', path: 'generate/index'    },
  { icon: '🎭', label: '灵感库',   desc: '精选秀场 + 品牌图',  color: '#1abc9c', path: 'inspiration/index' },
  { icon: '🌿', label: '整季系列', desc: '一键生成 A/B/C',    color: '#888',    path: 'season/index'      },
  { icon: '🔧', label: '改款工具', desc: '参考图 → 新款',     color: '#888',    path: 'redesign/index'    },
]

const QUICK_LINKS = [
  { icon: '🏪', label: '我的店铺', path: 'shop/mine' },
  { icon: '🎨', label: 'AI设计',   path: 'generate/index' },
  { icon: '✏️', label: 'AI改款',   path: 'redesign/index' },
  { icon: '🏆', label: '排行榜',   path: 'leaderboard/index' },
  { icon: '🎁', label: '邀请好友', path: 'invite/index' },
  { icon: '📐', label: '设计稿',   path: 'techpack/index' },
]
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--dp-bg);
  color: var(--dp-text);
  position: relative;
}

/* Header */
.header {
  height: 112rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--dp-bg);
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid var(--dp-border);
}
.header-left { display: flex; align-items: center; gap: 16rpx; }
.logo {
  width: 64rpx; height: 64rpx; border-radius: 16rpx;
  background: linear-gradient(135deg, #1abc9c 0%, #0d6e5a 100%);
  display: flex; align-items: center; justify-content: center;
  font-weight: 900; font-size: 28rpx; color: #fff;
  box-shadow: 0 4rpx 20rpx rgba(26,188,156,0.4);
}
.logo-text { font-weight: 700; font-size: 32rpx; color: var(--dp-text-bright); letter-spacing: 1rpx; }
.header-right { display: flex; align-items: center; gap: 16rpx; }
.icon-btn {
  width: 64rpx; height: 64rpx; border-radius: 50%;
  background: var(--dp-chip-bg); border: 1rpx solid var(--dp-chip-border);
  display: flex; align-items: center; justify-content: center; font-size: 32rpx;
}
.quota-badge {
  padding: 10rpx 24rpx; border-radius: 999rpx;
  font-size: 24rpx; font-weight: 600;
}

/* Scroll */
.scroll { padding: 0; }

/* Hero */
.hero {
  padding: 60rpx 32rpx 40rpx;
}
.hero-sub {
  display: block;
  font-size: 28rpx;
  color: var(--dp-text-sub);
  margin-bottom: 12rpx;
}
.hero-title {
  display: block;
  font-size: 56rpx;
  font-weight: 900;
  color: var(--dp-text-bright);
  line-height: 1.2;
  letter-spacing: -1rpx;
}

/* Feature chips */
.chips-scroll { padding: 0 32rpx; }
.chips-row {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  padding-bottom: 8rpx;
  width: max-content;
}
.chip-feature {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  padding: 18rpx 28rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 500;
  background: var(--dp-chip-bg);
  border: 1rpx solid var(--dp-chip-border);
  color: var(--dp-text);
  white-space: nowrap;
}
.chip-label { font-size: 26rpx; }

/* Section */
.section { margin-top: 48rpx; }
.section-header { display: flex; align-items: center; justify-content: space-between; padding: 0 32rpx; margin-bottom: 24rpx; }
.section-title   { font-size: 28rpx; font-weight: 700; color: var(--dp-text); }
.section-more    { font-size: 24rpx; font-weight: 600; color: #1abc9c; }

/* Templates */
.templates-scroll { padding: 0 32rpx; }
.templates-row {
  display: flex; flex-direction: row; gap: 24rpx;
  padding-bottom: 8rpx; width: max-content;
}
.template-card {
  width: 260rpx; border-radius: 24rpx; overflow: hidden;
  background: var(--dp-surface);
  border: 1rpx solid var(--dp-card-border);
  flex-shrink: 0;
}
.template-cover {
  height: 200rpx; width: 100%; position: relative;
  display: flex; align-items: flex-end; padding: 16rpx;
}
.template-tag {
  font-size: 20rpx; padding: 6rpx 16rpx; border-radius: 999rpx;
  background: rgba(0,0,0,0.5); color: rgba(255,255,255,0.8);
}
.template-info { padding: 20rpx 24rpx 24rpx; }
.template-name { display: block; font-size: 28rpx; font-weight: 700; color: var(--dp-text-bright); }
.template-count { display: block; font-size: 22rpx; color: var(--dp-text-muted); margin-top: 6rpx; }

/* AI tools grid */
.tools-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  padding: 0 32rpx;
}
.tool-card {
  padding: 36rpx 28rpx;
  border-radius: 24rpx;
  background: var(--dp-card);
  border: 1rpx solid var(--dp-card-border);
}
.tool-icon  { display: block; font-size: 52rpx; margin-bottom: 16rpx; }
.tool-label { display: block; font-size: 30rpx; font-weight: 700; color: var(--dp-text-bright); }
.tool-desc  { display: block; font-size: 22rpx; margin-top: 8rpx; }

/* Quick links grid */
.quick-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16rpx;
  padding: 0 32rpx;
}
.quick-card {
  padding: 32rpx 16rpx;
  border-radius: 20rpx;
  display: flex; flex-direction: column;
  align-items: center; gap: 12rpx;
  background: var(--dp-card);
  border: 1rpx solid var(--dp-card-border);
}
.quick-icon  { font-size: 44rpx; }
.quick-label { font-size: 22rpx; font-weight: 600; color: var(--dp-text); text-align: center; }

/* Input bar */
.input-bar {
  position: fixed;
  left: 0; right: 0;
  padding: 16rpx 32rpx;
  z-index: 100;
}
.input-inner {
  display: flex; align-items: center; gap: 16rpx;
  padding: 12rpx 12rpx;
  border-radius: 48rpx;
  background: var(--dp-input-bg);
  border: 1rpx solid var(--dp-card-border);
  box-shadow: 0 8rpx 48rpx rgba(0,0,0,0.4);
}
.input-add {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  background: var(--dp-chip-bg);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.input-add-icon { font-size: 36rpx; font-weight: 300; color: var(--dp-text-sub); }
.input-field {
  flex: 1; font-size: 28rpx; color: var(--dp-text);
  background: transparent; border: none; padding: 0 8rpx;
  height: 72rpx; line-height: 72rpx;
}
.input-send {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  transition: all 0.2s;
}
.input-send-icon { font-size: 32rpx; font-weight: 700; }
</style>
