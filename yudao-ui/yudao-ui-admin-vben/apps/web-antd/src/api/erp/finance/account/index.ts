import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpAccountApi {
  /** ERP 结算账户信息 */
  export interface Account {
    id?: number; // 结算账户编号
    no: string; // 账户编码
    remark: string; // 备注
    status: number; // 开启状态
    sort: number; // 排序
    defaultStatus: boolean; // 是否默认
    name: string; // 账户名称
  }

  /** 结算账户分页查询参数 */
  export interface AccountPageParam extends PageParam {
    name?: string;
    no?: string;
    status?: number;
  }
}

/** 查询结算账户分页 */
export function getAccountPage(params: ErpAccountApi.AccountPageParam) {
  return requestClient.get<PageResult<ErpAccountApi.Account>>(
    '/erp/account/page',
    { params },
  );
}

/** 查询结算账户精简列表 */
export function getAccountSimpleList() {
  return requestClient.get<ErpAccountApi.Account[]>('/erp/account/simple-list');
}

/** 查询结算账户详情 */
export function getAccount(id: number) {
  return requestClient.get<ErpAccountApi.Account>(`/erp/account/get?id=${id}`);
}

/** 新增结算账户 */
export function createAccount(data: ErpAccountApi.Account) {
  return requestClient.post('/erp/account/create', data);
}

/** 修改结算账户 */
export function updateAccount(data: ErpAccountApi.Account) {
  return requestClient.put('/erp/account/update', data);
}

/** 修改结算账户默认状态 */
export function updateAccountDefaultStatus(id: number, defaultStatus: boolean) {
  return requestClient.put('/erp/account/update-default-status', null, {
    params: { id, defaultStatus },
  });
}

/** 删除结算账户 */
export function deleteAccount(id: number) {
  return requestClient.delete(`/erp/account/delete?id=${id}`);
}

/** 导出结算账户 Excel */
export function exportAccount(params: any) {
  return requestClient.download('/erp/account/export-excel', { params });
}
