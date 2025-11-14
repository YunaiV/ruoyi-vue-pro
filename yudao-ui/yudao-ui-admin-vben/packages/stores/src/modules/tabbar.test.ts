import { createRouter, createWebHistory } from 'vue-router';

import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { useTabbarStore } from './tabbar';

describe('useAccessStore', () => {
  const router = createRouter({
    history: createWebHistory(),
    routes: [],
  });
  router.push = vi.fn();
  router.replace = vi.fn();
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('adds a new tab', () => {
    const store = useTabbarStore();
    const tab: any = {
      fullPath: '/home',
      meta: {},
      key: '/home',
      name: 'Home',
      path: '/home',
    };
    const addNewTab = store.addTab(tab);
    expect(store.tabs.length).toBe(1);
    expect(store.tabs[0]).toEqual(addNewTab);
  });

  it('adds a new tab if it does not exist', () => {
    const store = useTabbarStore();
    const newTab: any = {
      fullPath: '/new',
      meta: {},
      name: 'New',
      path: '/new',
    };
    const addNewTab = store.addTab(newTab);
    expect(store.tabs).toContainEqual(addNewTab);
  });

  it('updates an existing tab instead of adding a new one', () => {
    const store = useTabbarStore();
    const initialTab: any = {
      fullPath: '/existing',
      meta: {
        fullPathKey: false,
      },
      name: 'Existing',
      path: '/existing',
      query: {},
    };
    store.addTab(initialTab);
    const updatedTab = { ...initialTab, query: { id: '1' } };
    store.addTab(updatedTab);
    expect(store.tabs.length).toBe(1);
    expect(store.tabs[0]?.query).toEqual({ id: '1' });
  });

  it('closes all tabs', async () => {
    const store = useTabbarStore();
    store.addTab({
      fullPath: '/home',
      meta: {},
      name: 'Home',
      path: '/home',
    } as any);
    router.replace = vi.fn();

    await store.closeAllTabs(router);

    expect(store.tabs.length).toBe(1);
  });

  it('closes a non-affix tab', () => {
    const store = useTabbarStore();
    const tab: any = {
      fullPath: '/closable',
      meta: {},
      name: 'Closable',
      path: '/closable',
    };
    store.tabs.push(tab);
    store._close(tab);
    expect(store.tabs.length).toBe(0);
  });

  it('does not close an affix tab', () => {
    const store = useTabbarStore();
    const affixTab: any = {
      fullPath: '/affix',
      meta: { affixTab: true },
      name: 'Affix',
      path: '/affix',
    };
    store.tabs.push(affixTab);
    store._close(affixTab);
    expect(store.tabs.length).toBe(1); // Affix tab should not be closed
  });

  it('returns all cache tabs', () => {
    const store = useTabbarStore();
    store.cachedTabs.add('Home');
    store.cachedTabs.add('About');
    expect(store.getCachedTabs).toEqual(['Home', 'About']);
  });

  it('returns all tabs, including affix tabs', () => {
    const store = useTabbarStore();
    const normalTab: any = {
      fullPath: '/normal',
      meta: {},
      name: 'Normal',
      path: '/normal',
    };
    const affixTab: any = {
      fullPath: '/affix',
      meta: { affixTab: true },
      name: 'Affix',
      path: '/affix',
    };
    store.tabs.push(normalTab);
    store.affixTabs.push(affixTab);
    expect(store.getTabs).toContainEqual(normalTab);
    expect(store.affixTabs).toContainEqual(affixTab);
  });

  it('navigates to a specific tab', async () => {
    const store = useTabbarStore();
    const tab: any = { meta: {}, name: 'Dashboard', path: '/dashboard' };

    await store._goToTab(tab, router);

    expect(router.replace).toHaveBeenCalledWith({
      params: {},
      path: '/dashboard',
      query: {},
    });
  });

  it('closes multiple tabs by paths', async () => {
    const store = useTabbarStore();
    store.addTab({
      fullPath: '/home',
      meta: {},
      name: 'Home',
      path: '/home',
    } as any);
    store.addTab({
      fullPath: '/about',
      meta: {},
      name: 'About',
      path: '/about',
    } as any);
    store.addTab({
      fullPath: '/contact',
      meta: {},
      name: 'Contact',
      path: '/contact',
    } as any);

    await store._bulkCloseByKeys(['/home', '/contact']);

    expect(store.tabs).toHaveLength(1);
    expect(store.tabs[0]?.name).toBe('About');
  });

  it('closes all tabs to the left of the specified tab', async () => {
    const store = useTabbarStore();
    store.addTab({
      fullPath: '/home',
      meta: {},
      name: 'Home',
      path: '/home',
    } as any);
    store.addTab({
      fullPath: '/about',
      meta: {},
      name: 'About',
      path: '/about',
    } as any);
    const targetTab: any = {
      fullPath: '/contact',
      meta: {},
      name: 'Contact',
      path: '/contact',
    };
    const addTargetTab = store.addTab(targetTab);
    await store.closeLeftTabs(addTargetTab);

    expect(store.tabs).toHaveLength(1);
    expect(store.tabs[0]?.name).toBe('Contact');
  });

  it('closes all tabs except the specified tab', async () => {
    const store = useTabbarStore();
    store.addTab({
      fullPath: '/home',
      meta: {},
      name: 'Home',
      path: '/home',
    } as any);
    const targetTab: any = {
      fullPath: '/about',
      meta: {},
      name: 'About',
      path: '/about',
    };
    const addTargetTab = store.addTab(targetTab);
    store.addTab({
      fullPath: '/contact',
      meta: {},
      name: 'Contact',
      path: '/contact',
    } as any);

    await store.closeOtherTabs(addTargetTab);

    expect(store.tabs).toHaveLength(1);
    expect(store.tabs[0]?.name).toBe('About');
  });

  it('closes all tabs to the right of the specified tab', async () => {
    const store = useTabbarStore();
    const targetTab: any = {
      fullPath: '/home',
      meta: {},
      name: 'Home',
      path: '/home',
    };
    const addTargetTab = store.addTab(targetTab);
    store.addTab({
      fullPath: '/about',
      meta: {},
      name: 'About',
      path: '/about',
    } as any);
    store.addTab({
      fullPath: '/contact',
      meta: {},
      name: 'Contact',
      path: '/contact',
    } as any);

    await store.closeRightTabs(addTargetTab);

    expect(store.tabs).toHaveLength(1);
    expect(store.tabs[0]?.name).toBe('Home');
  });

  it('closes the tab with the specified key', async () => {
    const store = useTabbarStore();
    const keyToClose = '/about';
    store.addTab({
      fullPath: '/home',
      meta: {},
      name: 'Home',
      path: '/home',
    } as any);
    store.addTab({
      fullPath: keyToClose,
      meta: {},
      name: 'About',
      path: '/about',
    } as any);
    store.addTab({
      fullPath: '/contact',
      meta: {},
      name: 'Contact',
      path: '/contact',
    } as any);

    await store.closeTabByKey(keyToClose, router);

    expect(store.tabs).toHaveLength(2);
    expect(
      store.tabs.find((tab) => tab.fullPath === keyToClose),
    ).toBeUndefined();
  });

  it('refreshes the current tab', async () => {
    const store = useTabbarStore();
    const currentTab: any = {
      fullPath: '/dashboard',
      meta: { name: 'Dashboard' },
      name: 'Dashboard',
      path: '/dashboard',
    };
    router.currentRoute.value = currentTab;

    await store.refresh(router);

    expect(store.excludeCachedTabs.has('Dashboard')).toBe(false);
    expect(store.renderRouteView).toBe(true);
  });
});
