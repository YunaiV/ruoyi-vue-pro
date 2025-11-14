import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { openWindow } from '../window';

describe('openWindow', () => {
  // 保存原始的 window.open 函数
  let originalOpen: typeof window.open;

  beforeEach(() => {
    originalOpen = window.open;
  });

  afterEach(() => {
    window.open = originalOpen;
  });

  it('should call window.open with correct arguments', () => {
    const url = 'https://example.com';
    const options = { noopener: true, noreferrer: true, target: '_blank' };

    window.open = vi.fn();

    // 调用函数
    openWindow(url, options);

    // 验证 window.open 是否被正确地调用
    expect(window.open).toHaveBeenCalledWith(
      url,
      options.target,
      'noopener=yes,noreferrer=yes',
    );
  });
});
