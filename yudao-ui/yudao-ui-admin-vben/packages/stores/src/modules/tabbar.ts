import type { ComputedRef } from 'vue';
import type {
  RouteLocationNormalized,
  Router,
  RouteRecordNormalized,
} from 'vue-router';

import type { TabDefinition } from '@vben-core/typings';

import { toRaw } from 'vue';

import { preferences } from '@vben-core/preferences';
import {
  openRouteInNewWindow,
  startProgress,
  stopProgress,
} from '@vben-core/shared/utils';

import { acceptHMRUpdate, defineStore } from 'pinia';

interface TabbarState {
  /**
   * @zh_CN 当前打开的标签页列表缓存
   */
  cachedTabs: Set<string>;
  /**
   * @zh_CN 拖拽结束的索引
   */
  dragEndIndex: number;
  /**
   * @zh_CN 需要排除缓存的标签页
   */
  excludeCachedTabs: Set<string>;
  /**
   * @zh_CN 标签右键菜单列表
   */
  menuList: string[];
  /**
   * @zh_CN 是否刷新
   */
  renderRouteView?: boolean;
  /**
   * @zh_CN 当前打开的标签页列表
   */
  tabs: TabDefinition[];
  /**
   * @zh_CN 更新时间，用于一些更新场景，使用watch深度监听的话，会损耗性能
   */
  updateTime?: number;
}

/**
 * @zh_CN 访问权限相关
 */
