import type { App } from 'vue'
import { getAccessToken } from '@/utils/auth'
import type { RouteRecordRaw } from 'vue-router'
import remainingRouter from './modules/remaining'
import { useCache } from '@/hooks/web/useCache'
import { useTitle } from '@/hooks/web/useTitle'
import { useNProgress } from '@/hooks/web/useNProgress'
import { usePageLoading } from '@/hooks/web/usePageLoading'
import { createRouter, createWebHashHistory } from 'vue-router'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useDictStoreWithOut } from '@/store/modules/dict'
import { listSimpleDictDataApi } from '@/api/system/dict/dict.data'

const permissionStore = usePermissionStoreWithOut()

const dictStore = useDictStoreWithOut()

const { wsCache } = useCache()

const { start, done } = useNProgress()

const { loadStart, loadDone } = usePageLoading()

// 创建路由实例
const router = createRouter({
  history: createWebHashHistory(),
  strict: true,
  routes: remainingRouter as RouteRecordRaw[],
  scrollBehavior: () => ({ left: 0, top: 0 })
})

// 路由白名单
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
      if (!dictStore.getIsSetDict) {
        // 获取所有字典
        const res = await listSimpleDictDataApi()
        if (res) {
          dictStore.setDictMap(res)
          dictStore.setIsSetDict(true)
        }
      }
      if (permissionStore.getIsAddRouters) {
        next()
        return
      }
      // 开发者可根据实际情况进行修改
      const roleRouters = wsCache.get('roleRouters') || []

      await permissionStore.generateRoutes(roleRouters as AppCustomRouteRecordRaw[])

      permissionStore.getAddRouters.forEach((route) => {
        router.addRoute(route as unknown as RouteRecordRaw) // 动态添加可访问路由表
      })
      const redirectPath = from.query.redirect || to.path
      const redirect = decodeURIComponent(redirectPath as string)
      const nextData = to.path === redirect ? { ...to, replace: true } : { path: redirect }
      permissionStore.setIsAddRouters(true)
      next(nextData)
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`) // 否则全部重定向到登录页
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
  routes: remainingRouter as RouteRecordRaw[]
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
