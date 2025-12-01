import type { PageParam, PageResult } from '@vben/request';

import type { CrmPermissionApi } from '#/api/crm/permission';

import { requestClient } from '#/api/request';

export namespace CrmBusinessApi {
  /** 商机信息 */
  export interface Business {
    id: number;
    name: string;
    customerId: number;
    customerName?: string;
    followUpStatus: boolean;
    contactLastTime: Date;
    contactNextTime: Date;
    ownerUserId: number;
    ownerUserName?: string; // 负责人的用户名称
    ownerUserDept?: string; // 负责人的部门名称
    statusTypeId: number;
    statusTypeName?: string;
    statusId: number;
    statusName?: string;
    endStatus: number;
    endRemark: string;
    dealTime: Date;
    totalProductPrice: number;
    totalPrice: number;
    discountPercent: number;
    status?: number;
    remark: string;
    creator: string; // 创建人
    creatorName?: string; // 创建人名称
    createTime: Date; // 创建时间
    updateTime: Date; // 更新时间
    products?: BusinessProduct[];
  }

  /** 商机产品信息 */
  export interface BusinessProduct {
    id: number;
    productId: number;
    productName: string;
    productNo: string;
    productUnit: number;
    productPrice: number;
    businessPrice: number;
    count: number;
    totalPrice: number;
  }

  /** 商机更新状态请求 */
  export interface BusinessUpdateStatusReqVO {
    id: number;
    statusId: number | undefined;
    endStatus: number | undefined;
  }
}

/** 查询商机列表 */
export function getBusinessPage(params: PageParam) {
  return requestClient.get<PageResult<CrmBusinessApi.Business>>(
    '/crm/business/page',
    { params },
  );
}

/** 查询商机列表，基于指定客户 */
export function getBusinessPageByCustomer(params: PageParam) {
  return requestClient.get<PageResult<CrmBusinessApi.Business>>(
    '/crm/business/page-by-customer',
    { params },
  );
}

/** 查询商机详情 */
export function getBusiness(id: number) {
  return requestClient.get<CrmBusinessApi.Business>(
    `/crm/business/get?id=${id}`,
  );
}

/** 获得商机列表（精简） */
export function getSimpleBusinessList() {
  return requestClient.get<CrmBusinessApi.Business[]>(
    '/crm/business/simple-all-list',
  );
}

/** 新增商机 */
export function createBusiness(data: CrmBusinessApi.Business) {
  return requestClient.post('/crm/business/create', data);
}

/** 修改商机 */
export function updateBusiness(data: CrmBusinessApi.Business) {
  return requestClient.put('/crm/business/update', data);
}

/** 修改商机状态 */
export function updateBusinessStatus(
  data: CrmBusinessApi.BusinessUpdateStatusReqVO,
) {
  return requestClient.put('/crm/business/update-status', data);
}

/** 删除商机 */
export function deleteBusiness(id: number) {
  return requestClient.delete(`/crm/business/delete?id=${id}`);
}

/** 导出商机 */
export function exportBusiness(params: any) {
  return requestClient.download('/crm/business/export-excel', { params });
}

/** 联系人关联商机列表 */
export function getBusinessPageByContact(params: PageParam) {
  return requestClient.get<PageResult<CrmBusinessApi.Business>>(
    '/crm/business/page-by-contact',
    { params },
  );
}

/** 商机转移 */
export function transferBusiness(data: CrmPermissionApi.BusinessTransferReqVO) {
  return requestClient.put('/crm/business/transfer', data);
}
