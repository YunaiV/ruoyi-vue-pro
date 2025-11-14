import type { PageParam } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmStatisticsPortraitApi {
  /** 客户基础统计信息 */
  export interface CustomerBase {
    customerCount: number;
    dealCount: number;
    dealPortion: number | string;
  }

  /** 客户行业统计信息 */
  export interface CustomerIndustry extends CustomerBase {
    industryId: number;
    industryPortion: number | string;
  }

  /** 客户来源统计信息 */
  export interface CustomerSource extends CustomerBase {
    source: number;
    sourcePortion: number | string;
  }

  /** 客户级别统计信息 */
  export interface CustomerLevel extends CustomerBase {
    level: number;
    levelPortion: number | string;
  }

  /** 客户地区统计信息 */
  export interface CustomerArea extends CustomerBase {
    areaId: number;
    areaName: string;
    areaPortion: number | string;
  }
}

export function getDatas(activeTabName: any, params: any) {
  switch (activeTabName) {
    case 'area': {
      return getCustomerArea(params);
    }
    case 'industry': {
      return getCustomerIndustry(params);
    }
    case 'level': {
      return getCustomerLevel(params);
    }
    case 'source': {
      return getCustomerSource(params);
    }
    default: {
      return [];
    }
  }
}

/** 获取客户行业统计数据 */
export function getCustomerIndustry(params: PageParam) {
  return requestClient.get<CrmStatisticsPortraitApi.CustomerIndustry[]>(
    '/crm/statistics-portrait/get-customer-industry-summary',
    { params },
  );
}

/** 获取客户来源统计数据 */
export function getCustomerSource(params: PageParam) {
  return requestClient.get<CrmStatisticsPortraitApi.CustomerSource[]>(
    '/crm/statistics-portrait/get-customer-source-summary',
    { params },
  );
}

/** 获取客户级别统计数据 */
export function getCustomerLevel(params: PageParam) {
  return requestClient.get<CrmStatisticsPortraitApi.CustomerLevel[]>(
    '/crm/statistics-portrait/get-customer-level-summary',
    { params },
  );
}

/** 获取客户地区统计数据 */
export function getCustomerArea(params: PageParam) {
  return requestClient.get<CrmStatisticsPortraitApi.CustomerArea[]>(
    '/crm/statistics-portrait/get-customer-area-summary',
    { params },
  );
}
