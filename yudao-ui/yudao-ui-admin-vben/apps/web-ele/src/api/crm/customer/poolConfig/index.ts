import { requestClient } from '#/api/request';

export namespace CrmCustomerPoolConfigApi {
  /** 客户公海规则设置 */
  export interface CustomerPoolConfig {
    enabled?: boolean;
    contactExpireDays?: number;
    dealExpireDays?: number;
    notifyEnabled?: boolean;
    notifyDays?: number;
  }
}

/** 获取客户公海规则设置 */
export function getCustomerPoolConfig() {
  return requestClient.get<CrmCustomerPoolConfigApi.CustomerPoolConfig>(
    '/crm/customer-pool-config/get',
  );
}

/** 更新客户公海规则设置 */
export function saveCustomerPoolConfig(
  data: CrmCustomerPoolConfigApi.CustomerPoolConfig,
) {
  return requestClient.put('/crm/customer-pool-config/save', data);
}
