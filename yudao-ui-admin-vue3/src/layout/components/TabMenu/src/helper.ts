import { getAllParentPath } from '@/layout/components/Menu/src/helper'
import type { RouteMeta } from 'vue-router'
import { isUrl } from '@/utils/is'
import { cloneDeep } from 'lodash-es'

export type TabMapTypes = {
  [key: string]: string[]
}

export const tabPathMap = reactive<TabMapTypes>({})

export const initTabMap = (routes: AppRouteRecordRaw[]) => {
  for (const v of routes) {
    const meta = (v.meta ?? {}) as RouteMeta
    if (!meta?.hidden) {
      tabPathMap[v.path] = []
    }
  }
}

export const filterMenusPath = (
  routes: AppRouteRecordRaw[],
  allRoutes: AppRouteRecordRaw[]
): AppRouteRecordRaw[] => {
  const res: AppRouteRecordRaw[] = []
  for (const v of routes) {
    let data: Nullable<AppRouteRecordRaw> = null
    const meta = (v.meta ?? {}) as RouteMeta
    if (!meta.hidden || meta.canTo) {
      const allParentPath = getAllParentPath<AppRouteRecordRaw>(allRoutes, v.path)

      const fullPath = isUrl(v.path) ? v.path : allParentPath.join('/')

      data = cloneDeep(v)
      data.path = fullPath
      if (v.children && data) {
        data.children = filterMenusPath(v.children, allRoutes)
      }

      if (data) {
        res.push(data)
      }

      if (allParentPath.length && Reflect.has(tabPathMap, allParentPath[0])) {
        tabPathMap[allParentPath[0]].push(fullPath)
      }
    }
  }

  return res
}
