import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmReceivableApi {
  /** 回款信息 */
  export interface Receivable {
    id: number;
    no: string;
    planId?: number;
    period?: number;
    customerId?: number;
    customerName?: string;
    contractId?: number;
    contract?: Contract;
    auditStatus: number;
    processInstanceId: number;
    returnTime: Date;
    returnType: number;
    price: number;
    ownerUserId: number;
    ownerUserName?: string;
    remark: string;
    creator: string; // 创建人
    creatorName?: string; // 创建人名称
    createTime: Date; // 创建时间
    updateTime: Date; // 更新时间
  }

  /** 合同信息 */
  export interface Contract {
    id?: number;
    name?: string;
    no: string;
    totalPrice: number;
  }
}

/** 查询回款列表 */
export function getReceivablePage(params: PageParam) {
  return requestClient.get<PageResult<CrmReceivableApi.Receivable>>(
    '/crm/receivable/page',
    { params },
  );
}

/** 查询回款列表，基于指定客户 */
export function getReceivablePageByCustomer(params: PageParam) {
  return requestClient.get<PageResult<CrmReceivableApi.Receivable>>(
    '/crm/receivable/page-by-customer',
    { params },
  );
}

/** 查询回款详情 */
export function getReceivable(id: number) {
  return requestClient.get<CrmReceivableApi.Receivable>(
    `/crm/receivable/get?id=${id}`,
  );
}

/** 新增回款 */
export function createReceivable(data: CrmReceivableApi.Receivable) {
  return requestClient.post('/crm/receivable/create', data);
}

/** 修改回款 */
export function updateReceivable(data: CrmReceivableApi.Receivable) {
  return requestClient.put('/crm/receivable/update', data);
}

/** 删除回款 */
export function deleteReceivable(id: number) {
  return requestClient.delete(`/crm/receivable/delete?id=${id}`);
}

/** 导出回款 */
export function exportReceivable(params: any) {
  return requestClient.download('/crm/receivable/export-excel', { params });
}

/** 提交审核 */
export function submitReceivable(id: number) {
  return requestClient.put(`/crm/receivable/submit?id=${id}`);
}

/** 获得待审核回款数量 */
export function getAuditReceivableCount() {
  return requestClient.get<number>('/crm/receivable/audit-count');
}
