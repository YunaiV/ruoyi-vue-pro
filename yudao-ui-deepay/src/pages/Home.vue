<!--
  Home.vue — App首页（功能入口）
  Gemini-style design with hero + chips + templates + AI grid + quick links
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { templates } from '@/data/templates'
import { getQuotaInfo } from '@/api/design'
import { initUserId } from '@/utils/user'

const router  = useRouter()
const prompt  = ref('')

const USER_ID      = initUserId()
const remainFree   = ref(3)
const remainPaid   = ref(0)
const totalRemain  = computed(() => remainFree.value + remainPaid.value)

onMounted(async () => {
  try {
    const info = await getQuotaInfo(USER_ID)
    remainFree.value = info?.remainFree ?? 3
    remainPaid.value = info?.remainPaid ?? 0
  } catch (_) {}
})

function goGenerate() {
  const q = prompt.value.trim()
  router.push(q ? `/generate?prompt=${encodeURIComponent(q)}` : '/generate')
}

function goTemplate(id) {
  router.push(`/template/${id}`)
}

// Feature chips
const FEATURE_CHIPS = [
  { label: '✨ AI出款', path: '/ai/design' },
  { label: '🎭 灵感库', path: '/inspiration' },
  { label: '🌿 整季系列', path: '/ai/season' },
  { label: '🔧 改款工具', path: '/redesign' },
  { label: '📐 设计稿', path: '/generate' },
]

// AI tools grid
const AI_TOOLS = [
  { icon: '🎯', title: 'AI出款',   sub: '可控出款，系列生成', path: '/ai/design',   accent: false },
  { icon: '🎭', title: '灵感库',   sub: '精选秀场 + 品牌图',  path: '/inspiration', accent: true  },
  { icon: '🌿', title: '整季系列', sub: '一键生成 A/B/C',     path: '/ai/season',   accent: false },
  { icon: '🔧', title: '改款工具', sub: '参考图 → 新款',      path: '/redesign',    accent: false },
]

// Quick links grid (3 columns)
const QUICK_LINKS = [
  { icon: '🏪', title: '我的店铺', sub: () => myShops.value.length > 0 ? `${myShops.value.length} 家` : '暂无', path: '/me' },
  { icon: '🎨', title: 'AI设计',   sub: () => `每日 ${remainFree.value} 次`,                                     path: '/generate' },
  { icon: '✏️', title: 'AI改款',   sub: () => '上传参考图',                                                       path: '/redesign' },
  { icon: '🏆', title: '排行榜',   sub: () => '收益排名',                                                         path: '/leaderboard' },
  { icon: '🎁', title: '邀请好友', sub: () => '每邀一人得 €2',                                                    path: '/invite' },
  { icon: '📦', title: '全部模板', sub: () => `${templates.length} 套`,                                           path: '/template' },
]

// My stores from localStorage
const myShops = computed(() => {
  const result = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key?.startsWith('shop_')) {
      try {
        const data = JSON.parse(localStorage.getItem(key))
        result.push({ id: key.replace('shop_', ''), ...data })
      } catch (_) {}
    }
  }
  return result.slice(0, 3)
})

const inputFocused = ref(false)
</script>

