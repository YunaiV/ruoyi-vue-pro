/**
 * features.js — 功能菜单配置接口
 *
 * getRawFeatures()  → 返回标准化数组 [{key, name, path, visible, sort, …}]
 *                     供 feature store 使用。
 * getFeatures()     → 仍保留分组对象格式，向后兼容旧调用方。
 */
import http from '@/utils/request'

/** 分组标题映射 */
export const GROUP_LABELS = {
  ai_design: '🎨 AI设计',
  material:  '🧠 设计素材',
  commerce:  '💰 商业中心',
  ops:       '⚙️ 系统运营',
}

/**
 * 获取功能列表，展平为标准数组。
 * 字段：{ key, name, path, visible, sort, menuGroup, icon, description }
 *
 * 后端返回分组对象时自动展平；
 * 后端不可用时返回兜底默认数据。
 */
export async function getRawFeatures() {
  try {
    const grouped = await http.get('/api/features')
    return flattenGrouped(grouped)
  } catch (_) {
    return DEFAULT_LIST
  }
}

/**
 * 把后端返回的分组对象展平为统一数组。
 * 后端格式：{ ai_design: [{featureKey, featureName, routePath, …}], … }
 * 输出格式：[{ key, name, path, visible, sort, menuGroup, icon, description }]
 */
function flattenGrouped(grouped) {
  if (!grouped || typeof grouped !== 'object') return DEFAULT_LIST
  const result = []
  let globalSort = 0
  for (const [group, items] of Object.entries(grouped)) {
    if (!Array.isArray(items)) continue
    items.forEach(f => {
      result.push({
        key:         f.featureKey  ?? f.key  ?? '',
        name:        f.featureName ?? f.name ?? '',
        path:        f.routePath   ?? f.path ?? '/',
        visible:     f.enabled !== undefined ? f.enabled === 1 : (f.visible ?? true),
        sort:        f.sortOrder   ?? f.sort ?? (++globalSort),
        menuGroup:   group,
        icon:        f.icon        ?? '',
        description: f.description ?? '',
      })
    })
  }
  return result.length ? result : DEFAULT_LIST
}

/**
 * 兜底默认数据（后端不可用时使用，与 SQL 预置一致）。
 * visible=true 表示"默认启用"。
 */
const DEFAULT_LIST = [
  { key: 'ai_generate',  name: 'AI出款生成', path: '/ai/design',   visible: true,  sort: 1,  menuGroup: 'ai_design', icon: '🎯', description: '可控出款，系列生成' },
  { key: 'ai_season',    name: '整季系列',   path: '/ai/season',   visible: true,  sort: 2,  menuGroup: 'ai_design', icon: '🌿', description: '一键生成 A/B/C 整季' },
  { key: 'ai_redesign',  name: '改款设计',   path: '/redesign',    visible: true,  sort: 3,  menuGroup: 'ai_design', icon: '🔧', description: '参考图 → 新款' },
  { key: 'techpack',     name: '技术包',     path: '/ai/techpack', visible: true,  sort: 4,  menuGroup: 'ai_design', icon: '📐', description: 'Tech Pack 设计稿' },
  { key: 'inspiration',  name: '灵感库',     path: '/inspiration', visible: true,  sort: 5,  menuGroup: 'material',  icon: '🎭', description: '精选秀场 + 品牌图' },
  { key: 'template',     name: '模板库',     path: '/template',    visible: true,  sort: 6,  menuGroup: 'material',  icon: '📦', description: '快速开店模板' },
  { key: 'shop',         name: '我的店铺',   path: '/me',          visible: true,  sort: 7,  menuGroup: 'commerce',  icon: '🏪', description: '管理和分享你的店铺' },
  { key: 'leaderboard',  name: '收益排行',   path: '/leaderboard', visible: true,  sort: 8,  menuGroup: 'commerce',  icon: '🏆', description: '看看你排第几名' },
  { key: 'invite',       name: '邀请好友',   path: '/invite',      visible: true,  sort: 9,  menuGroup: 'commerce',  icon: '🎁', description: '每邀请一人得 €2' },
  { key: 'share',        name: '收益贡献',   path: '/share',       visible: true,  sort: 10, menuGroup: 'commerce',  icon: '💸', description: '购买份额，持续分红' },
]

/** 向后兼容：返回分组对象格式（旧代码使用） */
export async function getFeatures() {
  const list = await getRawFeatures()
  const grouped = {}
  list.forEach(f => {
    const g = f.menuGroup || 'other'
    if (!grouped[g]) grouped[g] = []
    grouped[g].push(f)
  })
  return grouped
}
