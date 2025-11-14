import type { AsTag } from 'radix-vue';

import type { Component } from 'vue';

import type { ButtonVariants, ButtonVariantSize } from '../../ui';

export interface VbenButtonProps {
  /**
   * The element or component this component should render as. Can be overwrite by `asChild`
   * @defaultValue "div"
   */
  as?: AsTag | Component;
  /**
   * Change the default rendered element for the one passed as a child, merging their props and behavior.
   *
   * Read our [Composition](https://www.radix-vue.com/guides/composition.html) guide for more details.
   */
  asChild?: boolean;
  class?: any;
  disabled?: boolean;
  loading?: boolean;
  size?: ButtonVariantSize;
  variant?: ButtonVariants;
}

export type CustomRenderType = (() => Component | string) | string;

export type ValueType = boolean | number | string;

export interface VbenButtonGroupProps
  extends Pick<VbenButtonProps, 'disabled'> {
  /** 单选模式下允许清除选中 */
  allowClear?: boolean;
  /** 值改变前的回调 */
  beforeChange?: (
    value: ValueType,
    isChecked: boolean,
  ) => boolean | PromiseLike<boolean | undefined> | undefined;
  /** 按钮样式 */
  btnClass?: any;
  /** 按钮间隔距离 */
  gap?: number;
  /** 多选模式下限制最多选择的数量。0表示不限制 */
  maxCount?: number;
  /** 是否允许多选 */
  multiple?: boolean;
  /** 选项 */
  options?: { [key: string]: any; label: CustomRenderType; value: ValueType }[];
  /** 显示图标 */
  showIcon?: boolean;
  /** 尺寸 */
  size?: 'large' | 'middle' | 'small';
}
