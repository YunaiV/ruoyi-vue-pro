import type { DrawerState } from '../drawer';

import { beforeEach, describe, expect, it, vi } from 'vitest';

import { DrawerApi } from '../drawer-api';

// 模拟 Store 类
vi.mock('@vben-core/shared/store', () => {
  return {
    isFunction: (fn: any) => typeof fn === 'function',
    Store: class {
      get state() {
        return this._state;
      }
      private _state: DrawerState;

      private options: any;

      constructor(initialState: DrawerState, options: any) {
        this._state = initialState;
        this.options = options;
      }

      batch(cb: () => void) {
        cb();
      }

      setState(fn: (prev: DrawerState) => DrawerState) {
        this._state = fn(this._state);
        this.options.onUpdate();
      }
    },
  };
});

describe('drawerApi', () => {
  let drawerApi: DrawerApi;
  let drawerState: DrawerState;

  beforeEach(() => {
    drawerApi = new DrawerApi();
    drawerState = drawerApi.store.state;
  });

  it('should initialize with default state', () => {
    expect(drawerState.isOpen).toBe(false);
    expect(drawerState.cancelText).toBe(undefined);
    expect(drawerState.confirmText).toBe(undefined);
  });

  it('should open the drawer', () => {
    drawerApi.open();
    expect(drawerApi.store.state.isOpen).toBe(true);
  });

  it('should close the drawer if onBeforeClose allows it', () => {
    drawerApi.close();
    expect(drawerApi.store.state.isOpen).toBe(false);
  });

  it('should not close the drawer if onBeforeClose returns false', () => {
    const onBeforeClose = vi.fn(() => false);
    const drawerApiWithHook = new DrawerApi({ onBeforeClose });
    drawerApiWithHook.open();
    drawerApiWithHook.close();
    expect(drawerApiWithHook.store.state.isOpen).toBe(true);
    expect(onBeforeClose).toHaveBeenCalled();
  });

  it('should trigger onCancel and keep drawer open if onCancel is provided', () => {
    const onCancel = vi.fn();
    const drawerApiWithHook = new DrawerApi({ onCancel });
    drawerApiWithHook.open();
    drawerApiWithHook.onCancel();
    expect(onCancel).toHaveBeenCalled();
    expect(drawerApiWithHook.store.state.isOpen).toBe(true); // 关闭逻辑不在 onCancel 内
  });

  it('should update shared data correctly', () => {
    const testData = { key: 'value' };
    drawerApi.setData(testData);
    expect(drawerApi.getData()).toEqual(testData);
  });

  it('should set state correctly using an object', () => {
    drawerApi.setState({ title: 'New Title' });
    expect(drawerApi.store.state.title).toBe('New Title');
  });

  it('should set state correctly using a function', () => {
    drawerApi.setState((prev) => ({ ...prev, confirmText: 'Yes' }));
    expect(drawerApi.store.state.confirmText).toBe('Yes');
  });

  it('should call onOpenChange when state changes', () => {
    const onOpenChange = vi.fn();
    const drawerApiWithHook = new DrawerApi({ onOpenChange });
    drawerApiWithHook.open();
    expect(onOpenChange).toHaveBeenCalledWith(true);
  });

  it('should call onClosed callback when provided', () => {
    const onClosed = vi.fn();
    const drawerApiWithHook = new DrawerApi({ onClosed });
    drawerApiWithHook.onClosed();
    expect(onClosed).toHaveBeenCalled();
  });

  it('should call onOpened callback when provided', () => {
    const onOpened = vi.fn();
    const drawerApiWithHook = new DrawerApi({ onOpened });
    drawerApiWithHook.open();
    drawerApiWithHook.onOpened();
    expect(onOpened).toHaveBeenCalled();
  });
});
