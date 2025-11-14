import { beforeEach, describe, expect, it, vi } from 'vitest';

import { getElementVisibleRect } from '../dom';

describe('getElementVisibleRect', () => {
  // 设置浏览器视口尺寸的 mock
  beforeEach(() => {
    vi.spyOn(document.documentElement, 'clientHeight', 'get').mockReturnValue(
      800,
    );
    vi.spyOn(window, 'innerHeight', 'get').mockReturnValue(800);
    vi.spyOn(document.documentElement, 'clientWidth', 'get').mockReturnValue(
      1000,
    );
    vi.spyOn(window, 'innerWidth', 'get').mockReturnValue(1000);
  });

  it('should return default rect if element is undefined', () => {
    expect(getElementVisibleRect()).toEqual({
      bottom: 0,
      height: 0,
      left: 0,
      right: 0,
      top: 0,
      width: 0,
    });
  });

  it('should return default rect if element is null', () => {
    expect(getElementVisibleRect(null)).toEqual({
      bottom: 0,
      height: 0,
      left: 0,
      right: 0,
      top: 0,
      width: 0,
    });
  });

  it('should return correct visible rect when element is fully visible', () => {
    const element = {
      getBoundingClientRect: () => ({
        bottom: 400,
        height: 300,
        left: 200,
        right: 600,
        top: 100,
        width: 400,
      }),
    } as HTMLElement;

    expect(getElementVisibleRect(element)).toEqual({
      bottom: 400,
      height: 300,
      left: 200,
      right: 600,
      top: 100,
      width: 400,
    });
  });

  it('should return correct visible rect when element is partially off-screen at the top', () => {
    const element = {
      getBoundingClientRect: () => ({
        bottom: 200,
        height: 250,
        left: 100,
        right: 500,
        top: -50,
        width: 400,
      }),
    } as HTMLElement;

    expect(getElementVisibleRect(element)).toEqual({
      bottom: 200,
      height: 200,
      left: 100,
      right: 500,
      top: 0,
      width: 400,
    });
  });

  it('should return correct visible rect when element is partially off-screen at the right', () => {
    const element = {
      getBoundingClientRect: () => ({
        bottom: 400,
        height: 300,
        left: 800,
        right: 1200,
        top: 100,
        width: 400,
      }),
    } as HTMLElement;

    expect(getElementVisibleRect(element)).toEqual({
      bottom: 400,
      height: 300,
      left: 800,
      right: 1000,
      top: 100,
      width: 200,
    });
  });

  it('should return all zeros when element is completely off-screen', () => {
    const element = {
      getBoundingClientRect: () => ({
        bottom: 1200,
        height: 300,
        left: 1100,
        right: 1400,
        top: 900,
        width: 300,
      }),
    } as HTMLElement;

    expect(getElementVisibleRect(element)).toEqual({
      bottom: 800,
      height: 0,
      left: 1100,
      right: 1000,
      top: 900,
      width: 0,
    });
  });
});
