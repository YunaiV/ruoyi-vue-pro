import type { PageParam, PageResult } from '@vben/request';

import type { CrmPermissionApi } from '#/api/crm/permission';

import { requestClient } from '#/api/request';

export namespace CrmClueApi {
  /** 线索信息 */
  export interface Clue {
    id: number; // 编号
    name: string; // 线索名称
    followUpStatus: boolean; // 跟进状态
    contactLastTime: Date; // 最后跟进时间
    contactLastContent: string; // 最后跟进内容
    contactNextTime: Date; // 下次联系时间
    ownerUserId: number; // 负责人的用户编号
    ownerUserName?: string; // 负责人的用户名称
    ownerUserDept?: string; // 负责人的部门名称
    transformStatus: boolean; // 转化状态
    customerId: number; // 客户编号
    customerName?: string; // 客户名称
    mobile: string; // 手机号
    telephone: string; // 电话
    qq: string; // QQ
    wechat: string; // wechat
    email: string; // email
    areaId: number; // 所在地
    areaName?: string; // 所在地名称
    detailAddress: string; // 详细地址
    industryId: number; // 所属行业
    level: number; // 客户等级
    source: number; // 客户来源
    remark: string; // 备注
    creator: string; // 创建人
    creatorName?: string; // 创建人名称
    createTime: Date; // 创建时间
    updateTime: Date; // 更新时间
  }
}

/** 查询线索列表 */
export function getCluePage(params: PageParam) {
  return requestClient.get<PageResult<CrmClueApi.Clue>>('/crm/clue/page', {
    params,
  });
}

/** 查询线索详情 */
export function getClue(id: number) {
  return requestClient.get<CrmClueApi.Clue>(`/crm/clue/get?id=${id}`);
}

/** 新增线索 */
export function createClue(data: CrmClueApi.Clue) {
  return requestClient.post('/crm/clue/create', data);
}

/** 修改线索 */
export function updateClue(data: CrmClueApi.Clue) {
  return requestClient.put('/crm/clue/update', data);
}

/** 删除线索 */
export function deleteClue(id: number) {
  return requestClient.delete(`/crm/clue/delete?id=${id}`);
}

/** 导出线索 */
export function exportClue(params: any) {
  return requestClient.download('/crm/clue/export-excel', { params });
}

/** 线索转移 */
export function transferClue(data: CrmPermissionApi.BusinessTransferReqVO) {
  return requestClient.put('/crm/clue/transfer', data);
}

/** 线索转化为客户 */
export function transformClue(id: number) {
  return requestClient.put(`/crm/clue/transform?id=${id}`);
}

/** 获得分配给我的、待跟进的线索数量 */
export function getFollowClueCount() {
  return requestClient.get<number>('/crm/clue/follow-count');
}