<template>
  <div class="home-page">

    <!-- ── Hero Section ──────────────────────────── -->
    <section class="hero-section">
      <p class="hero-greeting">设计师，你好 👋</p>
      <h1 class="hero-title">今天想做什么款式？</h1>

      <!-- Quota badge -->
      <div class="quota-badge" :class="{ 'quota-low': totalRemain <= 1, 'quota-empty': totalRemain <= 0 }">
        {{ totalRemain <= 0 ? '次数已用完' : `剩余 ${totalRemain} 次免费生成` }}
      </div>
    </section>

    <!-- ── Feature Chips ─────────────────────────── -->
    <section class="chips-section">
      <div class="chips-scroll">
        <button
          v-for="chip in FEATURE_CHIPS"
          :key="chip.path"
          class="feature-chip"
          @click="router.push(chip.path)"
        >{{ chip.label }}</button>
      </div>
    </section>

    <!-- ── Template Horizontal Scroll ─────────────── -->
    <section class="section-block">
      <div class="section-header">
        <h2 class="section-title">快速开店模板</h2>
        <button class="section-more" @click="router.push('/template')">查看全部</button>
      </div>
      <div class="template-scroll">
        <div
          v-for="tpl in templates"
          :key="tpl.id"
          class="template-card"
          @click="goTemplate(tpl.id)"
        >
          <div class="template-cover" :style="{ background: tpl.gradient }">
            <span class="template-tag">{{ tpl.tag }}</span>
          </div>
          <div class="template-info">
            <p class="template-name">{{ tpl.name }}</p>
            <p class="template-count">{{ tpl.products.length }} 款商品</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ── AI Tools 2×2 Grid ─────────────────────── -->
    <section class="section-block">
      <h2 class="section-title">AI 设计系统</h2>
      <div class="tools-grid">
        <button
          v-for="tool in AI_TOOLS"
          :key="tool.path"
          class="tool-card"
          :class="{ 'tool-accent': tool.accent }"
          @click="router.push(tool.path)"
        >
          <span class="tool-icon">{{ tool.icon }}</span>
          <p class="tool-title">{{ tool.title }}</p>
          <p class="tool-sub" :class="{ 'tool-sub-accent': tool.accent }">{{ tool.sub }}</p>
        </button>
      </div>
    </section>

    <!-- ── Quick Links 3-col Grid ─────────────────── -->
    <section class="section-block">
      <h2 class="section-title">快捷入口</h2>
      <div class="quick-grid">
        <button
          v-for="link in QUICK_LINKS"
          :key="link.path"
          class="quick-card"
          @click="router.push(link.path)"
        >
          <span class="quick-icon">{{ link.icon }}</span>
          <p class="quick-title">{{ link.title }}</p>
          <p class="quick-sub">{{ link.sub() }}</p>
        </button>
      </div>
    </section>

    <!-- ── Recent Shops ───────────────────────────── -->
    <section v-if="myShops.length" class="section-block">
      <div class="section-header">
        <h2 class="section-title">最近店铺</h2>
        <button class="section-more" @click="router.push('/me')">全部</button>
      </div>
      <div class="shops-list">
        <div
          v-for="shop in myShops"
          :key="shop.id"
          class="shop-row"
          @click="router.push(`/shop/${shop.id}`)"
        >
          <div class="shop-thumb" :style="{ background: shop.gradient || '#1A1A1A' }" />
          <div class="shop-info">
            <p class="shop-name">{{ shop.name || 'AI设计款' }}</p>
            <p class="shop-url">/shop/{{ shop.id }}</p>
          </div>
          <svg class="shop-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/>
          </svg>
        </div>
      </div>
    </section>

    <!-- bottom padding -->
    <div style="height:24px" />

    <!-- ── Fixed Bottom Input Bar (above tabbar) ─── -->
    <div class="bottom-input-bar" :class="{ focused: inputFocused }">
      <div class="bottom-input-wrap">
        <span class="bottom-input-icon">✨</span>
        <input
          v-model="prompt"
          type="text"
          placeholder="描述你想要的款式，AI 即刻生成…"
          class="bottom-input"
          @focus="inputFocused = true"
          @blur="inputFocused = false"
          @keydown.enter="goGenerate"
        />
        <button class="bottom-input-send" @click="goGenerate">→</button>
      </div>
    </div>

  </div>
</template>

<style scoped>
.home-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 0 100px;
}

