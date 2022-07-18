import { defHttp } from '@/config/axios'
import type { TenantVO } from './types'

// 查询租户列表
export const getTenantPageApi = ({ params }) => {
  return defHttp.get<PageResult<TenantVO>>({ url: '/system/tenant/page', params })
}

// 查询租户详情
export const getTenantApi = (id: number) => {
  return defHttp.get<TenantVO>({ url: '/system/tenant/get?id=' + id })
}

// 新增租户
export const createTenantApi = (params: TenantVO) => {
  return defHttp.post({ url: '/system/tenant/create', params })
}

// 修改租户
export const updateTenantApi = (params: TenantVO) => {
  return defHttp.put({ url: '/system/tenant/update', params })
}

// 删除租户
export const deleteTenantApi = (id: number) => {
  return defHttp.delete({ url: '/system/tenant/delete?id=' + id })
}

// 导出租户
export const exportTenantApi = (params) => {
  return defHttp.get({ url: '/system/tenant/export-excel', params, responseType: 'blob' })
}
