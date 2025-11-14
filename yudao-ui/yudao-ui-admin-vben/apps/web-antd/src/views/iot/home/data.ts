/**
 * IoT 首页数据配置文件
 *
 * 该文件封装了 IoT 首页所需的:
 * - 统计数据接口定义
 * - 业务逻辑函数
 * - 工具函数
 */

import type { IotStatisticsApi } from '#/api/iot/statistics';

import { onMounted, ref } from 'vue';

import { getStatisticsSummary } from '#/api/iot/statistics';

/** 统计数据接口 - 使用 API 定义的类型 */
export type StatsData = IotStatisticsApi.StatisticsSummary;

/** 默认统计数据 */
export const defaultStatsData: StatsData = {
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

/**
 * 加载统计数据
 * @returns Promise<StatsData>
 */
export async function loadStatisticsData(): Promise<StatsData> {
  try {
    const data = await getStatisticsSummary();
    return data;
  } catch (error) {
    console.error('获取统计数据出错:', error);
    console.warn('使用 Mock 数据，请检查后端接口是否已实现');

    // 返回 Mock 数据用于开发调试
    return {
      productCategoryCount: 12,
      productCount: 45,
      deviceCount: 328,
      deviceMessageCount: 15_678,
      productCategoryTodayCount: 2,
      productTodayCount: 5,
      deviceTodayCount: 23,
      deviceMessageTodayCount: 1234,
      deviceOnlineCount: 256,
      deviceOfflineCount: 48,
      deviceInactiveCount: 24,
      productCategoryDeviceCounts: {
        智能家居: 120,
        工业设备: 98,
        环境监测: 65,
        智能穿戴: 45,
      },
    };
  }
}

/**
 * IoT 首页业务逻辑 Hook
 * 封装了首页的所有业务逻辑和状态管理
 */
export function useIotHome() {
  const loading = ref(true);
  const statsData = ref<StatsData>(defaultStatsData);

  /**
   * 加载数据
   */
  async function loadData() {
    loading.value = true;
    try {
      statsData.value = await loadStatisticsData();
    } catch (error) {
      console.error('获取统计数据出错:', error);
    } finally {
      loading.value = false;
    }
  }

  // 组件挂载时加载数据
  onMounted(() => {
    loadData();
  });

  return {
    loading,
    statsData,
    loadData,
  };
}

/** 格式化数字 - 大数字显示为 K/M */
export function formatNumber(num: number): string {
  if (num >= 1_000_000) {
    return `${(num / 1_000_000).toFixed(1)}M`;
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}K`;
  }
  return num.toString();
}
