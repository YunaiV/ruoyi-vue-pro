import { defHttp } from '@/config/axios'
import type { TenantPackageVO } from './types'

// 查询租户套餐列表
export const getTenantPackageTypePageApi = ({ params }) => {
  return defHttp.get<PageResult<TenantPackageVO>>({ url: '/system/tenant-package/page', params })
}

// 获得租户
export const getTenantPackageApi = (id: number) => {
  return defHttp.get<TenantPackageVO>({ url: '/system/tenant-package/get?id=' + id })
}

// 新增租户套餐
export const createTenantPackageTypeApi = (params: TenantPackageVO) => {
  return defHttp.post({ url: '/system/tenant-package/create', params })
}

// 修改租户套餐
export const updateTenantPackageTypeApi = (params: TenantPackageVO) => {
  return defHttp.put({ url: '/system/tenant-package/update', params })
}

// 删除租户套餐
export const deleteTenantPackageTypeApi = (id: number) => {
  return defHttp.delete({ url: '/system/tenant-package/delete?id=' + id })
}
// // 获取租户套餐精简信息列表
export const getTenantPackageList = () => {
  return defHttp.get({
    url: '/system/tenant-package/get-simple-list'
  })
}
