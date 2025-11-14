import type { Recordable } from '@vben/types';

/**
 * 一个缓存对象，在不刷新页面时，无需重复请求远程接口
 */
export const ICONS_MAP: Recordable<string[]> = {};

interface IconifyResponse {
  prefix: string;
  total: number;
  title: string;
  uncategorized?: string[];
  categories?: Recordable<string[]>;
  aliases?: Recordable<string>;
}

const PENDING_REQUESTS: Recordable<Promise<string[]>> = {};

/**
 * 通过Iconify接口获取图标集数据。
 * 同一时间多个图标选择器同时请求同一个图标集时，实际上只会发起一次请求（所有请求共享同一份结果）。
 * 请求结果会被缓存，刷新页面前同一个图标集不会再次请求
 * @param prefix 图标集名称
 * @returns 图标集中包含的所有图标名称
 */
export async function fetchIconsData(prefix: string): Promise<string[]> {
  if (Reflect.has(ICONS_MAP, prefix) && ICONS_MAP[prefix]) {
    return ICONS_MAP[prefix];
  }
  if (Reflect.has(PENDING_REQUESTS, prefix) && PENDING_REQUESTS[prefix]) {
    return PENDING_REQUESTS[prefix];
  }
  PENDING_REQUESTS[prefix] = (async () => {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 1000 * 10);
      const response: IconifyResponse = await fetch(
        `https://api.iconify.design/collection?prefix=${prefix}`,
        { signal: controller.signal },
      ).then((res) => res.json());
      clearTimeout(timeoutId);
      const list = response.uncategorized || [];
      if (response.categories) {
        for (const category in response.categories) {
          list.push(...(response.categories[category] || []));
        }
      }
      ICONS_MAP[prefix] = list.map((v) => `${prefix}:${v}`);
    } catch (error) {
      console.error(`Failed to fetch icons for prefix ${prefix}:`, error);
      return [] as string[];
    }
    return ICONS_MAP[prefix];
  })();
  return PENDING_REQUESTS[prefix];
}
