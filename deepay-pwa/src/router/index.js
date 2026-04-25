import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/utils/storage'

const routes = [
  { path: '/',                component: () => import('@/views/Home.vue'),            meta: { title: 'Deepay · 首页' } },
  { path: '/image-library',   component: () => import('@/views/ImageLibrary.vue'),    meta: { title: '图库' } },
  { path: '/template-library',component: () => import('@/views/TemplateLibrary.vue'), meta: { title: '模板库' } },
  { path: '/model-library',   component: () => import('@/views/ModelLibrary.vue'),    meta: { title: '模特库' } },
  { path: '/design-library',  component: () => import('@/views/DesignLibrary.vue'),   meta: { title: '设计库' } },
  { path: '/ai-sales',        component: () => import('@/views/AISales.vue'),         meta: { title: 'AI销售' } },
  { path: '/settings',        component: () => import('@/views/Settings.vue'),        meta: { title: '设置' } },
  { path: '/:pathMatch(.*)*', redirect: '/' },
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
