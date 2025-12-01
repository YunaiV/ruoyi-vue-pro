import type {
  RouteLocationNormalized,
  RouteRecordNormalized,
} from 'vue-router';

import { defineAsyncComponent } from 'vue';

const modules = import.meta.glob('../views/**/*.{vue,tsx}');

/**
 * 注册一个异步组件
 * @param componentPath 例:/bpm/oa/leave/detail
 */
export function registerComponent(componentPath: string) {
  for (const item in modules) {
    if (item.includes(componentPath)) {
      // 使用异步组件的方式来动态加载组件
      return defineAsyncComponent(modules[item] as any);
    }
  }
}

export const getRawRoute = (
  route: RouteLocationNormalized,
): RouteLocationNormalized => {
  if (!route) return route;
  const { matched, ...opt } = route;
  return {
    ...opt,
    matched: (matched
      ? matched.map((item) => ({
          meta: item.meta,
          name: item.name,
          path: item.path,
        }))
      : undefined) as RouteRecordNormalized[],
  };
};
