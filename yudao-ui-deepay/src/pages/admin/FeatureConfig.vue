<!--
  admin/FeatureConfig.vue — 功能菜单配置页
  路径：/admin/menu-config
  ✔ 表格展示所有功能项（含分组）
  ✔ 每行有启用/禁用开关
  ✔ 调用 /admin/deepay/feature/* 接口（降级到本地数据）
-->
<script setup>
import { ref, onMounted } from 'vue'
import http from '@/utils/request'

// 分组标签
const GROUP_LABELS = {
  ai_design: '🎨 AI设计',
  material:  '🧠 设计素材',
  commerce:  '💰 商业中心',
  ops:       '⚙️ 系统运营',
}

// 本地兜底数据
const DEFAULT_FEATURES = [
  { id: 1,  featureKey: 'ai_generate',  featureName: 'AI出款生成', icon: '🎯', description: '可控出款，系列生成',   routePath: '/ai/design',   menuGroup: 'ai_design', sortOrder: 1, enabled: 1, visibleTo: 'all' },
  { id: 2,  featureKey: 'ai_season',    featureName: '整季系列',   icon: '🌿', description: '一键生成 A/B/C 整季', routePath: '/ai/season',   menuGroup: 'ai_design', sortOrder: 2, enabled: 1, visibleTo: 'all' },
  { id: 3,  featureKey: 'ai_redesign',  featureName: '改款设计',   icon: '🔧', description: '参考图 → 新款',       routePath: '/redesign',    menuGroup: 'ai_design', sortOrder: 3, enabled: 1, visibleTo: 'all' },
  { id: 4,  featureKey: 'techpack',     featureName: '技术包',     icon: '📐', description: 'Tech Pack 设计稿',    routePath: '/ai/techpack', menuGroup: 'ai_design', sortOrder: 4, enabled: 1, visibleTo: 'all' },
  { id: 5,  featureKey: 'inspiration',  featureName: '灵感库',     icon: '🎭', description: '精选秀场 + 品牌图',   routePath: '/inspiration', menuGroup: 'material',  sortOrder: 1, enabled: 1, visibleTo: 'all' },
  { id: 6,  featureKey: 'template',     featureName: '模板库',     icon: '📦', description: '快速开店模板',        routePath: '/template',    menuGroup: 'material',  sortOrder: 2, enabled: 1, visibleTo: 'all' },
  { id: 7,  featureKey: 'shop',         featureName: '我的店铺',   icon: '🏪', description: '管理和分享你的店铺',  routePath: '/me',          menuGroup: 'commerce',  sortOrder: 1, enabled: 1, visibleTo: 'all' },
  { id: 8,  featureKey: 'leaderboard',  featureName: '收益排行',   icon: '🏆', description: '看看你排第几名',      routePath: '/leaderboard', menuGroup: 'commerce',  sortOrder: 2, enabled: 1, visibleTo: 'all' },
  { id: 9,  featureKey: 'invite',       featureName: '邀请好友',   icon: '🎁', description: '每邀请一人得 €2',     routePath: '/invite',      menuGroup: 'commerce',  sortOrder: 3, enabled: 1, visibleTo: 'all' },
  { id: 10, featureKey: 'share',        featureName: '收益贡献',   icon: '💸', description: '购买份额，持续分红',  routePath: '/share',       menuGroup: 'commerce',  sortOrder: 4, enabled: 1, visibleTo: 'all' },
]

const features = ref([])
const loading   = ref(false)
const saveMsg   = ref('')

async function loadFeatures() {
  loading.value = true
  try {
    features.value = await http.get('/admin/deepay/feature/list')
  } catch (_) {
    features.value = DEFAULT_FEATURES.map(f => ({ ...f }))
  } finally {
    loading.value = false
  }
}

async function toggleEnabled(item) {
  const newVal = item.enabled === 1 ? 0 : 1
  try {
    await http.post('/admin/deepay/feature/batch-enable', { ids: [item.id] })
    item.enabled = newVal
  } catch (_) {
    item.enabled = newVal  // 本地先更新（离线模式）
  }
}

async function saveAll() {
  saveMsg.value = '保存中…'
  try {
    for (const f of features.value) {
      await http.post('/admin/deepay/feature/save', f)
    }
    saveMsg.value = '✅ 已保存'
  } catch (_) {
    saveMsg.value = '⚠️ 后端不可用，仅本地预览'
  }
  setTimeout(() => { saveMsg.value = '' }, 2500)
}

onMounted(loadFeatures)
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1 class="page-title">⚙️ 功能菜单配置</h1>
        <p class="page-sub">控制用户端首页显示哪些功能入口，修改后实时生效</p>
      </div>
      <div style="display:flex;align-items:center;gap:12px">
        <span v-if="saveMsg" class="save-msg">{{ saveMsg }}</span>
        <button class="btn-save" @click="saveAll">保存全部</button>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中…</div>

    <template v-else>
      <div v-for="group in ['ai_design','material','commerce','ops']" :key="group" class="group-block">
        <div class="group-title">{{ GROUP_LABELS[group] }}</div>
        <div class="feature-list">
          <div
            v-for="f in features.filter(x => x.menuGroup === group)"
            :key="f.featureKey"
            :class="['feature-row', f.enabled === 1 && 'enabled']"
          >
            <!-- 图标 + 名称 -->
            <div class="feat-main">
              <span class="feat-icon">{{ f.icon }}</span>
              <div>
                <div class="feat-name">{{ f.featureName }}</div>
                <div class="feat-path">{{ f.routePath }}</div>
              </div>
            </div>

            <!-- 描述 -->
            <div class="feat-desc">{{ f.description }}</div>

            <!-- 可见范围 -->
            <div class="feat-visible">
              <select v-model="f.visibleTo" class="sel-visible">
                <option value="all">所有人</option>
                <option value="vip">VIP</option>
                <option value="beta">内测</option>
                <option value="admin">仅管理员</option>
              </select>
            </div>

            <!-- 开关 -->
            <button
              :class="['toggle-btn', f.enabled === 1 && 'on']"
              @click="toggleEnabled(f)"
            >
              <span class="toggle-dot" />
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
}
.page-title { font-size: 22px; font-weight: 800; color: #fff; margin: 0 0 4px; }
.page-sub   { font-size: 13px; color: #666; margin: 0; }
.save-msg   { font-size: 13px; color: #00FF88; }
.btn-save {
  padding: 8px 20px;
  background: #00FF88;
  color: #000;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.btn-save:hover { opacity: 0.85; }

.loading { color: #555; padding: 40px 0; text-align: center; }

.group-block { margin-bottom: 32px; }
.group-title {
  font-size: 13px;
  font-weight: 700;
  color: #555;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #1a1a1a;
}

.feature-list { display: flex; flex-direction: column; gap: 6px; }

.feature-row {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #111;
  border: 1px solid #1e1e1e;
  border-radius: 12px;
  padding: 12px 16px;
  transition: border-color 0.15s;
}
.feature-row.enabled { border-color: #00FF8822; }

.feat-main  { display: flex; align-items: center; gap: 12px; min-width: 180px; }
.feat-icon  { font-size: 22px; flex-shrink: 0; }
.feat-name  { font-size: 14px; font-weight: 600; color: #fff; }
.feat-path  { font-size: 11px; color: #444; margin-top: 2px; }
.feat-desc  { flex: 1; font-size: 12px; color: #666; }

.feat-visible { flex-shrink: 0; }
.sel-visible {
  background: #1a1a1a;
  border: 1px solid #333;
  color: #aaa;
  border-radius: 8px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
}

/* 开关 */
.toggle-btn {
  width: 44px;
  height: 24px;
  border-radius: 12px;
  background: #333;
  border: none;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
  flex-shrink: 0;
}
.toggle-btn.on { background: #00FF88; }
.toggle-dot {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  transition: transform 0.2s;
  display: block;
}
.toggle-btn.on .toggle-dot { transform: translateX(20px); }
</style>
