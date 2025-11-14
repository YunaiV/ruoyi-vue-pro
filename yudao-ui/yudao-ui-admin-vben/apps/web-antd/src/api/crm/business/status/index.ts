import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmBusinessStatusApi {
  /** 商机状态信息 */
  export interface BusinessStatusType {
    [x: string]: any;
    id?: number;
    name: string;
    percent: number;
  }

  /** 商机状态组信息 */
  export interface BusinessStatus {
    id?: number;
    name: string;
    deptIds?: number[];
    deptNames?: string[];
    creator?: string;
    createTime?: Date;
    statuses?: BusinessStatusType[];
  }
}

/** 默认商机状态 */
export const DEFAULT_STATUSES = [
  {
    endStatus: 1,
    key: '结束',
    name: '赢单',
    percent: 100,
  },
  {
    endStatus: 2,
    key: '结束',
    name: '输单',
    percent: 0,
  },
  {
    endStatus: 3,
    key: '结束',
    name: '无效',
    percent: 0,
  },
];

/** 查询商机状态组列表 */
export function getBusinessStatusPage(params: PageParam) {
  return requestClient.get<PageResult<CrmBusinessStatusApi.BusinessStatus>>(
    '/crm/business-status/page',
    { params },
  );
}

/** 新增商机状态组 */
export function createBusinessStatus(
  data: CrmBusinessStatusApi.BusinessStatus,
) {
  return requestClient.post('/crm/business-status/create', data);
}

/** 修改商机状态组 */
export function updateBusinessStatus(
  data: CrmBusinessStatusApi.BusinessStatus,
) {
  return requestClient.put('/crm/business-status/update', data);
}

/** 查询商机状态类型详情 */
export function getBusinessStatus(id: number) {
  return requestClient.get<CrmBusinessStatusApi.BusinessStatus>(
    `/crm/business-status/get?id=${id}`,
  );
}

/** 删除商机状态 */
export function deleteBusinessStatus(id: number) {
  return requestClient.delete(`/crm/business-status/delete?id=${id}`);
}

/** 获得商机状态组列表 */
export function getBusinessStatusTypeSimpleList() {
  return requestClient.get<CrmBusinessStatusApi.BusinessStatus[]>(
    '/crm/business-status/type-simple-list',
  );
}

/** 获得商机阶段列表 */
export function getBusinessStatusSimpleList(typeId: number) {
  return requestClient.get<CrmBusinessStatusApi.BusinessStatusType[]>(
    '/crm/business-status/status-simple-list',
    { params: { typeId } },
  );
}
