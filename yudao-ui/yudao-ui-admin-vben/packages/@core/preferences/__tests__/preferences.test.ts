import { beforeEach, describe, expect, it, vi } from 'vitest';

import { defaultPreferences } from '../src/config';
import { PreferenceManager } from '../src/preferences';
import { isDarkTheme } from '../src/update-css-variables';

describe('preferences', () => {
  let preferenceManager: PreferenceManager;

  // 模拟 window.matchMedia 方法
  vi.stubGlobal(
    'matchMedia',
    vi.fn().mockImplementation((query) => ({
      addEventListener: vi.fn(),
      addListener: vi.fn(), // Deprecated
      dispatchEvent: vi.fn(),
      matches: query === '(prefers-color-scheme: dark)',
      media: query,
      onchange: null,
      removeEventListener: vi.fn(),
      removeListener: vi.fn(), // Deprecated
    })),
  );
  beforeEach(() => {
    preferenceManager = new PreferenceManager();
  });

  it('loads default preferences if no saved preferences found', () => {
    const preferences = preferenceManager.getPreferences();
    expect(preferences).toEqual(defaultPreferences);
  });

  it('initializes preferences with overrides', async () => {
    const overrides: any = {
      app: {
        locale: 'en-US',
      },
    };
    await preferenceManager.initPreferences({
      namespace: 'testNamespace',
      overrides,
    });

    // 等待防抖动操作完成
    // await new Promise((resolve) => setTimeout(resolve, 300)); // 等待100毫秒

    const expected = {
      ...defaultPreferences,
      app: {
        ...defaultPreferences.app,
        ...overrides.app,
      },
    };

    expect(preferenceManager.getPreferences()).toEqual(expected);
  });

  it('updates theme mode correctly', () => {
    preferenceManager.updatePreferences({
      theme: {
        mode: 'light',
      },
    });

    expect(preferenceManager.getPreferences().theme.mode).toBe('light');
  });

  it('updates color modes correctly', () => {
    preferenceManager.updatePreferences({
      app: { colorGrayMode: true, colorWeakMode: true },
    });

    expect(preferenceManager.getPreferences().app.colorGrayMode).toBe(true);
    expect(preferenceManager.getPreferences().app.colorWeakMode).toBe(true);
  });

  it('resets preferences to default', () => {
    // 先更新一些偏好设置
    preferenceManager.updatePreferences({
      theme: {
        mode: 'light',
      },
    });

    // 然后重置偏好设置
    preferenceManager.resetPreferences();

    expect(preferenceManager.getPreferences()).toEqual(defaultPreferences);
  });

  it('updates isMobile correctly', () => {
    // 模拟移动端状态
    vi.stubGlobal(
      'matchMedia',
      vi.fn().mockImplementation((query) => ({
        addEventListener: vi.fn(),
        addListener: vi.fn(),
        dispatchEvent: vi.fn(),
        matches: query === '(max-width: 768px)',
        media: query,
        onchange: null,
        removeEventListener: vi.fn(),
        removeListener: vi.fn(),
      })),
    );

    preferenceManager.updatePreferences({
      app: { isMobile: true },
    });

    expect(preferenceManager.getPreferences().app.isMobile).toBe(true);
  });

  it('updates the locale preference correctly', () => {
    preferenceManager.updatePreferences({
      app: { locale: 'en-US' },
    });

    expect(preferenceManager.getPreferences().app.locale).toBe('en-US');
  });

  it('updates the sidebar width correctly', () => {
    preferenceManager.updatePreferences({
      sidebar: { width: 200 },
    });

    expect(preferenceManager.getPreferences().sidebar.width).toBe(200);
  });
  it('updates the sidebar collapse state correctly', () => {
    preferenceManager.updatePreferences({
      sidebar: { collapsed: true },
    });

    expect(preferenceManager.getPreferences().sidebar.collapsed).toBe(true);
  });
  it('updates the navigation style type correctly', () => {
    preferenceManager.updatePreferences({
      navigation: { styleType: 'flat' },
    } as any);

    expect(preferenceManager.getPreferences().navigation.styleType).toBe(
      'flat',
    );
  });

  it('resets preferences to default correctly', () => {
    // 先更新一些偏好设置
    preferenceManager.updatePreferences({
      app: { locale: 'en-US' },
      sidebar: { collapsed: true, width: 200 },
      theme: {
        mode: 'light',
      },
    });

    // 然后重置偏好设置
    preferenceManager.resetPreferences();

    expect(preferenceManager.getPreferences()).toEqual(defaultPreferences);
  });

  it('does not update undefined preferences', () => {
    const originalPreferences = preferenceManager.getPreferences();

    preferenceManager.updatePreferences({
      app: { nonexistentField: 'value' },
    } as any);

    expect(preferenceManager.getPreferences()).toEqual(originalPreferences);
  });

  it('reverts to default when a preference field is deleted', () => {
    preferenceManager.updatePreferences({
      app: { locale: 'en-US' },
    });

    preferenceManager.updatePreferences({
      app: { locale: undefined },
    });

    expect(preferenceManager.getPreferences().app.locale).toBe('en-US');
  });

  it('ignores updates with invalid preference value types', () => {
    const originalPreferences = preferenceManager.getPreferences();

    preferenceManager.updatePreferences({
      app: { isMobile: 'true' as unknown as boolean }, // 错误类型
    });

    expect(preferenceManager.getPreferences()).toEqual(originalPreferences);
  });

  it('merges nested preference objects correctly', () => {
    preferenceManager.updatePreferences({
      app: { name: 'New App Name' },
    });

    const expected = {
      ...defaultPreferences,
      app: {
        ...defaultPreferences.app,
        name: 'New App Name',
      },
    };

    expect(preferenceManager.getPreferences()).toEqual(expected);
  });

  it('applies updates immediately after initialization', async () => {
    const overrides: any = {
      app: {
        locale: 'en-US',
      },
    };

    await preferenceManager.initPreferences(overrides);

    preferenceManager.updatePreferences({
      theme: { mode: 'light' },
    });

    expect(preferenceManager.getPreferences().theme.mode).toBe('light');
  });
});

describe('isDarkTheme', () => {
  it('should return true for dark theme', () => {
    expect(isDarkTheme('dark')).toBe(true);
  });

  it('should return false for light theme', () => {
    expect(isDarkTheme('light')).toBe(false);
  });

  it('should return system preference for auto theme', () => {
    vi.spyOn(window, 'matchMedia').mockImplementation((query) => ({
      addEventListener: vi.fn(),
      addListener: vi.fn(), // Deprecated
      dispatchEvent: vi.fn(),
      matches: query === '(prefers-color-scheme: dark)',
      media: query,
      onchange: null,
      removeEventListener: vi.fn(),
      removeListener: vi.fn(), // Deprecated
    }));

    expect(isDarkTheme('auto')).toBe(true);
    expect(window.matchMedia).toHaveBeenCalledWith(
      '(prefers-color-scheme: dark)',
    );
  });
});