export const useTabbarStore = defineStore('core-tabbar', {
  actions: {
    /**
     * Close tabs in bulk
     */
    async _bulkCloseByKeys(keys: string[]) {
      const keySet = new Set(keys);
      this.tabs = this.tabs.filter(
        (item) => !keySet.has(getTabKeyFromTab(item)),
      );

      await this.updateCacheTabs();
    },
    /**
     * @zh_CN 关闭标签页
     * @param tab
     */
    _close(tab: TabDefinition) {
      if (isAffixTab(tab)) {
        return;
      }
      const index = this.tabs.findIndex((item) => equalTab(item, tab));
      index !== -1 && this.tabs.splice(index, 1);
    },
    /**
     * @zh_CN 跳转到默认标签页
     */
    async _goToDefaultTab(router: Router) {
      if (this.getTabs.length <= 0) {
        return;
      }
      const firstTab = this.getTabs[0];
      if (firstTab) {
        await this._goToTab(firstTab, router);
      }
    },
    /**
     * @zh_CN 跳转到标签页
     * @param tab
     * @param router
     */
    async _goToTab(tab: TabDefinition, router: Router) {
      const { params, path, query } = tab;
      const toParams = {
        params: params || {},
        path,
        query: query || {},
      };
      await router.replace(toParams);
    },
    /**
     * @zh_CN 添加标签页
     * @param routeTab
     */
    addTab(routeTab: TabDefinition): TabDefinition {
      let tab = cloneTab(routeTab);
      if (!tab.key) {
        tab.key = getTabKey(routeTab);
      }
      if (!isTabShown(tab)) {
        return tab;
      }

      const tabIndex = this.tabs.findIndex((item) => {
        return equalTab(item, tab);
      });

      if (tabIndex === -1) {
        const maxCount = preferences.tabbar.maxCount;
        // 获取动态路由打开数，超过 0 即代表需要控制打开数
        const maxNumOfOpenTab = (routeTab?.meta?.maxNumOfOpenTab ??
          -1) as number;
        // 如果动态路由层级大于 0 了，那么就要限制该路由的打开数限制了
        // 获取到已经打开的动态路由数, 判断是否大于某一个值
        if (
          maxNumOfOpenTab > 0 &&
          this.tabs.filter((tab) => tab.name === routeTab.name).length >=
            maxNumOfOpenTab
        ) {
          // 关闭第一个
          const index = this.tabs.findIndex(
            (item) => item.name === routeTab.name,
          );
          index !== -1 && this.tabs.splice(index, 1);
        } else if (maxCount > 0 && this.tabs.length >= maxCount) {
          // 关闭第一个
          const index = this.tabs.findIndex(
            (item) =>
              !Reflect.has(item.meta, 'affixTab') || !item.meta.affixTab,
          );
          index !== -1 && this.tabs.splice(index, 1);
        }
        this.tabs.push(tab);
      } else {
        // 页面已经存在，不重复添加选项卡，只更新选项卡参数
        const currentTab = toRaw(this.tabs)[tabIndex];
        const mergedTab = {
          ...currentTab,
          ...tab,
          meta: { ...currentTab?.meta, ...tab.meta },
        };
        if (currentTab) {
          const curMeta = currentTab.meta;
          if (Reflect.has(curMeta, 'affixTab')) {
            mergedTab.meta.affixTab = curMeta.affixTab;
          }
          if (Reflect.has(curMeta, 'newTabTitle')) {
            mergedTab.meta.newTabTitle = curMeta.newTabTitle;
          }
        }
        tab = mergedTab;
        this.tabs.splice(tabIndex, 1, mergedTab);
      }
      this.updateCacheTabs();
      return tab;
    },
    /**
     * @zh_CN 关闭所有标签页
     */
    async closeAllTabs(router: Router) {
      const newTabs = this.tabs.filter((tab) => isAffixTab(tab));
      this.tabs = newTabs.length > 0 ? newTabs : [...this.tabs].splice(0, 1);
      await this._goToDefaultTab(router);
      this.updateCacheTabs();
    },
    /**
     * @zh_CN 关闭左侧标签页
     * @param tab
     */
    async closeLeftTabs(tab: TabDefinition) {
      const index = this.tabs.findIndex((item) => equalTab(item, tab));

      if (index < 1) {
        return;
      }

      const leftTabs = this.tabs.slice(0, index);
      const keys: string[] = [];

      for (const item of leftTabs) {
        if (!isAffixTab(item)) {
          keys.push(item.key as string);
        }
      }
      await this._bulkCloseByKeys(keys);
    },
    /**
     * @zh_CN 关闭其他标签页
     * @param tab
     */
    async closeOtherTabs(tab: TabDefinition) {
      const closeKeys = this.tabs.map((item) => getTabKeyFromTab(item));

      const keys: string[] = [];

      for (const key of closeKeys) {
        if (key !== getTabKeyFromTab(tab)) {
          const closeTab = this.tabs.find(
            (item) => getTabKeyFromTab(item) === key,
          );
          if (!closeTab) {
            continue;
          }
          if (!isAffixTab(closeTab)) {
            keys.push(closeTab.key as string);
          }
        }
      }
      await this._bulkCloseByKeys(keys);
    },
    /**
     * @zh_CN 关闭右侧标签页
     * @param tab
     */
    async closeRightTabs(tab: TabDefinition) {
      const index = this.tabs.findIndex((item) => equalTab(item, tab));

      if (index !== -1 && index < this.tabs.length - 1) {
        const rightTabs = this.tabs.slice(index + 1);

        const keys: string[] = [];
        for (const item of rightTabs) {
          if (!isAffixTab(item)) {
            keys.push(item.key as string);
          }
        }
        await this._bulkCloseByKeys(keys);
      }
    },

    /**
     * @zh_CN 关闭标签页
     * @param tab
     * @param router
     */
    async closeTab(tab: TabDefinition, router: Router) {
      const { currentRoute } = router;
      // 关闭不是激活选项卡
      if (getTabKey(currentRoute.value) !== getTabKeyFromTab(tab)) {
        this._close(tab);
        this.updateCacheTabs();
        return;
      }
      const index = this.getTabs.findIndex(
        (item) => getTabKeyFromTab(item) === getTabKey(currentRoute.value),
      );

      const before = this.getTabs[index - 1];
      const after = this.getTabs[index + 1];

      // 下一个tab存在，跳转到下一个
      if (after) {
        this._close(tab);
        await this._goToTab(after, router);
        // 上一个tab存在，跳转到上一个
      } else if (before) {
        this._close(tab);
        await this._goToTab(before, router);
      } else {
        console.error('Failed to close the tab; only one tab remains open.');
      }
    },

    /**
     * @zh_CN 通过key关闭标签页
     * @param key
     * @param router
     */
    async closeTabByKey(key: string, router: Router) {
      const originKey = decodeURIComponent(key);
      const index = this.tabs.findIndex(
        (item) => getTabKeyFromTab(item) === originKey,
      );
      if (index === -1) {
        return;
      }

      const tab = this.tabs[index];
      if (tab) {
        await this.closeTab(tab, router);
      }
    },

    /**
     * 根据tab的key获取tab
     * @param key
     */
    getTabByKey(key: string) {
      return this.getTabs.find(
        (item) => getTabKeyFromTab(item) === key,
      ) as TabDefinition;
    },
    /**
     * @zh_CN 新窗口打开标签页
     * @param tab
     */
    async openTabInNewWindow(tab: TabDefinition) {
      openRouteInNewWindow(tab.fullPath || tab.path);
    },

    /**
     * @zh_CN 固定标签页
     * @param tab
     */
    async pinTab(tab: TabDefinition) {
      const index = this.tabs.findIndex((item) => equalTab(item, tab));
      if (index === -1) {
        return;
      }
      const oldTab = this.tabs[index];
      tab.meta.affixTab = true;
      tab.meta.title = oldTab?.meta?.title as string;
      // this.addTab(tab);
      this.tabs.splice(index, 1, tab);
      // 过滤固定tabs，后面更改affixTabOrder的值的话可能会有问题，目前行464排序affixTabs没有设置值
      const affixTabs = this.tabs.filter((tab) => isAffixTab(tab));
      // 获得固定tabs的index
      const newIndex = affixTabs.findIndex((item) => equalTab(item, tab));
      // 交换位置重新排序
      await this.sortTabs(index, newIndex);
    },

    /**
     * 刷新标签页
     */
    async refresh(router: Router | string) {
      // 如果是Router路由，那么就根据当前路由刷新
      // 如果是string字符串，为路由名称，则定向刷新指定标签页，不能是当前路由名称，否则不会刷新
      if (typeof router === 'string') {
        return await this.refreshByName(router);
      }

      const { currentRoute } = router;
      const { name } = currentRoute.value;

      this.excludeCachedTabs.add(name as string);
      this.renderRouteView = false;
      startProgress();

      await new Promise((resolve) => setTimeout(resolve, 200));

      this.excludeCachedTabs.delete(name as string);
      this.renderRouteView = true;
      stopProgress();
    },

    /**
     * 根据路由名称刷新指定标签页
     */
    async refreshByName(name: string) {
      this.excludeCachedTabs.add(name);
      await new Promise((resolve) => setTimeout(resolve, 200));
      this.excludeCachedTabs.delete(name);
    },

    /**
     * @zh_CN 重置标签页标题
     */
    async resetTabTitle(tab: TabDefinition) {
      if (tab?.meta?.newTabTitle) {
        return;
      }
      const findTab = this.tabs.find((item) => equalTab(item, tab));
      if (findTab) {
        findTab.meta.newTabTitle = undefined;
        await this.updateCacheTabs();
      }
    },

    /**
     * 设置固定标签页
     * @param tabs
     */
    setAffixTabs(tabs: RouteRecordNormalized[]) {
      for (const tab of tabs) {
        tab.meta.affixTab = true;
        this.addTab(routeToTab(tab));
      }
    },

    /**
     * @zh_CN 更新菜单列表
     * @param list
     */
    setMenuList(list: string[]) {
      this.menuList = list;
    },

    /**
     * @zh_CN 设置标签页标题
     *
     * @zh_CN 支持设置静态标题字符串或计算属性作为动态标题
     * @zh_CN 当标题为计算属性时,标题会随计算属性值变化而自动更新
     * @zh_CN 适用于需要根据状态或多语言动态更新标题的场景
     *
     * @param {TabDefinition} tab - 标签页对象
     * @param {ComputedRef<string> | string} title - 标题内容,支持静态字符串或计算属性
     *
     * @example
     * // 设置静态标题
     * setTabTitle(tab, '新标签页');
     *
     * @example
     * // 设置动态标题
     * setTabTitle(tab, computed(() => t('common.dashboard')));
     */
    async setTabTitle(tab: TabDefinition, title: ComputedRef<string> | string) {
      const findTab = this.tabs.find((item) => equalTab(item, tab));

      if (findTab) {
        findTab.meta.newTabTitle = title;

        await this.updateCacheTabs();
      }
    },
    setUpdateTime() {
      this.updateTime = Date.now();
    },
    /**
     * @zh_CN 设置标签页顺序
     * @param oldIndex
     * @param newIndex
     */
    async sortTabs(oldIndex: number, newIndex: number) {
      const currentTab = this.tabs[oldIndex];
      if (!currentTab) {
        return;
      }
      this.tabs.splice(oldIndex, 1);
      this.tabs.splice(newIndex, 0, currentTab);
      this.dragEndIndex = this.dragEndIndex + 1;
    },

    /**
     * @zh_CN 切换固定标签页
     * @param tab
     */
    async toggleTabPin(tab: TabDefinition) {
      const affixTab = tab?.meta?.affixTab ?? false;

      await (affixTab ? this.unpinTab(tab) : this.pinTab(tab));
    },

    /**
     * @zh_CN 取消固定标签页
     * @param tab
     */
    async unpinTab(tab: TabDefinition) {
      const index = this.tabs.findIndex((item) => equalTab(item, tab));
      if (index === -1) {
        return;
      }
      const oldTab = this.tabs[index];
      tab.meta.affixTab = false;
      tab.meta.title = oldTab?.meta?.title as string;
      // this.addTab(tab);
      this.tabs.splice(index, 1, tab);
      // 过滤固定tabs，后面更改affixTabOrder的值的话可能会有问题，目前行464排序affixTabs没有设置值
      const affixTabs = this.tabs.filter((tab) => isAffixTab(tab));
      // 获得固定tabs的index,使用固定tabs的下一个位置也就是活动tabs的第一个位置
      const newIndex = affixTabs.length;
      // 交换位置重新排序
      await this.sortTabs(index, newIndex);
    },
    /**
     * 根据当前打开的选项卡更新缓存
     */
    async updateCacheTabs() {
      const cacheMap = new Set<string>();

      for (const tab of this.tabs) {
        // 跳过不需要持久化的标签页
        const keepAlive = tab.meta?.keepAlive;
        if (!keepAlive) {
          continue;
        }
        (tab.matched || []).forEach((t, i) => {
          if (i > 0) {
            cacheMap.add(t.name as string);
          }
        });

        const name = tab.name as string;
        cacheMap.add(name);
      }
      this.cachedTabs = cacheMap;
    },
  },
  getters: {
    affixTabs(): TabDefinition[] {
      const affixTabs = this.tabs.filter((tab) => isAffixTab(tab));

      return affixTabs.toSorted((a, b) => {
        const orderA = (a.meta?.affixTabOrder ?? 0) as number;
        const orderB = (b.meta?.affixTabOrder ?? 0) as number;
        return orderA - orderB;
      });
    },
    getCachedTabs(): string[] {
      return [...this.cachedTabs];
    },
    getExcludeCachedTabs(): string[] {
      return [...this.excludeCachedTabs];
    },
    getMenuList(): string[] {
      return this.menuList;
    },
    getTabs(): TabDefinition[] {
      const normalTabs = this.tabs.filter((tab) => !isAffixTab(tab));
      return [...this.affixTabs, ...normalTabs].filter(Boolean);
    },
  },
  persist: [
    // tabs不需要保存在localStorage
    {
      pick: ['tabs'],
      storage: sessionStorage,
    },
  ],
  state: (): TabbarState => ({
    cachedTabs: new Set(),
    dragEndIndex: 0,
    excludeCachedTabs: new Set(),
    menuList: [
      'close',
      'affix',
      'maximize',
      'reload',
      'open-in-new-window',
      'close-left',
      'close-right',
      'close-other',
      'close-all',
    ],
    renderRouteView: true,
    tabs: [],
    updateTime: Date.now(),
  }),
});

