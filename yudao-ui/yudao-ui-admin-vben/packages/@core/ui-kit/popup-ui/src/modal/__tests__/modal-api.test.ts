import type { ModalState } from '../modal';

import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ModalApi } from '../modal-api';

vi.mock('@vben-core/shared/store', () => {
  return {
    isFunction: (fn: any) => typeof fn === 'function',
    Store: class {
      get state() {
        return this._state;
      }
      private _state: ModalState;

      private options: any;

      constructor(initialState: ModalState, options: any) {
        this._state = initialState;
        this.options = options;
      }

      batch(cb: () => void) {
        cb();
      }

      setState(fn: (prev: ModalState) => ModalState) {
        this._state = fn(this._state);
        this.options.onUpdate();
      }
    },
  };
});

describe('modalApi', () => {
  let modalApi: ModalApi;
  // 使用 modalState 而不是 state
  let modalState: ModalState;

  beforeEach(() => {
    modalApi = new ModalApi();
    // 获取 modalApi 内的 state
    modalState = modalApi.store.state;
  });

  it('should initialize with default state', () => {
    expect(modalState.isOpen).toBe(false);
    expect(modalState.cancelText).toBe(undefined);
    expect(modalState.confirmText).toBe(undefined);
  });

  it('should open the modal', () => {
    modalApi.open();
    expect(modalApi.store.state.isOpen).toBe(true);
  });

  it('should close the modal if onBeforeClose allows it', () => {
    modalApi.close();
    expect(modalApi.store.state.isOpen).toBe(false);
  });

  it('should not close the modal if onBeforeClose returns false', () => {
    const onBeforeClose = vi.fn(() => false);
    const modalApiWithHook = new ModalApi({ onBeforeClose });
    modalApiWithHook.open();
    modalApiWithHook.close();
    expect(modalApiWithHook.store.state.isOpen).toBe(true);
    expect(onBeforeClose).toHaveBeenCalled();
  });

  it('should trigger onCancel and close the modal if no onCancel hook is provided', () => {
    const onCancel = vi.fn();
    const modalApiWithHook = new ModalApi({ onCancel });
    modalApiWithHook.open();
    modalApiWithHook.onCancel();
    expect(onCancel).toHaveBeenCalled();
    expect(modalApiWithHook.store.state.isOpen).toBe(true);
  });

  it('should update shared data correctly', () => {
    const testData = { key: 'value' };
    modalApi.setData(testData);
    expect(modalApi.getData()).toEqual(testData);
  });

  it('should set state correctly using an object', () => {
    modalApi.setState({ title: 'New Title' });
    expect(modalApi.store.state.title).toBe('New Title');
  });

  it('should set state correctly using a function', () => {
    modalApi.setState((prev) => ({ ...prev, confirmText: 'Yes' }));
    expect(modalApi.store.state.confirmText).toBe('Yes');
  });

  it('should call onOpenChange when state changes', () => {
    const onOpenChange = vi.fn();
    const modalApiWithHook = new ModalApi({ onOpenChange });
    modalApiWithHook.open();
    expect(onOpenChange).toHaveBeenCalledWith(true);
  });

  it('should call onClosed callback when provided', () => {
    const onClosed = vi.fn();
    const modalApiWithHook = new ModalApi({ onClosed });
    modalApiWithHook.onClosed();
    expect(onClosed).toHaveBeenCalled();
  });

  it('should call onOpened callback when provided', () => {
    const onOpened = vi.fn();
    const modalApiWithHook = new ModalApi({ onOpened });
    modalApiWithHook.open();
    modalApiWithHook.onOpened();
    expect(onOpened).toHaveBeenCalled();
  });
});
