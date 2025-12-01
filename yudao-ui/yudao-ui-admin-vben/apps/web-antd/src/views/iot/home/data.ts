import type { IotStatisticsApi } from '#/api/iot/statistics';

/** 默认统计数据 */
export const defaultStatsData: IotStatisticsApi.StatisticsSummary = {
  productCategoryCount: 0,
  productCount: 0,
  deviceCount: 0,
  deviceMessageCount: 0,
  productCategoryTodayCount: 0,
  productTodayCount: 0,
  deviceTodayCount: 0,
  deviceMessageTodayCount: 0,
  deviceOnlineCount: 0,
  deviceOfflineCount: 0,
  deviceInactiveCount: 0,
  productCategoryDeviceCounts: {},
};
