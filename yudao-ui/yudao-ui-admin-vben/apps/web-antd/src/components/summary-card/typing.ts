export interface SummaryCardProps {
  title: string;
  tooltip?: string;
  icon?: string;
  iconColor?: string;
  iconBgColor?: string;
  prefix?: string;
  value?: number;
  decimals?: number;
  percent?: number | string;
}
