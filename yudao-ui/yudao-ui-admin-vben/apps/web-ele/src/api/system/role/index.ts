import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemRoleApi {
  /** 角色信息 */
  export interface Role {
    id?: number;
    name: string;
    code: string;
    sort: number;
    status: number;
    type: number;
    dataScope: number;
    dataScopeDeptIds: number[];
    createTime?: Date;
  }
}

/** 查询角色列表 */
export function getRolePage(params: PageParam) {
  return requestClient.get<PageResult<SystemRoleApi.Role>>(
    '/system/role/page',
    { params },
  );
}

/** 查询角色（精简)列表 */
export function getSimpleRoleList() {
  return requestClient.get<SystemRoleApi.Role[]>('/system/role/simple-list');
}

/** 查询角色详情 */
export function getRole(id: number) {
  return requestClient.get<SystemRoleApi.Role>(`/system/role/get?id=${id}`);
}

/** 新增角色 */
export function createRole(data: SystemRoleApi.Role) {
  return requestClient.post('/system/role/create', data);
}

/** 修改角色 */
export function updateRole(data: SystemRoleApi.Role) {
  return requestClient.put('/system/role/update', data);
}

/** 删除角色 */
export function deleteRole(id: number) {
  return requestClient.delete(`/system/role/delete?id=${id}`);
}

/** 批量删除角色 */
export function deleteRoleList(ids: number[]) {
  return requestClient.delete(`/system/role/delete-list?ids=${ids.join(',')}`);
}

/** 导出角色 */
export function exportRole(params: any) {
  return requestClient.download('/system/role/export-excel', {
    params,
  });
}
