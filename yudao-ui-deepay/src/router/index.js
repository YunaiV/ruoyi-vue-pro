import { createRouter, createWebHistory } from 'vue-router'
import Home            from '@/pages/Home.vue'
import Generate        from '@/pages/Generate.vue'
import Template        from '@/pages/Template.vue'
import TemplatePreview from '@/pages/TemplatePreview.vue'
import Shop            from '@/pages/Shop.vue'
import Me              from '@/pages/Me.vue'
import Leaderboard     from '@/pages/Leaderboard.vue'
import Landing         from '@/pages/Landing.vue'
import PayResult       from '@/pages/PayResult.vue'
import PayOrderResult  from '@/pages/PayOrderResult.vue'
import Pay             from '@/pages/Pay.vue'
import { isLoggedIn } from '@/utils/auth'

// ── 管理后台子页面（懒加载，按需拆分 chunk）──────────────────────────
const AdminLayout    = () => import('@/pages/admin/AdminLayout.vue')
const AdminHome      = () => import('@/pages/admin/AdminHome.vue')
const AdminInspiration = () => import('@/views/ai/inspiration/index.vue')
const AdminFeature   = () => import('@/pages/admin/FeatureConfig.vue')
const AdminProduct   = () => import('@/pages/admin/Product.vue')
const AdminOrder     = () => import('@/pages/admin/Order.vue')
const AdminPayment   = () => import('@/pages/admin/Payment.vue')
const AdminWallet    = () => import('@/pages/admin/Wallet.vue')

const routes = [
  // ── 公开路由 ──────────────────────────────────────────────────────
  { path: '/',             component: Home,            meta: { title: 'Deepay · 开店' } },
  { path: '/generate',     component: Generate,        meta: { title: 'AI生成' } },
  { path: '/redesign',     component: () => import('@/pages/Redesign.vue'),    meta: { title: 'AI改款设计' } },
  { path: '/inspiration',  component: () => import('@/pages/Inspiration.vue'), meta: { title: '时装灵感库' } },
  { path: '/ai/design',    component: () => import('@/pages/AiDesign.vue'),    meta: { title: 'AI出款工具' } },
  { path: '/template',     component: Template,        meta: { title: '模板' } },
  { path: '/template/:id', component: TemplatePreview, meta: { title: '模板预览' } },
  { path: '/shop/:id',      component: Shop,            meta: { title: '店铺' } },
  { path: '/shop/:id/edit', component: () => import('@/pages/ShopEdit.vue'), meta: { title: '编辑店铺' } },
  { path: '/login',        component: () => import('@/pages/Login.vue'), meta: { title: '登录' } },

  // ── 需要登录 ──────────────────────────────────────────────────────
  { path: '/me',          component: Me,          meta: { title: '我的',      requiresAuth: true } },
  { path: '/leaderboard', component: Leaderboard, meta: { title: '收益排行榜', requiresAuth: true } },
  { path: '/invite',      component: () => import('@/pages/Invite.vue'), meta: { title: '邀请好友', requiresAuth: true } },
  { path: '/share',       component: () => import('@/pages/Share.vue'),  meta: { title: '购买份额', requiresAuth: true } },

  // ── 支付 ──────────────────────────────────────────────────────────
  { path: '/pay/result',       component: PayResult,      meta: { title: '支付完成' } },
  { path: '/pay/order/result', component: PayOrderResult, meta: { title: '购买成功' } },
  { path: '/pay/:id',          component: Pay,            meta: { title: '支付结果' } },

  // ── 其他用户端 ────────────────────────────────────────────────────
  { path: '/landing',       component: Landing,                                               meta: { title: 'Deepay · AI爆款' } },
  { path: '/ai/season',     component: () => import('@/pages/AiSeason.vue'),                  meta: { title: '整季系列生成' } },
  { path: '/ai/techpack',   component: () => import('@/pages/TechPack.vue'),                  meta: { title: '设计稿' } },
  { path: '/design',        component: () => import('@/pages/AiPipeline.vue'),                meta: { title: 'AI设计流水线' } },
  { path: '/design/detail', component: () => import('@/pages/DesignDetail.vue'),              meta: { title: '设计稿' } },
  { path: '/brand',         component: () => import('@/pages/Brand.vue'),                     meta: { title: '品牌风格' } },

  // ── 管理后台（AdminLayout 嵌套，侧边栏动态读 feature store）──────
  // 规则：visible=false → 侧边栏菜单消失，但路由仍可通过 URL 直接访问
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '',            component: AdminHome,        meta: { title: 'Deepay Admin' } },

      // 🎨 AI 设计（feature store 控制菜单可见）
      { path: 'ai/generate', component: AdminInspiration, meta: { title: '出款生成' } },
      { path: 'ai/season',   component: AdminHome,        meta: { title: '整季系列（建设中）' } },
      { path: 'ai/redesign', component: AdminHome,        meta: { title: '改款设计（建设中）' } },
      { path: 'ai/techpack', component: AdminHome,        meta: { title: '技术包（建设中）' } },

      // 🧠 设计素材
      { path: 'inspiration', component: AdminInspiration, meta: { title: '灵感库' } },
      { path: 'template',    component: AdminHome,        meta: { title: '模板库（建设中）' } },

      // 💰 商业中心
      { path: 'shop',        component: AdminHome,        meta: { title: '店铺（建设中）' } },
      { path: 'leaderboard', component: AdminHome,        meta: { title: '排行榜（建设中）' } },
      { path: 'invite',      component: AdminHome,        meta: { title: '邀请（建设中）' } },
      { path: 'share',       component: AdminHome,        meta: { title: '收益贡献（建设中）' } },

      // ⚙️ 系统运营（P0，固定不受 feature 控制）
      { path: 'product',     component: AdminProduct,     meta: { title: '商品管理' } },
      { path: 'order',       component: AdminOrder,       meta: { title: '订单管理' } },
      { path: 'payment',     component: AdminPayment,     meta: { title: '支付流水' } },
      { path: 'wallet',      component: AdminWallet,      meta: { title: '钱包提现' } },
      { path: 'menu-config', component: AdminFeature,     meta: { title: '菜单配置' } },
    ],
  },

  // ── 404 ───────────────────────────────────────────────────────────
  { path: '/:pathMatch(.*)*', component: () => import('@/pages/NotFound.vue'), meta: { title: '页面不存在' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.afterEach(to => {
  document.title = to.meta.title || 'Deepay'
})

// ── 路由守卫：未登录跳转到登录页 ────────────────────────────────────
router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !isLoggedIn()) {
    // 记录目标路径，登录后可跳回
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