/* ── Hero ── */
.hero-section {
  padding: 28px 20px 16px;
}
.hero-greeting {
  font-size: 14px;
  color: var(--c-text-sub);
  margin: 0 0 6px;
}
.hero-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--c-text-bright);
  margin: 0 0 14px;
  line-height: 1.25;
  letter-spacing: -0.3px;
}
.quota-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(26,188,156,0.12);
  color: #1abc9c;
  border: 1px solid rgba(26,188,156,0.25);
}
.quota-low   { background: rgba(245,158,11,0.12); color: #f59e0b; border-color: rgba(245,158,11,0.25); }
.quota-empty { background: rgba(239,68,68,0.12);  color: #ef4444; border-color: rgba(239,68,68,0.25); }

/* ── Feature Chips ── */
.chips-section { padding: 8px 0 4px; }
.chips-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 4px 20px 8px;
  scrollbar-width: none;
}
.chips-scroll::-webkit-scrollbar { display: none; }
.feature-chip {
  flex-shrink: 0;
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid var(--c-card-border);
  background: var(--c-card);
  color: var(--c-text);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
}
.feature-chip:hover {
  border-color: rgba(26,188,156,0.4);
  background: rgba(26,188,156,0.06);
  color: #1abc9c;
}
.feature-chip:active { transform: scale(0.94); }

/* ── Section common ── */
.section-block { padding: 20px 20px 0; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.section-title {
  font-size: 15px; font-weight: 700;
  color: var(--c-text-bright); margin: 0 0 12px;
}
.section-header .section-title { margin: 0; }
.section-more {
  font-size: 12px; font-weight: 600;
  color: #1abc9c; background: transparent; border: none; cursor: pointer;
  padding: 4px 0;
}

/* ── Template scroll ── */
.template-scroll {
  display: flex; gap: 12px;
  overflow-x: auto; padding-bottom: 4px;
  scrollbar-width: none;
}
.template-scroll::-webkit-scrollbar { display: none; }
.template-card {
  flex-shrink: 0; width: 130px;
  background: var(--c-card);
  border: 1px solid var(--c-card-border);
  border-radius: 16px; overflow: hidden;
  cursor: pointer; transition: transform 0.15s;
}
.template-card:active { transform: scale(0.95); }
.template-cover {
  height: 96px; display: flex; align-items: flex-end; padding: 8px;
}
.template-tag {
  font-size: 10px; padding: 3px 8px; border-radius: 20px;
  background: rgba(0,0,0,0.45); color: rgba(255,255,255,0.85);
  backdrop-filter: blur(6px);
}
.template-info { padding: 10px; }
.template-name  { font-size: 13px; font-weight: 600; color: var(--c-text-bright); margin: 0 0 3px; }
.template-count { font-size: 11px; color: var(--c-text-sub); margin: 0; }

/* ── AI Tools grid ── */
.tools-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
}
.tool-card {
  background: var(--c-card);
  border: 1px solid var(--c-card-border);
  border-radius: 16px; padding: 16px;
  text-align: left; cursor: pointer;
  transition: all 0.15s;
}
.tool-card:hover { border-color: rgba(26,188,156,0.35); }
.tool-card:active { transform: scale(0.96); }
.tool-icon  { font-size: 28px; display: block; margin-bottom: 8px; }
.tool-title { font-size: 14px; font-weight: 700; color: var(--c-text-bright); margin: 0 0 4px; }
.tool-sub   { font-size: 12px; color: var(--c-text-sub); margin: 0; }
.tool-sub-accent { color: #1abc9c !important; }

/* ── Quick links 3-col ── */
.quick-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px;
}
.quick-card {
  background: var(--c-card);
  border: 1px solid var(--c-card-border);
  border-radius: 14px; padding: 14px 10px;
  text-align: center; cursor: pointer;
  transition: all 0.15s;
}
.quick-card:hover { border-color: rgba(26,188,156,0.3); }
.quick-card:active { transform: scale(0.94); }
.quick-icon  { font-size: 24px; display: block; margin-bottom: 6px; }
.quick-title { font-size: 13px; font-weight: 700; color: var(--c-text-bright); margin: 0 0 3px; }
.quick-sub   { font-size: 11px; color: var(--c-text-sub); margin: 0; }

/* ── Recent Shops ── */
.shops-list  { display: flex; flex-direction: column; gap: 8px; }
.shop-row {
  display: flex; align-items: center; gap: 12px;
  background: var(--c-card);
  border: 1px solid var(--c-card-border);
  border-radius: 14px; padding: 12px;
  cursor: pointer; transition: all 0.15s;
}
.shop-row:active { transform: scale(0.98); }
.shop-thumb { width: 46px; height: 46px; border-radius: 12px; flex-shrink: 0; }
.shop-info  { flex: 1; min-width: 0; }
.shop-name  { font-size: 14px; font-weight: 600; color: var(--c-text-bright); margin: 0 0 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.shop-url   { font-size: 12px; color: var(--c-text-sub); margin: 0; }
.shop-arrow { width: 16px; height: 16px; color: var(--c-text-sub); flex-shrink: 0; }

/* ── Bottom Input Bar ── */
.bottom-input-bar {
  position: fixed;
  bottom: calc(56px + env(safe-area-inset-bottom));
  left: 0; right: 0;
  padding: 10px 16px;
  background: var(--c-bg);
  border-top: 1px solid var(--c-border);
  transition: border-color 0.2s;
  z-index: 50;
}
.bottom-input-bar.focused { border-color: rgba(26,188,156,0.4); }
.bottom-input-wrap {
  display: flex; align-items: center; gap: 10px;
  background: var(--c-card);
  border: 1px solid var(--c-card-border);
  border-radius: 24px;
  padding: 8px 8px 8px 16px;
  max-width: 600px; margin: 0 auto;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.bottom-input-bar.focused .bottom-input-wrap {
  border-color: rgba(26,188,156,0.5);
  box-shadow: 0 0 0 3px rgba(26,188,156,0.08);
}
.bottom-input-icon { font-size: 18px; flex-shrink: 0; }
.bottom-input {
  flex: 1; background: transparent; border: none; outline: none;
  font-size: 14px; color: var(--c-text); min-width: 0;
}
.bottom-input::placeholder { color: var(--c-text-muted); }
.bottom-input-send {
  width: 36px; height: 36px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #1abc9c, #16a085);
  border: none; cursor: pointer; color: #fff;
  font-size: 16px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(26,188,156,0.4);
  transition: all 0.2s;
}
.bottom-input-send:active { transform: scale(0.9); }
</style>
