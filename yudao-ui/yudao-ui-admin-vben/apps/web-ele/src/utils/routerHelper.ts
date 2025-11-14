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
