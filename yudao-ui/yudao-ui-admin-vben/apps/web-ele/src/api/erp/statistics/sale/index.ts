import { requestClient } from '#/api/request';

export namespace ErpSaleStatisticsApi {
  /** 销售全局统计 */
  export interface SaleSummaryRespVO {
    todayPrice: number; // 今日销售金额
    yesterdayPrice: number; // 昨日销售金额
    monthPrice: number; // 本月销售金额
    yearPrice: number; // 今年销售金额
  }

  /** 销售时间段统计 */
  export interface SaleTimeSummaryRespVO {
    time: string; // 时间
    price: number; // 销售金额
  }
}

/** 获得销售统计 */
export function getSaleSummary() {
  return requestClient.get<ErpSaleStatisticsApi.SaleSummaryRespVO>(
    '/erp/sale-statistics/summary',
  );
}

/** 获得销售时间段统计 */
export function getSaleTimeSummary() {
  return requestClient.get<ErpSaleStatisticsApi.SaleTimeSummaryRespVO[]>(
    '/erp/sale-statistics/time-summary',
  );
}
