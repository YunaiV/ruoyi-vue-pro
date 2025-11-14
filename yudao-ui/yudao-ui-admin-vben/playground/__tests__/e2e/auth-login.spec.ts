import { expect, test } from '@playwright/test';

import { authLogin } from './common/auth';

test.beforeEach(async ({ page }) => {
  await page.goto('/');
});

test.describe('Auth Login Page Tests', () => {
  test('check title and page elements', async ({ page }) => {
    // 获取页面标题并断言标题包含 'Vben Admin'
    const title = await page.title();
    expect(title).toContain('Vben Admin');
  });

  // 测试用例: 成功登录
  test('should successfully login with valid credentials', async ({ page }) => {
    await authLogin(page);
  });
});
