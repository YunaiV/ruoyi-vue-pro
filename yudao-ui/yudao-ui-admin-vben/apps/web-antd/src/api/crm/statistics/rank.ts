import type { PageParam } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmStatisticsRankApi {
  /** 排行统计数据 */
  export interface Rank {
    count: number;
    nickname: string;
    deptName: string;
  }
}

export function getDatas(activeTabName: any, params: any) {
  switch (activeTabName) {
    case 'contactCountRank': {
      return getContactsCountRank(params);
    }
    case 'contractCountRank': {
      return getContractCountRank(params);
    }
    case 'contractPriceRank': {
      return getContractPriceRank(params);
    }
    case 'customerCountRank': {
      return getCustomerCountRank(params);
    }
    case 'followCountRank': {
      return getFollowCountRank(params);
    }
    case 'followCustomerCountRank': {
      return getFollowCustomerCountRank(params);
    }
    case 'productSalesRank': {
      return getProductSalesRank(params);
    }
    case 'receivablePriceRank': {
      return getReceivablePriceRank(params);
    }
    default: {
      return [];
    }
  }
}

/** 获得合同排行榜 */
export function getContractPriceRank(params: PageParam) {
  return requestClient.get<CrmStatisticsRankApi.Rank[]>(
    '/crm/statistics-rank/get-contract-price-rank',
    { params },
  );
}

/** 获得回款排行榜 */
export function getReceivablePriceRank(params: PageParam) {
  return requestClient.get<CrmStatisticsRankApi.Rank[]>(
    '/crm/statistics-rank/get-receivable-price-rank',
    { params },
  );
}

/** 签约合同排行 */
export function getContractCountRank(params: PageParam) {
  return requestClient.get<CrmStatisticsRankApi.Rank[]>(
    '/crm/statistics-rank/get-contract-count-rank',
    { params },
  );
}

/** 产品销量排行 */
export function getProductSalesRank(params: PageParam) {
  return requestClient.get<CrmStatisticsRankApi.Rank[]>(
    '/crm/statistics-rank/get-product-sales-rank',
    { params },
  );
}

/** 新增客户数排行 */
export function getCustomerCountRank(params: PageParam) {
  return requestClient.get<CrmStatisticsRankApi.Rank[]>(
    '/crm/statistics-rank/get-customer-count-rank',
    { params },
  );
}

/** 新增联系人数排行 */
export function getContactsCountRank(params: PageParam) {
  return requestClient.get<CrmStatisticsRankApi.Rank[]>(
    '/crm/statistics-rank/get-contacts-count-rank',
    { params },
  );
}

/** 跟进次数排行 */
export function getFollowCountRank(params: PageParam) {
  return requestClient.get<CrmStatisticsRankApi.Rank[]>(
    '/crm/statistics-rank/get-follow-count-rank',
    { params },
  );
}

/** 跟进客户数排行 */
export function getFollowCustomerCountRank(params: PageParam) {
  return requestClient.get<CrmStatisticsRankApi.Rank[]>(
    '/crm/statistics-rank/get-follow-customer-count-rank',
    { params },
  );
}
