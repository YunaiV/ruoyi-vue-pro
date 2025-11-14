import type { CubicBezierPoints, EasingFunction } from '@vueuse/core';

import type { StyleValue } from 'vue';

import { TransitionPresets as TransitionPresetsData } from '@vueuse/core';

export type TransitionPresets = keyof typeof TransitionPresetsData;

export const TransitionPresetsKeys = Object.keys(
  TransitionPresetsData,
) as TransitionPresets[];

export interface CountToProps {
  /** 初始值 */
  startVal?: number;
  /** 当前值 */
  endVal: number;
  /** 是否禁用动画 */
  disabled?: boolean;
  /** 延迟动画开始的时间 */
  delay?: number;
  /** 持续时间  */
  duration?: number;
  /** 小数位数  */
  decimals?: number;
  /** 小数点  */
  decimal?: string;
  /** 分隔符  */
  separator?: string;
  /** 前缀  */
  prefix?: string;
  /** 后缀  */
  suffix?: string;
  /** 过渡效果  */
  transition?: CubicBezierPoints | EasingFunction | TransitionPresets;
  /** 整数部分的类名 */
  mainClass?: string;
  /** 小数部分的类名 */
  decimalClass?: string;
  /** 前缀部分的类名 */
  prefixClass?: string;
  /** 后缀部分的类名 */
  suffixClass?: string;

  /** 整数部分的样式 */
  mainStyle?: StyleValue;
  /** 小数部分的样式 */
  decimalStyle?: StyleValue;
  /** 前缀部分的样式 */
  prefixStyle?: StyleValue;
  /** 后缀部分的样式 */
  suffixStyle?: StyleValue;
}
