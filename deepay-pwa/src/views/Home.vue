<template>
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
