import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemNoticeApi {
  /** 公告信息 */
  export interface Notice {
    id?: number;
    title: string;
    type: number;
    content: string;
    status: number;
    remark: string;
    creator?: string;
    createTime?: Date;
  }
}

/** 查询公告列表 */
export function getNoticePage(params: PageParam) {
  return requestClient.get<PageResult<SystemNoticeApi.Notice>>(
    '/system/notice/page',
    { params },
  );
}

/** 查询公告详情 */
export function getNotice(id: number) {
  return requestClient.get<SystemNoticeApi.Notice>(
    `/system/notice/get?id=${id}`,
  );
}

/** 新增公告 */
export function createNotice(data: SystemNoticeApi.Notice) {
  return requestClient.post('/system/notice/create', data);
}

/** 修改公告 */
export function updateNotice(data: SystemNoticeApi.Notice) {
  return requestClient.put('/system/notice/update', data);
}

/** 删除公告 */
export function deleteNotice(id: number) {
  return requestClient.delete(`/system/notice/delete?id=${id}`);
}

/** 批量删除公告 */
export function deleteNoticeList(ids: number[]) {
  return requestClient.delete(
    `/system/notice/delete-list?ids=${ids.join(',')}`,
  );
}

/** 推送公告 */
export function pushNotice(id: number) {
  return requestClient.post(`/system/notice/push?id=${id}`);
}
