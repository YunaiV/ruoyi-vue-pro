import type { DrawerApiOptions, DrawerState } from './drawer';

import { Store } from '@vben-core/shared/store';
import { bindMethods, isFunction } from '@vben-core/shared/utils';

export class DrawerApi {
  // 共享数据
  public sharedData: Record<'payload', any> = {
    payload: {},
  };
  public store: Store<DrawerState>;

  private api: Pick<
    DrawerApiOptions,
    | 'onBeforeClose'
    | 'onCancel'
    | 'onClosed'
    | 'onConfirm'
    | 'onOpenChange'
    | 'onOpened'
  >;

  // private prevState!: DrawerState;
  private state!: DrawerState;

  constructor(options: DrawerApiOptions = {}) {
    const {
      connectedComponent: _,
      onBeforeClose,
      onCancel,
      onClosed,
      onConfirm,
      onOpenChange,
      onOpened,
      ...storeState
    } = options;

    const defaultState: DrawerState = {
      class: '',
      closable: true,
      closeIconPlacement: 'right',
      closeOnClickModal: true,
      closeOnPressEscape: true,
      confirmLoading: false,
      contentClass: '',
      footer: true,
      header: true,
      isOpen: false,
      loading: false,
      modal: true,
      openAutoFocus: false,
      placement: 'right',
      showCancelButton: true,
      showConfirmButton: true,
      submitting: false,
      title: '',
    };

    this.store = new Store<DrawerState>(
      {
        ...defaultState,
        ...storeState,
      },
      {
        onUpdate: () => {
          const state = this.store.state;
          if (state?.isOpen === this.state?.isOpen) {
            this.state = state;
          } else {
            this.state = state;
            this.api.onOpenChange?.(!!state?.isOpen);
          }
        },
      },
    );
    this.state = this.store.state;
    this.api = {
      onBeforeClose,
      onCancel,
      onClosed,
      onConfirm,
      onOpenChange,
      onOpened,
    };
    bindMethods(this);
  }

  /**
   * 关闭抽屉
   * @description 关闭抽屉时会调用 onBeforeClose 钩子函数，如果 onBeforeClose 返回 false，则不关闭弹窗
   */
  async close() {
    // 通过 onBeforeClose 钩子函数来判断是否允许关闭弹窗
    // 如果 onBeforeClose 返回 false，则不关闭弹窗
    const allowClose = (await this.api.onBeforeClose?.()) ?? true;
    if (allowClose) {
      this.store.setState((prev) => ({
        ...prev,
        isOpen: false,
        submitting: false,
      }));
    }
  }

  getData<T extends object = Record<string, any>>() {
    return (this.sharedData?.payload ?? {}) as T;
  }

  /**
   * 锁定抽屉状态（用于提交过程中的等待状态）
   * @description 锁定状态将禁用默认的取消按钮，使用spinner覆盖抽屉内容，隐藏关闭按钮，阻止手动关闭弹窗，将默认的提交按钮标记为loading状态
   * @param isLocked 是否锁定
   */
  lock(isLocked: boolean = true) {
    return this.setState({ submitting: isLocked });
  }

  /**
   * 取消操作
   */
  onCancel() {
    if (this.api.onCancel) {
      this.api.onCancel?.();
    } else {
      this.close();
    }
  }

  /**
   * 弹窗关闭动画播放完毕后的回调
   */
  onClosed() {
    if (!this.state.isOpen) {
      this.api.onClosed?.();
    }
  }

  /**
   * 确认操作
   */
  onConfirm() {
    this.api.onConfirm?.();
  }

  /**
   * 弹窗打开动画播放完毕后的回调
   */
  onOpened() {
    if (this.state.isOpen) {
      this.api.onOpened?.();
    }
  }

  open() {
    this.store.setState((prev) => ({ ...prev, isOpen: true }));
  }

  setData<T>(payload: T) {
    this.sharedData.payload = payload;
    return this;
  }

  setState(
    stateOrFn:
      | ((prev: DrawerState) => Partial<DrawerState>)
      | Partial<DrawerState>,
  ) {
    if (isFunction(stateOrFn)) {
      this.store.setState(stateOrFn);
    } else {
      this.store.setState((prev) => ({ ...prev, ...stateOrFn }));
    }
    return this;
  }

  /**
   * 解除抽屉的锁定状态
   * @description 解除由lock方法设置的锁定状态，是lock(false)的别名
   */
  unlock() {
    return this.lock(false);
  }
}
