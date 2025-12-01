import { requestClient } from '#/api/request';

export namespace CrmStatisticsPerformanceApi {
  /** 员工业绩统计请求 */
  export interface PerformanceReqVO {
    times: string[];
    deptId: number;
    userId: number;
  }

  /** 员工业绩统计响应 */
  export interface PerformanceRespVO {
    time: string;
    currentMonthCount: number;
    lastMonthCount: number;
    lastYearCount: number;
  }
}

/** 员工获得合同金额统计 */
export function getContractPricePerformance(
  params: CrmStatisticsPerformanceApi.PerformanceReqVO,
) {
  return requestClient.get<CrmStatisticsPerformanceApi.PerformanceRespVO[]>(
    '/crm/statistics-performance/get-contract-price-performance',
    { params },
  );
}

/** 员工获得回款统计 */
export function getReceivablePricePerformance(
  params: CrmStatisticsPerformanceApi.PerformanceReqVO,
) {
  return requestClient.get<CrmStatisticsPerformanceApi.PerformanceRespVO[]>(
    '/crm/statistics-performance/get-receivable-price-performance',
    { params },
  );
}

/** 员工获得签约合同数量统计 */
export function getContractCountPerformance(
  params: CrmStatisticsPerformanceApi.PerformanceReqVO,
) {
  return requestClient.get<CrmStatisticsPerformanceApi.PerformanceRespVO[]>(
    '/crm/statistics-performance/get-contract-count-performance',
    { params },
  );
}
