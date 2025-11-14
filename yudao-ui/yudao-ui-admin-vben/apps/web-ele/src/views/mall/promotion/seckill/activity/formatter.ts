import { formatDate } from '@vben/utils';

// 全局变量，用于存储配置列表
let configList: any[] = [];

/** 设置配置列表 */
export function setConfigList(list: any[]) {
  configList = list;
}

/** 格式化配置名称 */
export function formatConfigNames(configId: number): string {
  const config = configList.find((item) => item.id === configId);
  return config === null || config === undefined
    ? ''
    : `${config.name}[${config.startTime} ~ ${config.endTime}]`;
}

/** 格式化秒杀价格 */
export function formatSeckillPrice(products: any[]): string {
  if (!products || products.length === 0) {
    return '￥0.00';
  }
  const seckillPrice = Math.min(...products.map((item) => item.seckillPrice));
  return `￥${(seckillPrice / 100).toFixed(2)}`;
}

/** 格式化活动时间范围 */
export function formatTimeRange(
  startTime: Date | string,
  endTime: Date | string,
): string {
  return `${formatDate(startTime, 'YYYY-MM-DD')} ~ ${formatDate(endTime, 'YYYY-MM-DD')}`;
}
