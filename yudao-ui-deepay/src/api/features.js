/**
 * features.js — 功能菜单配置接口
 *
 * 前端调用 /api/features 获取当前后台启用的功能列表，
 * 用于 Home.vue / Me.vue 动态渲染菜单卡片，不再硬编码。
 */
import http from '@/utils/request'

/**
 * 获取已启用的功能列表（按分组）。
 *
 * 返回结构：
 * {
 *   ai_design: [{ featureKey, featureName, icon, description, routePath, sortOrder }, …],
 *   material:  […],
 *   commerce:  […],
 *   ops:       […],
 * }
 *
 * 当后端不可用时，返回内置的默认数据，保证页面不白屏。
 */
export async function getFeatures() {
  try {
    return await http.get('/api/features')
  } catch (_) {
    return getDefaultFeatures()
  }
}

/**
 * 兜底默认数据（网络失败或后端未部署时使用）。
 * 与 SQL 预置数据保持一致。
 */
export function getDefaultFeatures() {
  return {
    ai_design: [
      { featureKey: 'ai_generate', featureName: 'AI出款生成', icon: '🎯', description: '可控出款，系列生成',   routePath: '/ai/design',   sortOrder: 1 },
      { featureKey: 'ai_season',   featureName: '整季系列',   icon: '🌿', description: '一键生成 A/B/C 整季', routePath: '/ai/season',   sortOrder: 2 },
      { featureKey: 'ai_redesign', featureName: '改款设计',   icon: '🔧', description: '参考图 → 新款',       routePath: '/redesign',    sortOrder: 3 },
      { featureKey: 'techpack',    featureName: '技术包',     icon: '📐', description: 'Tech Pack 设计稿',    routePath: '/ai/techpack', sortOrder: 4 },
    ],
    material: [
      { featureKey: 'inspiration', featureName: '灵感库', icon: '🎭', description: '精选秀场 + 品牌图', routePath: '/inspiration', sortOrder: 1 },
      { featureKey: 'template',    featureName: '模板库', icon: '📦', description: '快速开店模板',      routePath: '/template',    sortOrder: 2 },
    ],
    commerce: [
      { featureKey: 'shop',        featureName: '我的店铺', icon: '🏪', description: '管理和分享你的店铺',  routePath: '/me',          sortOrder: 1 },
      { featureKey: 'leaderboard', featureName: '收益排行', icon: '🏆', description: '看看你排第几名',     routePath: '/leaderboard', sortOrder: 2 },
      { featureKey: 'invite',      featureName: '邀请好友', icon: '🎁', description: '每邀请一人得 €2',    routePath: '/invite',      sortOrder: 3 },
      { featureKey: 'share',       featureName: '收益贡献', icon: '💸', description: '购买份额，持续分红', routePath: '/share',       sortOrder: 4 },
    ],
  }
}

/** 分组标题映射 */
export const GROUP_LABELS = {
  ai_design: '🎨 AI设计',
  material:  '🧠 设计素材',
  commerce:  '💰 商业中心',
  ops:       '⚙️ 系统运营',
}
