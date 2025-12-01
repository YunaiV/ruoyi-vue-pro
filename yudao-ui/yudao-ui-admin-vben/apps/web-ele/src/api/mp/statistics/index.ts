import { requestClient } from '#/api/request';

export namespace MpStatisticsApi {
  /** 统计查询参数 */
  export interface StatisticsGetReqVO {
    accountId: number;
    date: Date[];
  }

  /** 消息发送概况数据 */
  export interface StatisticsUpstreamMessageRespVO {
    refDate: string;
    msgType: string;
    msgUser: number;
    msgCount: number;
  }

  /** 用户增减数据 */
  export interface StatisticsUserSummaryRespVO {
    refDate: string;
    userSource: number;
    newUser: number;
    cancelUser: number;
    cumulateUser: number;
  }

  /** 用户累计数据 */
  export interface StatisticsUserCumulateRespVO {
    refDate: string;
    cumulateUser: number;
  }

  /** 接口分析数据 */
  export interface StatisticsInterfaceSummaryRespVO {
    refDate: string;
    callbackCount: number;
    failCount: number;
    totalTimeCost: number;
    maxTimeCost: number;
  }
}

/** 获取消息发送概况数据 */
export function getUpstreamMessage(params: MpStatisticsApi.StatisticsGetReqVO) {
  return requestClient.get<MpStatisticsApi.StatisticsUpstreamMessageRespVO[]>(
    '/mp/statistics/upstream-message',
    {
      params,
    },
  );
}

/** 获取用户增减数据 */
export function getUserSummary(params: MpStatisticsApi.StatisticsGetReqVO) {
  return requestClient.get<MpStatisticsApi.StatisticsUserSummaryRespVO[]>(
    '/mp/statistics/user-summary',
    {
      params,
    },
  );
}

/** 获取用户累计数据 */
export function getUserCumulate(params: MpStatisticsApi.StatisticsGetReqVO) {
  return requestClient.get<MpStatisticsApi.StatisticsUserCumulateRespVO[]>(
    '/mp/statistics/user-cumulate',
    {
      params,
    },
  );
}

/** 获取接口分析数据 */
export function getInterfaceSummary(
  params: MpStatisticsApi.StatisticsGetReqVO,
) {
  return requestClient.get<MpStatisticsApi.StatisticsInterfaceSummaryRespVO[]>(
    '/mp/statistics/interface-summary',
    {
      params,
    },
  );
}
