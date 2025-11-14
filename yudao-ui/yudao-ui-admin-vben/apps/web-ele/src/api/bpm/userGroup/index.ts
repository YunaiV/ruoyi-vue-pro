import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace BpmUserGroupApi {
  /** BPM 用户组 */
  export interface UserGroup {
    id: number;
    name: string;
    description: string;
    userIds: number[];
    status: number;
    remark: string;
    createTime: string;
  }
}

/** 查询用户组分页 */
export async function getUserGroupPage(params: PageParam) {
  return requestClient.get<PageResult<BpmUserGroupApi.UserGroup>>(
    '/bpm/user-group/page',
    { params },
  );
}

/** 查询用户组详情 */
export async function getUserGroup(id: number) {
  return requestClient.get<BpmUserGroupApi.UserGroup>(
    `/bpm/user-group/get?id=${id}`,
  );
}

/** 新增用户组 */
export async function createUserGroup(data: BpmUserGroupApi.UserGroup) {
  return requestClient.post<number>('/bpm/user-group/create', data);
}

/** 修改用户组 */
export async function updateUserGroup(data: BpmUserGroupApi.UserGroup) {
  return requestClient.put<boolean>('/bpm/user-group/update', data);
}

/** 删除用户组 */
export async function deleteUserGroup(id: number) {
  return requestClient.delete<boolean>(`/bpm/user-group/delete?id=${id}`);
}

/** 查询用户组列表 */
export async function getUserGroupSimpleList() {
  return requestClient.get<BpmUserGroupApi.UserGroup[]>(
    `/bpm/user-group/simple-list`,
  );
}
