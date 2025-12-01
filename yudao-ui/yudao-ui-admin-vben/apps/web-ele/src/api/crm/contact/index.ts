import type { PageParam, PageResult } from '@vben/request';

import type { CrmPermissionApi } from '#/api/crm/permission';

import { requestClient } from '#/api/request';

export namespace CrmContactApi {
  /** 联系人信息 */
  export interface Contact {
    id: number; // 编号
    name: string; // 联系人名称
    customerId: number; // 客户编号
    customerName?: string; // 客户名称
    contactLastTime: Date; // 最后跟进时间
    contactLastContent: string; // 最后跟进内容
    contactNextTime: Date; // 下次联系时间
    ownerUserId: number; // 负责人的用户编号
    ownerUserName?: string; // 负责人的用户名称
    ownerUserDept?: string; // 负责人的部门名称
    mobile: string; // 手机号
    telephone: string; // 电话
    qq: string; // QQ
    wechat: string; // wechat
    email: string; // email
    areaId: number; // 所在地
    areaName?: string; // 所在地名称
    detailAddress: string; // 详细地址
    sex: number; // 性别
    master: boolean; // 是否主联系人
    post: string; // 职务
    parentId: number; // 上级联系人编号
    parentName?: string; // 上级联系人名称
    remark: string; // 备注
    creator: string; // 创建人
    creatorName?: string; // 创建人名称
    createTime: Date; // 创建时间
    updateTime: Date; // 更新时间
  }

  /** 联系人商机关联请求 */
  export interface ContactBusinessReqVO {
    contactId: number;
    businessIds: number[];
  }

  /** 商机联系人关联请求 */
  export interface BusinessContactReqVO {
    businessId: number;
    contactIds: number[];
  }
}

/** 查询联系人列表 */
export function getContactPage(params: PageParam) {
  return requestClient.get<PageResult<CrmContactApi.Contact>>(
    '/crm/contact/page',
    { params },
  );
}

/** 查询联系人列表，基于指定客户 */
export function getContactPageByCustomer(params: PageParam) {
  return requestClient.get<PageResult<CrmContactApi.Contact>>(
    '/crm/contact/page-by-customer',
    { params },
  );
}

/** 查询联系人列表，基于指定商机 */
export function getContactPageByBusiness(params: PageParam) {
  return requestClient.get<PageResult<CrmContactApi.Contact>>(
    '/crm/contact/page-by-business',
    { params },
  );
}

/** 查询联系人详情 */
export function getContact(id: number) {
  return requestClient.get<CrmContactApi.Contact>(`/crm/contact/get?id=${id}`);
}

/** 新增联系人 */
export function createContact(data: CrmContactApi.Contact) {
  return requestClient.post('/crm/contact/create', data);
}

/** 修改联系人 */
export function updateContact(data: CrmContactApi.Contact) {
  return requestClient.put('/crm/contact/update', data);
}

/** 删除联系人 */
export function deleteContact(id: number) {
  return requestClient.delete(`/crm/contact/delete?id=${id}`);
}

/** 导出联系人 */
export function exportContact(params: any) {
  return requestClient.download('/crm/contact/export-excel', { params });
}

/** 获得联系人列表（精简） */
export function getSimpleContactList() {
  return requestClient.get<CrmContactApi.Contact[]>(
    '/crm/contact/simple-all-list',
  );
}

/** 批量新增联系人商机关联 */
export function createContactBusinessList(
  data: CrmContactApi.ContactBusinessReqVO,
) {
  return requestClient.post('/crm/contact/create-business-list', data);
}

/** 批量新增商机联系人关联 */
export function createBusinessContactList(
  data: CrmContactApi.BusinessContactReqVO,
) {
  return requestClient.post('/crm/contact/create-business-list2', data);
}

/** 解除联系人商机关联 */
export function deleteContactBusinessList(
  data: CrmContactApi.ContactBusinessReqVO,
) {
  return requestClient.delete('/crm/contact/delete-business-list', { data });
}

/** 解除商机联系人关联 */
export function deleteBusinessContactList(
  data: CrmContactApi.BusinessContactReqVO,
) {
  return requestClient.delete('/crm/contact/delete-business-list2', { data });
}

/** 联系人转移 */
export function transferContact(data: CrmPermissionApi.BusinessTransferReqVO) {
  return requestClient.put('/crm/contact/transfer', data);
}
