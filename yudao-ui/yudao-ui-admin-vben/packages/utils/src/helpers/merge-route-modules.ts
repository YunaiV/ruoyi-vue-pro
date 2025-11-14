import type { RouteRecordRaw } from 'vue-router';

// 定义模块类型
interface RouteModuleType {
  default: RouteRecordRaw[];
}

/**
 * 合并动态路由模块的默认导出
 * @param routeModules 动态导入的路由模块对象
 * @returns 合并后的路由配置数组
 */
function mergeRouteModules(
  routeModules: Record<string, unknown>,
): RouteRecordRaw[] {
  const mergedRoutes: RouteRecordRaw[] = [];

  for (const routeModule of Object.values(routeModules)) {
    const moduleRoutes = (routeModule as RouteModuleType)?.default ?? [];
    mergedRoutes.push(...moduleRoutes);
  }

  return mergedRoutes;
}

export { mergeRouteModules };

export type { RouteModuleType };
