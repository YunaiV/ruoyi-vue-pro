export interface StatisticCardProps {
  /** 标题 */
  title: string;
  /** 提示信息 */
  tooltip?: string;
  /** 前缀 */
  prefix?: string;
  /** 数值 */
  value?: number;
  /** 小数位数 */
  decimals?: number;
  /** 环比百分比 */
  percent?: number | string;
  /** 环比标签文本 */
  percentLabel?: string;
}