// 解决热更新问题
const hot = import.meta.hot;
if (hot) {
  hot.accept(acceptHMRUpdate(useTabbarStore, hot));
}

/**
 * @zh_CN 克隆路由,防止路由被修改
 * @param route
 */
function cloneTab(route: TabDefinition): TabDefinition {
  if (!route) {
    return route;
  }
  const { matched, meta, ...opt } = route;
  return {
    ...opt,
    matched: (matched
      ? matched.map((item) => ({
          meta: item.meta,
          name: item.name,
          path: item.path,
        }))
      : undefined) as RouteRecordNormalized[],
    meta: {
      ...meta,
      newTabTitle: meta.newTabTitle,
    },
  };
}

/**
 * @zh_CN 是否是固定标签页
 * @param tab
 */
function isAffixTab(tab: TabDefinition) {
  return tab?.meta?.affixTab ?? false;
}

/**
 * @zh_CN 是否显示标签
 * @param tab
 */
function isTabShown(tab: TabDefinition) {
  const matched = tab?.matched ?? [];
  return !tab.meta.hideInTab && matched.every((item) => !item.meta.hideInTab);
}

/**
 * 从route获取tab页的key
 * @param tab
 */
function getTabKey(tab: RouteLocationNormalized | RouteRecordNormalized) {
  const {
    fullPath,
    path,
    meta: { fullPathKey } = {},
    query = {},
  } = tab as RouteLocationNormalized;
  // pageKey可能是数组（查询参数重复时可能出现）
  const pageKey = Array.isArray(query.pageKey)
    ? query.pageKey[0]
    : query.pageKey;
  let rawKey;
  if (pageKey) {
    rawKey = pageKey;
  } else {
    rawKey = fullPathKey === false ? path : (fullPath ?? path);
  }
  try {
    return decodeURIComponent(rawKey);
  } catch {
    return rawKey;
  }
}

/**
 * 从tab获取tab页的key
 * 如果tab没有key,那么就从route获取key
 * @param tab
 */
function getTabKeyFromTab(tab: TabDefinition): string {
  return tab.key ?? getTabKey(tab);
}

/**
 * 比较两个tab是否相等
 * @param a
 * @param b
 */
function equalTab(a: TabDefinition, b: TabDefinition) {
  return getTabKeyFromTab(a) === getTabKeyFromTab(b);
}

function routeToTab(route: RouteRecordNormalized) {
  return {
    meta: route.meta,
    name: route.name,
    path: route.path,
    key: getTabKey(route),
  } as TabDefinition;
}

export { getTabKey };
