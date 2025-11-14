import type { ComputedRef } from 'vue';
import type { RouteLocationNormalized } from 'vue-router';

import { useRoute, useRouter } from 'vue-router';

import { useTabbarStore } from '@vben/stores';

export function useTabs() {
  const router = useRouter();
  const route = useRoute();
  const tabbarStore = useTabbarStore();

  async function closeLeftTabs(tab?: RouteLocationNormalized) {
    await tabbarStore.closeLeftTabs(tab || route);
  }

  async function closeAllTabs() {
    await tabbarStore.closeAllTabs(router);
  }

  async function closeRightTabs(tab?: RouteLocationNormalized) {
    await tabbarStore.closeRightTabs(tab || route);
  }

  async function closeOtherTabs(tab?: RouteLocationNormalized) {
    await tabbarStore.closeOtherTabs(tab || route);
  }

  async function closeCurrentTab(tab?: RouteLocationNormalized) {
    await tabbarStore.closeTab(tab || route, router);
  }

  async function pinTab(tab?: RouteLocationNormalized) {
    await tabbarStore.pinTab(tab || route);
  }

  async function unpinTab(tab?: RouteLocationNormalized) {
    await tabbarStore.unpinTab(tab || route);
  }

  async function toggleTabPin(tab?: RouteLocationNormalized) {
    await tabbarStore.toggleTabPin(tab || route);
  }

  async function refreshTab(name?: string) {
    await tabbarStore.refresh(name || router);
  }

  async function openTabInNewWindow(tab?: RouteLocationNormalized) {
    await tabbarStore.openTabInNewWindow(tab || route);
  }

  async function closeTabByKey(key: string) {
    await tabbarStore.closeTabByKey(key, router);
  }

  /**
   * 设置当前标签页的标题
   *
   * @description 支持设置静态标题字符串或动态计算标题
   * @description 动态标题会在每次渲染时重新计算,适用于多语言或状态相关的标题
   *
   * @param title - 标题内容
   *   - 静态标题: 直接传入字符串
   *   - 动态标题: 传入 ComputedRef
   *
   * @example
   * // 静态标题
   * setTabTitle('标签页')
   *
   * // 动态标题(多语言)
   * setTabTitle(computed(() => t('page.title')))
   */
  async function setTabTitle(title: ComputedRef<string> | string) {
    tabbarStore.setUpdateTime();
    await tabbarStore.setTabTitle(route, title);
  }

  async function resetTabTitle() {
    tabbarStore.setUpdateTime();
    await tabbarStore.resetTabTitle(route);
  }

  /**
   * 获取操作是否禁用
   * @param tab
   */
  function getTabDisableState(tab: RouteLocationNormalized = route) {
    const tabs = tabbarStore.getTabs;
    const affixTabs = tabbarStore.affixTabs;
    const index = tabs.findIndex((item) => item.path === tab.path);

    const disabled = tabs.length <= 1;

    const { meta } = tab;
    const affixTab = meta?.affixTab ?? false;
    const isCurrentTab = route.path === tab.path;

    // 当前处于最左侧或者减去固定标签页的数量等于0
    const disabledCloseLeft =
      index === 0 || index - affixTabs.length <= 0 || !isCurrentTab;

    const disabledCloseRight = !isCurrentTab || index === tabs.length - 1;

    const disabledCloseOther =
      disabled || !isCurrentTab || tabs.length - affixTabs.length <= 1;
    return {
      disabledCloseAll: disabled,
      disabledCloseCurrent: !!affixTab || disabled,
      disabledCloseLeft,
      disabledCloseOther,
      disabledCloseRight,
      disabledRefresh: !isCurrentTab,
    };
  }

  return {
    closeAllTabs,
    closeCurrentTab,
    closeLeftTabs,
    closeOtherTabs,
    closeRightTabs,
    closeTabByKey,
    getTabDisableState,
    openTabInNewWindow,
    pinTab,
    refreshTab,
    resetTabTitle,
    setTabTitle,
    toggleTabPin,
    unpinTab,
  };
}
