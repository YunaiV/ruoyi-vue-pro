import type { Component } from 'vue';

export interface SummaryCardProps {
  /** 标题 */
  title: string;
  /** 提示信息 */
  tooltip?: string;
  /** 图标 */
  icon?: Component | string;
  /** 图标颜色 */
  iconColor?: string;
  /** 图标背景色 */
  iconBgColor?: string;
  /** 前缀 */
  prefix?: string;
  /** 数值 */
  value?: number;
  /** 小数位数 */
  decimals?: number;
  /** 百分比 */
  percent?: number | string;
}
