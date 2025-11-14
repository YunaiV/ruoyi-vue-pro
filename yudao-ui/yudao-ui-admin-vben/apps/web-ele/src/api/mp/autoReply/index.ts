import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MpAutoReplyApi {
  /** 自动回复信息 */
  export interface AutoReply {
    id?: number;
    accountId: number;
    type: number;
    keyword: string;
    content: string;
    status: number;
    remark?: string;
    createTime?: Date;
  }
}

/** 查询自动回复列表 */
export function getAutoReplyPage(params: PageParam) {
  return requestClient.get<PageResult<MpAutoReplyApi.AutoReply>>(
    '/mp/auto-reply/page',
    {
      params,
    },
  );
}

/** 查询自动回复详情 */
export function getAutoReply(id: number) {
  return requestClient.get<MpAutoReplyApi.AutoReply>(
    `/mp/auto-reply/get?id=${id}`,
  );
}

/** 新增自动回复 */
export function createAutoReply(data: MpAutoReplyApi.AutoReply) {
  return requestClient.post('/mp/auto-reply/create', data);
}

/** 修改自动回复 */
export function updateAutoReply(data: MpAutoReplyApi.AutoReply) {
  return requestClient.put('/mp/auto-reply/update', data);
}

/** 删除自动回复 */
export function deleteAutoReply(id: number) {
  return requestClient.delete(`/mp/auto-reply/delete?id=${id}`);
}
