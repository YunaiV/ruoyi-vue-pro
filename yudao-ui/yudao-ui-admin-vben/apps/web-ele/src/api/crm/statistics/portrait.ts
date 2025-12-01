import type { PageParam } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmStatisticsPortraitApi {
  /** 客户基础统计响应 */
  export interface CustomerBaseRespVO {
    customerCount: number;
    dealCount: number;
    dealPortion: number | string;
  }

  /** 客户行业统计响应 */
  export interface CustomerIndustryRespVO extends CustomerBaseRespVO {
    industryId: number;
    industryPortion: number | string;
  }

  /** 客户来源统计响应 */
  export interface CustomerSourceRespVO extends CustomerBaseRespVO {
    source: number;
    sourcePortion: number | string;
  }

  /** 客户级别统计响应 */
  export interface CustomerLevelRespVO extends CustomerBaseRespVO {
    level: number;
    levelPortion: number | string;
  }

  /** 客户地区统计响应 */
  export interface CustomerAreaRespVO extends CustomerBaseRespVO {
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
  return requestClient.get<CrmStatisticsPortraitApi.CustomerIndustryRespVO[]>(
    '/crm/statistics-portrait/get-customer-industry-summary',
    { params },
  );
}

/** 获取客户来源统计数据 */
export function getCustomerSource(params: PageParam) {
  return requestClient.get<CrmStatisticsPortraitApi.CustomerSourceRespVO[]>(
    '/crm/statistics-portrait/get-customer-source-summary',
    { params },
  );
}

/** 获取客户级别统计数据 */
export function getCustomerLevel(params: PageParam) {
  return requestClient.get<CrmStatisticsPortraitApi.CustomerLevelRespVO[]>(
    '/crm/statistics-portrait/get-customer-level-summary',
    { params },
  );
}

/** 获取客户地区统计数据 */
export function getCustomerArea(params: PageParam) {
  return requestClient.get<CrmStatisticsPortraitApi.CustomerAreaRespVO[]>(
    '/crm/statistics-portrait/get-customer-area-summary',
    { params },
  );
}
