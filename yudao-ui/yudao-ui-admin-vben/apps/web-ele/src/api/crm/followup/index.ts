import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace CrmFollowUpApi {
  /** 关联商机信息 */
  export interface Business {
    id: number;
    name: string;
  }

  /** 关联联系人信息 */
  export interface Contact {
    id: number;
    name: string;
  }

  /** 跟进记录信息 */
  export interface FollowUpRecord {
    id: number; // 编号
    bizType: number; // 数据类型
    bizId: number; // 数据编号
    type: number; // 跟进类型
    content: string; // 跟进内容
    picUrls: string[]; // 图片
    fileUrls: string[]; // 附件
    nextTime: Date; // 下次联系时间
    businessIds: number[]; // 关联的商机编号数组
    businesses: Business[]; // 关联的商机数组
    contactIds: number[]; // 关联的联系人编号数组
    contacts: Contact[]; // 关联的联系人数组
    creator: string;
    creatorName?: string;
  }
}

/** 查询跟进记录分页 */
export function getFollowUpRecordPage(params: PageParam) {
  return requestClient.get<PageResult<CrmFollowUpApi.FollowUpRecord>>(
    '/crm/follow-up-record/page',
    { params },
  );
}

/** 新增跟进记录 */
export function createFollowUpRecord(data: CrmFollowUpApi.FollowUpRecord) {
  return requestClient.post('/crm/follow-up-record/create', data);
}

/** 删除跟进记录 */
export function deleteFollowUpRecord(id: number) {
  return requestClient.delete(`/crm/follow-up-record/delete?id=${id}`);
}
