import { requestClient } from '#/api/request';

export namespace CrmContractConfigApi {
  /** 合同配置信息 */
  export interface Config {
    notifyEnabled?: boolean;
    notifyDays?: number;
  }
}

/** 获取合同配置 */
export function getContractConfig() {
  return requestClient.get<CrmContractConfigApi.Config>(
    '/crm/contract-config/get',
  );
}

/** 更新合同配置 */
export function saveContractConfig(data: CrmContractConfigApi.Config) {
  return requestClient.put('/crm/contract-config/save', data);
}
