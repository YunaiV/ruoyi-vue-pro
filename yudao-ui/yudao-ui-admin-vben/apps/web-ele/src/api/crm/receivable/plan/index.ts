import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmReceivablePlanApi {
  /** 回款计划信息 */
  export interface Plan {
    id: number;
    period: number;
    receivableId: number;
    price: number;
    returnTime: Date;
    remindDays: number;
    returnType: number;
    remindTime: Date;
    customerId: number;
    customerName?: string;
    contractId?: number;
    contractNo?: string;
    ownerUserId: number;
    ownerUserName?: string;
    remark: string;
    creator: string;
    creatorName?: string;
    createTime: Date;
    updateTime: Date;
    receivable?: {
      price: number;
      returnTime: Date;
    };
  }

  export interface PlanPageParam extends PageParam {
    customerId?: number;
    contractId?: number;
    contractNo?: string;
    sceneType?: number;
    remindType?: number;
  }
}

/** 查询回款计划列表 */
export function getReceivablePlanPage(
  params: CrmReceivablePlanApi.PlanPageParam,
) {
  return requestClient.get<PageResult<CrmReceivablePlanApi.Plan>>(
    '/crm/receivable-plan/page',
    { params },
  );
}

/** 查询回款计划列表(按客户) */
export function getReceivablePlanPageByCustomer(
  params: CrmReceivablePlanApi.PlanPageParam,
) {
  return requestClient.get<PageResult<CrmReceivablePlanApi.Plan>>(
    '/crm/receivable-plan/page-by-customer',
    { params },
  );
}

/** 查询回款计划详情 */
export function getReceivablePlan(id: number) {
  return requestClient.get<CrmReceivablePlanApi.Plan>(
    '/crm/receivable-plan/get',
    { params: { id } },
  );
}

/** 查询回款计划下拉数据 */
export function getReceivablePlanSimpleList(
  customerId: number,
  contractId: number,
) {
  return requestClient.get<CrmReceivablePlanApi.Plan[]>(
    '/crm/receivable-plan/simple-list',
    {
      params: { customerId, contractId },
    },
  );
}

/** 新增回款计划 */
export function createReceivablePlan(data: CrmReceivablePlanApi.Plan) {
  return requestClient.post('/crm/receivable-plan/create', data);
}

/** 修改回款计划 */
export function updateReceivablePlan(data: CrmReceivablePlanApi.Plan) {
  return requestClient.put('/crm/receivable-plan/update', data);
}

/** 删除回款计划 */
export function deleteReceivablePlan(id: number) {
  return requestClient.delete('/crm/receivable-plan/delete', {
    params: { id },
  });
}

/** 导出回款计划 Excel */
export function exportReceivablePlan(params: PageParam) {
  return requestClient.download('/crm/receivable-plan/export-excel', {
    params,
  });
}

/** 获得待回款提醒数量 */
export function getReceivablePlanRemindCount() {
  return requestClient.get<number>('/crm/receivable-plan/remind-count');
}
