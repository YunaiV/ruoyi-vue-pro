import { requestClient } from '#/api/request';

export namespace IotStatisticsApi {
  /** IoT 统计摘要数据 */
  export interface StatisticsSummary {
    productCategoryCount: number;
    productCount: number;
    deviceCount: number;
    deviceMessageCount: number;
    productCategoryTodayCount: number;
    productTodayCount: number;
    deviceTodayCount: number;
    deviceMessageTodayCount: number;
    deviceOnlineCount: number;
    deviceOfflineCount: number;
    deviceInactiveCount: number;
    productCategoryDeviceCounts: Record<string, number>;
  }

  /** 时间戳-数值的键值对类型 */
  export interface TimeValueItem {
    [key: string]: number;
  }

  /** IoT 消息统计数据类型 */
  export interface DeviceMessageSummary {
    statType: number;
    upstreamCounts: TimeValueItem[];
    downstreamCounts: TimeValueItem[];
  }

  /** 消息统计数据项（按日期） */
  export interface DeviceMessageSummaryByDate {
    time: string;
    upstreamCount: number;
    downstreamCount: number;
  }

  /** 消息统计接口参数 */
  export interface DeviceMessageReq {
    interval: number;
    times?: string[];
  }
}

/** 获取 IoT 统计摘要数据 */
export function getStatisticsSummary() {
  return requestClient.get<IotStatisticsApi.StatisticsSummary>(
    '/iot/statistics/get-summary',
  );
}

/** 获取设备消息的数据统计（按日期） */
export function getDeviceMessageSummaryByDate(
  params: IotStatisticsApi.DeviceMessageReq,
) {
  return requestClient.get<IotStatisticsApi.DeviceMessageSummaryByDate[]>(
    '/iot/statistics/get-device-message-summary-by-date',
    { params },
  );
}

/** 获取设备消息统计摘要 */
export function getDeviceMessageSummary(statType: number) {
  return requestClient.get<IotStatisticsApi.DeviceMessageSummary>(
    '/iot/statistics/get-device-message-summary',
    { params: { statType } },
  );
}
