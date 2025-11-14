import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmCustomerLimitConfigApi {
  /** 客户限制配置 */
  export interface CustomerLimitConfig {
    id?: number;
    type?: number;
    userIds?: string;
    deptIds?: string;
    maxCount?: number;
    dealCountEnabled?: boolean;
  }
}

/** 客户限制配置类型 */
export enum LimitConfType {
  /** 锁定客户数限制 */
  CUSTOMER_LOCK_LIMIT = 2,
  /** 拥有客户数限制 */
  CUSTOMER_QUANTITY_LIMIT = 1,
}

/** 查询客户限制配置列表 */
export function getCustomerLimitConfigPage(params: PageParam) {
  return requestClient.get<
    PageResult<CrmCustomerLimitConfigApi.CustomerLimitConfig>
  >('/crm/customer-limit-config/page', { params });
}

/** 查询客户限制配置详情 */
export function getCustomerLimitConfig(id: number) {
  return requestClient.get<CrmCustomerLimitConfigApi.CustomerLimitConfig>(
    `/crm/customer-limit-config/get?id=${id}`,
  );
}

/** 新增客户限制配置 */
export function createCustomerLimitConfig(
  data: CrmCustomerLimitConfigApi.CustomerLimitConfig,
) {
  return requestClient.post('/crm/customer-limit-config/create', data);
}

/** 修改客户限制配置 */
export function updateCustomerLimitConfig(
  data: CrmCustomerLimitConfigApi.CustomerLimitConfig,
) {
  return requestClient.put('/crm/customer-limit-config/update', data);
}

/** 删除客户限制配置 */
export function deleteCustomerLimitConfig(id: number) {
  return requestClient.delete(`/crm/customer-limit-config/delete?id=${id}`);
}
