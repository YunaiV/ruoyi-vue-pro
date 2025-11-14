import type { Component, VNode, VNodeArrayChildren } from 'vue';

import type { Recordable } from '@vben-core/typings';

import { createContext } from '@vben-core/shadcn-ui';

export type IconType = 'error' | 'info' | 'question' | 'success' | 'warning';

export type BeforeCloseScope = {
  isConfirm: boolean;
};

export type AlertProps = {
  /** 关闭前的回调，如果返回false，则终止关闭 */
  beforeClose?: (
    scope: BeforeCloseScope,
  ) => boolean | Promise<boolean | undefined> | undefined;
  /** 边框 */
  bordered?: boolean;
  /**
   * 按钮对齐方式
   * @default 'end'
   */
  buttonAlign?: 'center' | 'end' | 'start';
  /** 取消按钮的标题 */
  cancelText?: string;
  /** 是否居中显示 */
  centered?: boolean;
  /** 确认按钮的标题 */
  confirmText?: string;
  /** 弹窗容器的额外样式 */
  containerClass?: string;
  /** 弹窗提示内容 */
  content: Component | string;
  /** 弹窗内容的额外样式 */
  contentClass?: string;
  /** 执行beforeClose回调期间，在内容区域显示一个loading遮罩*/
  contentMasking?: boolean;
  /** 弹窗底部内容（与按钮在同一个容器中） */
  footer?: Component | string;
  /** 弹窗的图标（在标题的前面） */
  icon?: Component | IconType;
  /**
   * 弹窗遮罩模糊效果
   */
  overlayBlur?: number;
  /** 是否显示取消按钮 */
  showCancel?: boolean;
  /** 弹窗标题 */
  title?: string;
};

/** Prompt属性 */
export type PromptProps<T = any> = {
  /** 关闭前的回调，如果返回false，则终止关闭 */
  beforeClose?: (scope: {
    isConfirm: boolean;
    value: T | undefined;
  }) => boolean | Promise<boolean | undefined> | undefined;
  /** 用于接受用户输入的组件 */
  component?: Component;
  /** 输入组件的属性 */
  componentProps?: Recordable<any>;
  /** 输入组件的插槽 */
  componentSlots?:
    | (() => any)
    | Recordable<unknown>
    | VNode
    | VNodeArrayChildren;
  /** 默认值 */
  defaultValue?: T;
  /** 输入组件的值属性名 */
  modelPropName?: string;
} & Omit<AlertProps, 'beforeClose'>;

/**
 * Alert上下文
 */
export type AlertContext = {
  /** 执行取消操作 */
  doCancel: () => void;
  /** 执行确认操作 */
  doConfirm: () => void;
};

export const [injectAlertContext, provideAlertContext] =
  createContext<AlertContext>('VbenAlertContext');

/**
 * 获取Alert上下文
 * @returns AlertContext
 */
export function useAlertContext() {
  const context = injectAlertContext();
  if (!context) {
    throw new Error('useAlertContext must be used within an AlertProvider');
  }
  return context;
}
