import type { App } from 'vue'
import { getAccessToken } from '@/utils/auth'
import type { RouteRecordRaw } from 'vue-router'
import remainingRouter from './modules/remaining'
import { useTitle } from '@/hooks/web/useTitle'
import { useNProgress } from '@/hooks/web/useNProgress'
import { usePageLoading } from '@/hooks/web/usePageLoading'
import { createRouter, createWebHashHistory } from 'vue-router'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useDictStoreWithOut } from '@/store/modules/dict'
import { useUserStoreWithOut } from '@/store/modules/user'
import { listSimpleDictDataApi } from '@/api/system/dict/dict.data'
import { isRelogin } from '@/config/axios'
import { getInfoApi } from '@/api/login'

const { start, done } = useNProgress()

const { loadStart, loadDone } = usePageLoading()

// 创建路由实例
const router = createRouter({
  history: createWebHashHistory(), // createWebHashHistory URL带#，createWebHistory URL不带#
  strict: true,
  routes: remainingRouter as RouteRecordRaw[],
  scrollBehavior: () => ({ left: 0, top: 0 })
})

// 路由不重定向白名单
const whiteList = [
  '/login',
  '/social-login',
  '/auth-redirect',
  '/bind',
  '/register',
  '/oauthLogin/gitee'
]

// 路由加载前
router.beforeEach(async (to, from, next) => {
  start()
  loadStart()
  if (getAccessToken()) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      // 获取所有字典
      const dictStore = useDictStoreWithOut()
      const userStore = useUserStoreWithOut()
      const permissionStore = usePermissionStoreWithOut()
      if (!dictStore.getHasDictData) {
        const res = await listSimpleDictDataApi()
        dictStore.setDictMap(res)
      }
      if (userStore.getRoles.length === 0) {
        isRelogin.show = true
        const res = await getInfoApi()
        await userStore.setUserInfoAction(res)
        isRelogin.show = false
        // 后端过滤菜单
        await permissionStore.generateRoutes()
        permissionStore.getAddRouters.forEach((route) => {
          router.addRoute(route as unknown as RouteRecordRaw) // 动态添加可访问路由表
        })
        const redirectPath = from.query.redirect || to.path
        const redirect = decodeURIComponent(redirectPath as string)
        const nextData = to.path === redirect ? { ...to, replace: true } : { path: redirect }
        next(nextData)
      } else {
        next()
      }
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`) // 否则全部重定向到登录页
    }
  }
})

router.afterEach((to) => {
  useTitle(to?.meta?.title as string)
  done() // 结束Progress
  loadDone()
})

export const resetRouter = (): void => {
  const resetWhiteNameList = ['Redirect', 'Login', 'NoFind', 'Root']
  router.getRoutes().forEach((route) => {
    const { name } = route
    if (name && !resetWhiteNameList.includes(name as string)) {
      router.hasRoute(name) && router.removeRoute(name)
    }
  })
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
