import { requestClient } from '#/api/request';

export namespace ErpPurchaseStatisticsApi {
  /** ERP 采购全局统计 */
  export interface PurchaseSummary {
    todayPrice: number; // 今日采购金额
    yesterdayPrice: number; // 昨日采购金额
    monthPrice: number; // 本月采购金额
    yearPrice: number; // 今年采购金额
  }

  /** ERP 采购时间段统计 */
  export interface PurchaseTimeSummary {
    time: string; // 时间
    price: number; // 采购金额
  }
}

/** 获得采购统计 */
export function getPurchaseSummary() {
  return requestClient.get<ErpPurchaseStatisticsApi.PurchaseSummary>(
    '/erp/purchase-statistics/summary',
  );
}

/** 获得采购时间段统计 */
export function getPurchaseTimeSummary() {
  return requestClient.get<ErpPurchaseStatisticsApi.PurchaseTimeSummary[]>(
    '/erp/purchase-statistics/time-summary',
  );
}
