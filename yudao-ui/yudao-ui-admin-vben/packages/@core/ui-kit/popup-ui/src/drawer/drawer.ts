import type { Component, Ref } from 'vue';

import type { ClassType, MaybePromise } from '@vben-core/typings';

import type { DrawerApi } from './drawer-api';

export type DrawerPlacement = 'bottom' | 'left' | 'right' | 'top';

export type CloseIconPlacement = 'left' | 'right';

export interface DrawerProps {
  /**
   * 是否挂载到内容区域
   * @default false
   */
  appendToMain?: boolean;
  /**
   * 取消按钮文字
   */
  cancelText?: string;
  class?: ClassType;
  /**
   * 是否显示关闭按钮
   * @default true
   */
  closable?: boolean;
  /**
   * 关闭按钮的位置
   */
  closeIconPlacement?: CloseIconPlacement;
  /**
   * 点击弹窗遮罩是否关闭弹窗
   * @default true
   */
  closeOnClickModal?: boolean;
  /**
   * 按下 ESC 键是否关闭弹窗
   * @default true
   */
  closeOnPressEscape?: boolean;
  /**
   * 确定按钮 loading
   * @default false
   */
  confirmLoading?: boolean;
  /**
   * 确定按钮文字
   */
  confirmText?: string;
  contentClass?: string;
  /**
   * 弹窗描述
   */
  description?: string;
  /**
   * 在关闭时销毁抽屉
   */
  destroyOnClose?: boolean;
  /**
   * 是否显示底部
   * @default true
   */
  footer?: boolean;
  /**
   * 弹窗底部样式
   */
  footerClass?: ClassType;
  /**
   * 是否显示顶栏
   * @default true
   */
  header?: boolean;
  /**
   * 弹窗头部样式
   */
  headerClass?: ClassType;
  /**
   * 弹窗是否显示
   * @default false
   */
  loading?: boolean;
  /**
   * 是否显示遮罩
   * @default true
   */
  modal?: boolean;

  /**
   * 是否自动聚焦
   */
  openAutoFocus?: boolean;
  /**
   * 弹窗遮罩模糊效果
   */
  overlayBlur?: number;
  /**
   * 抽屉位置
   * @default right
   */
  placement?: DrawerPlacement;

  /**
   * 是否显示取消按钮
   * @default true
   */
  showCancelButton?: boolean;
  /**
   * 是否显示确认按钮
   * @default true
   */
  showConfirmButton?: boolean;
  /**
   * 提交中（锁定抽屉状态）
   */
  submitting?: boolean;
  /**
   * 弹窗标题
   */
  title?: string;
  /**
   * 弹窗标题提示
   */
  titleTooltip?: string;
  /**
   * 抽屉层级
   */
  zIndex?: number;
}

export interface DrawerState extends DrawerProps {
  /** 弹窗打开状态 */
  isOpen?: boolean;
  /**
   * 共享数据
   */
  sharedData?: Record<string, any>;
}

export type ExtendedDrawerApi = DrawerApi & {
  useStore: <T = NoInfer<DrawerState>>(
    selector?: (state: NoInfer<DrawerState>) => T,
  ) => Readonly<Ref<T>>;
};

export interface DrawerApiOptions extends DrawerState {
  /**
   * 独立的抽屉组件
   */
  connectedComponent?: Component;
  /**
   * 关闭前的回调，返回 false 可以阻止关闭
   * @returns
   */
  onBeforeClose?: () => MaybePromise<boolean | undefined>;
  /**
   * 点击取消按钮的回调
   */
  onCancel?: () => void;
  /**
   * 弹窗关闭动画结束的回调
   * @returns
   */
  onClosed?: () => void;
  /**
   * 点击确定按钮的回调
   */
  onConfirm?: () => void;
  /**
   * 弹窗状态变化回调
   * @param isOpen
   * @returns
   */
  onOpenChange?: (isOpen: boolean) => void;
  /**
   * 弹窗打开动画结束的回调
   * @returns
   */
  onOpened?: () => void;
}
