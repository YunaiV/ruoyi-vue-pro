import type { PageParam } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MpStatisticsApi {
  /** 统计查询参数 */
  export interface StatisticsQuery extends PageParam {
    accountId: number;
    beginDate: string;
    endDate: string;
  }

  /** 消息发送概况数据 */
  export interface UpstreamMessage {
    refDate: string;
    msgType: string;
    msgUser: number;
    msgCount: number;
  }

  /** 用户增减数据 */
  export interface UserSummary {
    refDate: string;
    userSource: number;
    newUser: number;
    cancelUser: number;
    cumulateUser: number;
  }

  /** 用户累计数据 */
  export interface UserCumulate {
    refDate: string;
    cumulateUser: number;
  }

  /** 接口分析数据 */
  export interface InterfaceSummary {
    refDate: string;
    callbackCount: number;
    failCount: number;
    totalTimeCost: number;
    maxTimeCost: number;
  }
}

/** 获取消息发送概况数据 */
export function getUpstreamMessage(params: MpStatisticsApi.StatisticsQuery) {
  return requestClient.get<MpStatisticsApi.UpstreamMessage[]>(
    '/mp/statistics/upstream-message',
    {
      params,
    },
  );
}

/** 获取用户增减数据 */
export function getUserSummary(params: MpStatisticsApi.StatisticsQuery) {
  return requestClient.get<MpStatisticsApi.UserSummary[]>(
    '/mp/statistics/user-summary',
    {
      params,
    },
  );
}

/** 获取用户累计数据 */
export function getUserCumulate(params: MpStatisticsApi.StatisticsQuery) {
  return requestClient.get<MpStatisticsApi.UserCumulate[]>(
    '/mp/statistics/user-cumulate',
    {
      params,
    },
  );
}

/** 获取接口分析数据 */
export function getInterfaceSummary(params: MpStatisticsApi.StatisticsQuery) {
  return requestClient.get<MpStatisticsApi.InterfaceSummary[]>(
    '/mp/statistics/interface-summary',
    {
      params,
    },
  );
}
