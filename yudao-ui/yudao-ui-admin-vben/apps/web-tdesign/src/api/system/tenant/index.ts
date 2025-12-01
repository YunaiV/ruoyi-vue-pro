import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemTenantApi {
  /** 租户信息 */
  export interface Tenant {
    id?: number;
    name: string;
    packageId: number;
    contactName: string;
    contactMobile: string;
    accountCount: number;
    expireTime: Date;
    websites: string[];
    status: number;
  }
}

/** 租户列表 */
export function getTenantPage(params: PageParam) {
  return requestClient.get<PageResult<SystemTenantApi.Tenant>>(
    '/system/tenant/page',
    { params },
  );
}

/** 获取租户精简信息列表 */
export function getSimpleTenantList() {
  return requestClient.get<SystemTenantApi.Tenant[]>(
    '/system/tenant/simple-list',
  );
}

/** 查询租户详情 */
export function getTenant(id: number) {
  return requestClient.get<SystemTenantApi.Tenant>(
    `/system/tenant/get?id=${id}`,
  );
}

/** 获取租户精简信息列表 */
export function getTenantList() {
  return requestClient.get<SystemTenantApi.Tenant[]>(
    '/system/tenant/simple-list',
  );
}

/** 新增租户 */
export function createTenant(data: SystemTenantApi.Tenant) {
  return requestClient.post('/system/tenant/create', data);
}

/** 修改租户 */
export function updateTenant(data: SystemTenantApi.Tenant) {
  return requestClient.put('/system/tenant/update', data);
}

/** 删除租户 */
export function deleteTenant(id: number) {
  return requestClient.delete(`/system/tenant/delete?id=${id}`);
}

/** 批量删除租户 */
export function deleteTenantList(ids: number[]) {
  return requestClient.delete(
    `/system/tenant/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出租户 */
export function exportTenant(params: any) {
  return requestClient.download('/system/tenant/export-excel', {
    params,
  });
}
