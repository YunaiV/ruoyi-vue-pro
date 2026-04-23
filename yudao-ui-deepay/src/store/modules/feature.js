import { defineStore } from 'pinia'
import { getRawFeatures } from '@/api/features'

/**
 * feature store — 全局功能开关缓存
 *
 * 启动时调用 load()，之后所有组件直接读 visibleMenus / isEnabled。
 * 后台把 ai_generate 的 visible 改成 false → 前端按钮/菜单自动消失。
 */
export const useFeatureStore = defineStore('feature', {
  state: () => ({
    /** 原始列表，key→feature 映射 */
    list: [],
    loaded: false,
  }),

  getters: {
    /** 按 sort 升序、只返回 visible=true 的功能（用于菜单渲染） */
    visibleMenus: (state) =>
      state.list
        .filter(i => i.visible)
        .sort((a, b) => (a.sort ?? 99) - (b.sort ?? 99)),

    /** 按 menuGroup 分组，只含 visible=true 的功能 */
    visibleByGroup: (state) => {
      const map = {}
      state.list
        .filter(i => i.visible)
        .sort((a, b) => (a.sort ?? 99) - (b.sort ?? 99))
        .forEach(f => {
          const g = f.menuGroup || 'other'
          if (!map[g]) map[g] = []
          map[g].push(f)
        })
      return map
    },
  },

  actions: {
    /**
     * 从后端加载功能列表。
     * 后端返回的是按分组的对象 → 展平为统一数组（key/name/path/visible/sort）。
     */
    async load() {
      try {
        const raw = await getRawFeatures()
        this.list = raw
      } catch (_) {
        // 兜底：用硬编码默认值，保证页面不白屏
        this.list = DEFAULT_LIST
      }
      this.loaded = true
    },

    /** 判断某个 key 是否可见（页面级开关用） */
    isEnabled(key) {
      const f = this.list.find(i => i.key === key)
      return f ? f.visible : false
    },
  },
})

/** 兜底默认数据（与后端 SQL 预置一致） */
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
