import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ① 主页 — 聊天交互
  { path: '/',                 component: () => import('@/views/Home.vue'),            meta: { title: 'Deepay · 主页',    nav: 1 } },
  // ② 图库
  { path: '/image-library',    component: () => import('@/views/ImageLibrary.vue'),    meta: { title: 'Deepay · 图库',    nav: 2 } },
  // ③ AI 开店
  { path: '/ai-sales',         component: () => import('@/views/AISales.vue'),         meta: { title: 'Deepay · AI开店',  nav: 3 } },
  // ④ 模板库
  { path: '/template-library', component: () => import('@/views/TemplateLibrary.vue'), meta: { title: 'Deepay · 模板库',  nav: 4 } },
  // ⑤ 设置
  { path: '/settings',         component: () => import('@/views/Settings.vue'),        meta: { title: 'Deepay · 设置',    nav: 5 } },
  // ⑥ 管理后台 — 独立全屏，不走 App.vue 侧栏
  { path: '/admin',            component: () => import('@/views/Admin.vue'),           meta: { title: 'Deepay · 管理后台', admin: true } },
  // 旧路由 → 重定向
  { path: '/model-library',    redirect: '/image-library' },
  { path: '/design-library',   redirect: '/template-library' },
  // 兜底 404 → 主页
  { path: '/:pathMatch(.*)*',  redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  document.title = to.meta.title || 'Deepay'
})

export default router
