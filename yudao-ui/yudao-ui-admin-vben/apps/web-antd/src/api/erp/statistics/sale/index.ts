import { requestClient } from '#/api/request';

export namespace ErpSaleStatisticsApi {
  /** ERP 销售全局统计 */
  export interface SaleSummary {
    todayPrice: number; // 今日销售金额
    yesterdayPrice: number; // 昨日销售金额
    monthPrice: number; // 本月销售金额
    yearPrice: number; // 今年销售金额
  }

  /** ERP 销售时间段统计 */
  export interface SaleTimeSummary {
    time: string; // 时间
    price: number; // 销售金额
  }
}

/** 获得销售统计 */
export function getSaleSummary() {
  return requestClient.get<ErpSaleStatisticsApi.SaleSummary>(
    '/erp/sale-statistics/summary',
  );
}

/** 获得销售时间段统计 */
export function getSaleTimeSummary() {
  return requestClient.get<ErpSaleStatisticsApi.SaleTimeSummary[]>(
    '/erp/sale-statistics/time-summary',
  );
}
