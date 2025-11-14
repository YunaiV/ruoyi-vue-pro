import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MpTagApi {
  /** 标签信息 */
  export interface Tag {
    id?: number;
    accountId: number;
    name: string;
    count?: number;
    createTime?: Date;
  }

  /** 标签分页查询参数 */
  export interface TagPageQuery extends PageParam {
    accountId?: number;
    name?: string;
  }
}

/** 创建公众号标签 */
export function createTag(data: MpTagApi.Tag) {
  return requestClient.post('/mp/tag/create', data);
}

/** 更新公众号标签 */
export function updateTag(data: MpTagApi.Tag) {
  return requestClient.put('/mp/tag/update', data);
}

/** 删除公众号标签 */
export function deleteTag(id: number) {
  return requestClient.delete('/mp/tag/delete', {
    params: { id },
  });
}

/** 获取公众号标签 */
export function getTag(id: number) {
  return requestClient.get<MpTagApi.Tag>('/mp/tag/get', {
    params: { id },
  });
}

/** 获取公众号标签分页 */
export function getTagPage(params: MpTagApi.TagPageQuery) {
  return requestClient.get<PageResult<MpTagApi.Tag>>('/mp/tag/page', {
    params,
  });
}

/** 获取公众号标签精简信息列表 */
export function getSimpleTagList() {
  return requestClient.get<MpTagApi.Tag[]>('/mp/tag/list-all-simple');
}

/** 同步公众号标签 */
export function syncTag(accountId: number) {
  return requestClient.post('/mp/tag/sync', null, {
    params: { accountId },
  });
}
