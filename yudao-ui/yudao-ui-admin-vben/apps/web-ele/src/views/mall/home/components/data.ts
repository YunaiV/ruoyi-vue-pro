export interface WorkbenchQuickDataShowItem {
  name: string;
  value: number;
  prefix: string;
  decimals: number;
  routerName: string;
}

export interface AnalysisOverviewItem {
  title: string;
  totalTitle?: string;
  totalValue?: number;
  value: number;
  prefix?: string;
  tooltip?: string;
  // 环比增长相关字段
  showGrowthRate?: boolean; // 是否显示环比增长率，默认为false
}

export interface AnalysisOverviewIconItem {
  icon: string;
  title: string;
  value: number;
  prefix?: string;
  iconBgColor: string;
  iconColor: string;
  tooltip?: string;
  decimals?: number;
  percent?: number;
}

export interface AnalysisOverviewTradeItem {
  title: string;
  value: number;
  prefix?: string;
  decimals?: number;
  percent?: number;
  tooltip?: string;
}
