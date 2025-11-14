import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MemberTagApi {
  /** 会员标签信息 */
  export interface Tag {
    id?: number;
    name: string;
  }
}

/** 查询会员标签列表 */
export function getMemberTagPage(params: PageParam) {
  return requestClient.get<PageResult<MemberTagApi.Tag>>('/member/tag/page', {
    params,
  });
}

/** 查询会员标签详情 */
export function getMemberTag(id: number) {
  return requestClient.get<MemberTagApi.Tag>(`/member/tag/get?id=${id}`);
}

/** 查询会员标签 - 精简信息列表 */
export function getSimpleTagList() {
  return requestClient.get<MemberTagApi.Tag[]>('/member/tag/list-all-simple');
}

/** 新增会员标签 */
export function createMemberTag(data: MemberTagApi.Tag) {
  return requestClient.post('/member/tag/create', data);
}

/** 修改会员标签 */
export function updateMemberTag(data: MemberTagApi.Tag) {
  return requestClient.put('/member/tag/update', data);
}

/** 删除会员标签 */
export function deleteMemberTag(id: number) {
  return requestClient.delete(`/member/tag/delete?id=${id}`);
}
