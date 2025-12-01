import { requestClient } from '#/api/request';

export namespace CrmStatisticsCustomerApi {
  /** 客户统计请求 */
  export interface CustomerSummaryReqVO {
    times: string[];
    interval: number;
    deptId: number;
    userId: number;
    userIds: number[];
  }

  /** 客户总量分析(按日期)响应 */
  export interface CustomerSummaryByDateRespVO {
    time: string;
    customerCreateCount: number;
    customerDealCount: number;
  }

  /** 客户总量分析(按用户)响应 */
  export interface CustomerSummaryByUserRespVO {
    ownerUserName: string;
    customerCreateCount: number;
    customerDealCount: number;
    contractPrice: number;
    receivablePrice: number;
  }

  /** 客户跟进次数分析(按日期)响应 */
  export interface FollowUpSummaryByDateRespVO {
    time: string;
    followUpRecordCount: number;
    followUpCustomerCount: number;
  }

  /** 客户跟进次数分析(按用户)响应 */
  export interface FollowUpSummaryByUserRespVO {
    ownerUserName: string;
    followupRecordCount: number;
    followupCustomerCount: number;
  }

  /** 客户跟进方式统计响应 */
  export interface FollowUpSummaryByTypeRespVO {
    followUpType: string;
    followUpRecordCount: number;
  }

  /** 合同摘要信息响应 */
  export interface CustomerContractSummaryRespVO {
    customerName: string;
    contractName: string;
    totalPrice: number;
    receivablePrice: number;
    customerType: string;
    customerSource: string;
    ownerUserName: string;
    creatorUserName: string;
    createTime: Date;
    orderDate: Date;
  }

  /** 客户公海分析(按日期)响应 */
  export interface PoolSummaryByDateRespVO {
    time: string;
    customerPutCount: number;
    customerTakeCount: number;
  }

  /** 客户公海分析(按用户)响应 */
  export interface PoolSummaryByUserRespVO {
    ownerUserName: string;
    customerPutCount: number;
    customerTakeCount: number;
  }

  /** 客户成交周期(按日期)响应 */
  export interface CustomerDealCycleByDateRespVO {
    time: string;
    customerDealCycle: number;
  }

  /** 客户成交周期(按用户)响应 */
  export interface CustomerDealCycleByUserRespVO {
    ownerUserName: string;
    customerDealCycle: number;
    customerDealCount: number;
  }

  /** 客户成交周期(按地区)响应 */
  export interface CustomerDealCycleByAreaRespVO {
    areaName: string;
    customerDealCycle: number;
    customerDealCount: number;
  }

  /** 客户成交周期(按产品)响应 */
  export interface CustomerDealCycleByProductRespVO {
    productName: string;
    customerDealCycle: number;
    customerDealCount: number;
  }
}

export function getDatas(activeTabName: any, params: any) {
  switch (activeTabName) {
    case 'conversionStat': {
      return getContractSummary(params);
    }
    case 'customerSummary': {
      return getCustomerSummaryByUser(params);
    }
    case 'dealCycleByArea': {
      return getCustomerDealCycleByArea(params);
    }
    case 'dealCycleByProduct': {
      return getCustomerDealCycleByProduct(params);
    }
    case 'dealCycleByUser': {
      return getCustomerDealCycleByUser(params);
    }
    case 'followUpSummary': {
      return getFollowUpSummaryByUser(params);
    }
    case 'followUpType': {
      return getFollowUpSummaryByType(params);
    }
    case 'poolSummary': {
      return getPoolSummaryByUser(params);
    }
    default: {
      return [];
    }
  }
}

export function getChartDatas(activeTabName: any, params: any) {
  switch (activeTabName) {
    case 'conversionStat': {
      return getCustomerSummaryByDate(params);
    }
    case 'customerSummary': {
      return getCustomerSummaryByDate(params);
    }
    case 'dealCycleByArea': {
      return getCustomerDealCycleByArea(params);
    }
    case 'dealCycleByProduct': {
      return getCustomerDealCycleByProduct(params);
    }
    case 'dealCycleByUser': {
      return getCustomerDealCycleByUser(params);
    }
    case 'followUpSummary': {
      return getFollowUpSummaryByDate(params);
    }
    case 'followUpType': {
      return getFollowUpSummaryByType(params);
    }
    case 'poolSummary': {
      return getPoolSummaryByDate(params);
    }
    default: {
      return [];
    }
  }
}

/** 客户总量分析(按日期) */
export function getCustomerSummaryByDate(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.CustomerSummaryByDateRespVO[]
  >('/crm/statistics-customer/get-customer-summary-by-date', { params });
}

/** 客户总量分析(按用户) */
export function getCustomerSummaryByUser(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.CustomerSummaryByUserRespVO[]
  >('/crm/statistics-customer/get-customer-summary-by-user', { params });
}

/** 客户跟进次数分析(按日期) */
export function getFollowUpSummaryByDate(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.FollowUpSummaryByDateRespVO[]
  >('/crm/statistics-customer/get-follow-up-summary-by-date', { params });
}

/** 客户跟进次数分析(按用户) */
export function getFollowUpSummaryByUser(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.FollowUpSummaryByUserRespVO[]
  >('/crm/statistics-customer/get-follow-up-summary-by-user', { params });
}

/** 获取客户跟进方式统计数 */
export function getFollowUpSummaryByType(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.FollowUpSummaryByTypeRespVO[]
  >('/crm/statistics-customer/get-follow-up-summary-by-type', { params });
}

/** 合同摘要信息(客户转化率页面) */
export function getContractSummary(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.CustomerContractSummaryRespVO[]
  >('/crm/statistics-customer/get-contract-summary', { params });
}

/** 获取客户公海分析(按日期) */
export function getPoolSummaryByDate(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<CrmStatisticsCustomerApi.PoolSummaryByDateRespVO[]>(
    '/crm/statistics-customer/get-pool-summary-by-date',
    { params },
  );
}

/** 获取客户公海分析(按用户) */
export function getPoolSummaryByUser(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<CrmStatisticsCustomerApi.PoolSummaryByUserRespVO[]>(
    '/crm/statistics-customer/get-pool-summary-by-user',
    { params },
  );
}

/** 获取客户成交周期(按日期) */
export function getCustomerDealCycleByDate(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.CustomerDealCycleByDateRespVO[]
  >('/crm/statistics-customer/get-customer-deal-cycle-by-date', { params });
}

/** 获取客户成交周期(按用户) */
export function getCustomerDealCycleByUser(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.CustomerDealCycleByUserRespVO[]
  >('/crm/statistics-customer/get-customer-deal-cycle-by-user', { params });
}

/** 获取客户成交周期(按地区) */
export function getCustomerDealCycleByArea(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.CustomerDealCycleByAreaRespVO[]
  >('/crm/statistics-customer/get-customer-deal-cycle-by-area', { params });
}

/** 获取客户成交周期(按产品) */
export function getCustomerDealCycleByProduct(
  params: CrmStatisticsCustomerApi.CustomerSummaryReqVO,
) {
  return requestClient.get<
    CrmStatisticsCustomerApi.CustomerDealCycleByProductRespVO[]
  >('/crm/statistics-customer/get-customer-deal-cycle-by-product', { params });
}
