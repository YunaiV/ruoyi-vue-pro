import type { PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmStatisticsFunnelApi {
  /** 销售漏斗统计数据 */
  export interface FunnelSummary {
    customerCount: number; // 客户数
    businessCount: number; // 商机数
    businessWinCount: number; // 赢单数
  }

  /** 商机分析(按日期) */
  export interface BusinessSummaryByDate {
    time: string; // 时间
    businessCreateCount: number; // 商机数
    totalPrice: number | string; // 商机金额
  }

  /** 商机转化率分析(按日期) */
  export interface BusinessInversionRateSummaryByDate {
    time: string; // 时间
    businessCount: number; // 商机数量
    businessWinCount: number; // 赢单商机数
  }
}

export function getDatas(activeTabName: any, params: any) {
  switch (activeTabName) {
    case 'businessInversionRateSummary': {
      return getBusinessPageByDate(params);
    }
    case 'businessSummary': {
      return getBusinessPageByDate(params);
    }
    case 'funnel': {
      return getBusinessSummaryByEndStatus(params);
    }
    default: {
      return [];
    }
  }
}

export function getChartDatas(activeTabName: any, params: any) {
  switch (activeTabName) {
    case 'businessInversionRateSummary': {
      return getBusinessInversionRateSummaryByDate(params);
    }
    case 'businessSummary': {
      return getBusinessSummaryByDate(params);
    }
    case 'funnel': {
      return getFunnelSummary(params);
    }
    default: {
      return [];
    }
  }
}

/** 获取销售漏斗统计数据 */
export function getFunnelSummary(params: any) {
  return requestClient.get<CrmStatisticsFunnelApi.FunnelSummary>(
    '/crm/statistics-funnel/get-funnel-summary',
    { params },
  );
}

/** 获取商机结束状态统计 */
export function getBusinessSummaryByEndStatus(params: any) {
  return requestClient.get<Record<string, number>>(
    '/crm/statistics-funnel/get-business-summary-by-end-status',
    { params },
  );
}

/** 获取新增商机分析(按日期) */
export function getBusinessSummaryByDate(params: any) {
  return requestClient.get<CrmStatisticsFunnelApi.BusinessSummaryByDate[]>(
    '/crm/statistics-funnel/get-business-summary-by-date',
    { params },
  );
}

/** 获取商机转化率分析(按日期) */
export function getBusinessInversionRateSummaryByDate(params: any) {
  return requestClient.get<
    CrmStatisticsFunnelApi.BusinessInversionRateSummaryByDate[]
  >('/crm/statistics-funnel/get-business-inversion-rate-summary-by-date', {
    params,
  });
}

/** 获取商机列表(按日期) */
export function getBusinessPageByDate(params: any) {
  return requestClient.get<PageResult<any>>(
    '/crm/statistics-funnel/get-business-page-by-date',
    { params },
  );
}
