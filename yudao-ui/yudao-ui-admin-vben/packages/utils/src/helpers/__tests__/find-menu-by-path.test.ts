import { describe, expect, it } from 'vitest';

import { findMenuByPath, findRootMenuByPath } from '../find-menu-by-path';

// 示例菜单数据
const menus: any[] = [
  { path: '/', children: [] },
  { path: '/about', children: [] },
  {
    path: '/contact',
    children: [
      { path: '/contact/email', children: [] },
      { path: '/contact/phone', children: [] },
    ],
  },
  {
    path: '/services',
    children: [
      { path: '/services/design', children: [] },
      {
        path: '/services/development',
        children: [{ path: '/services/development/web', children: [] }],
      },
    ],
  },
];

describe('menu Finder Tests', () => {
  it('finds a top-level menu', () => {
    const menu = findMenuByPath(menus, '/about');
    expect(menu).toBeDefined();
    expect(menu?.path).toBe('/about');
  });

  it('finds a nested menu', () => {
    const menu = findMenuByPath(menus, '/services/development/web');
    expect(menu).toBeDefined();
    expect(menu?.path).toBe('/services/development/web');
  });

  it('returns null for a non-existent path', () => {
    const menu = findMenuByPath(menus, '/non-existent');
    expect(menu).toBeNull();
  });

  it('handles empty menus list', () => {
    const menu = findMenuByPath([], '/about');
    expect(menu).toBeNull();
  });

  it('handles menu items without children', () => {
    const menu = findMenuByPath(
      [{ path: '/only', children: undefined }] as any[],
      '/only',
    );
    expect(menu).toBeDefined();
    expect(menu?.path).toBe('/only');
  });

  it('finds root menu by path', () => {
    const { findMenu, rootMenu, rootMenuPath } = findRootMenuByPath(
      menus,
      '/services/development/web',
    );

    expect(findMenu).toBeDefined();
    expect(rootMenu).toBeUndefined();
    expect(rootMenuPath).toBeUndefined();
    expect(findMenu?.path).toBe('/services/development/web');
  });

  it('returns null for undefined or empty path', () => {
    const menuUndefinedPath = findMenuByPath(menus);
    const menuEmptyPath = findMenuByPath(menus, '');
    expect(menuUndefinedPath).toBeNull();
    expect(menuEmptyPath).toBeNull();
  });

  it('checks for root menu when path does not exist', () => {
    const { findMenu, rootMenu, rootMenuPath } = findRootMenuByPath(
      menus,
      '/non-existent',
    );
    expect(findMenu).toBeNull();
    expect(rootMenu).toBeUndefined();
    expect(rootMenuPath).toBeUndefined();
  });
});
