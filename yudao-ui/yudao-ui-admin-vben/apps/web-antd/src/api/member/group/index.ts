import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MemberGroupApi {
  /** 用户分组信息 */
  export interface Group {
    id?: number;
    name: string;
    remark: string;
    status: number;
  }
}

/** 查询用户分组列表 */
export function getGroupPage(params: PageParam) {
  return requestClient.get<PageResult<MemberGroupApi.Group>>(
    '/member/group/page',
    {
      params,
    },
  );
}

/** 查询用户分组详情 */
export function getGroup(id: number) {
  return requestClient.get<MemberGroupApi.Group>(`/member/group/get?id=${id}`);
}

/** 新增用户分组 */
export function createGroup(data: MemberGroupApi.Group) {
  return requestClient.post('/member/group/create', data);
}

/** 查询用户分组 - 精简信息列表 */
export function getSimpleGroupList() {
  return requestClient.get<MemberGroupApi.Group[]>(
    '/member/group/list-all-simple',
  );
}

/** 修改用户分组 */
export function updateGroup(data: MemberGroupApi.Group) {
  return requestClient.put('/member/group/update', data);
}

/** 删除用户分组 */
export function deleteGroup(id: number) {
  return requestClient.delete(`/member/group/delete?id=${id}`);
}
