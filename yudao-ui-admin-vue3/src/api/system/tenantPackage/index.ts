import request from '@/config/axios'

export interface TenantPackageVO {
  id: number
  name: string
  status: number
  remark: string
  creator: string
  updater: string
  updateTime: string
  menuIds: number[]
  createTime: Date
}

export interface TenantPackagePageReqVO extends PageParam {
  name?: string
  status?: number
  remark?: string
  createTime?: Date[]
}

// 查询租户套餐列表
export const getTenantPackageTypePageApi = (params: TenantPackagePageReqVO) => {
  return request.get({ url: '/system/tenant-package/page', params })
}

// 获得租户
export const getTenantPackageApi = (id: number) => {
  return request.get({ url: '/system/tenant-package/get?id=' + id })
}

// 新增租户套餐
export const createTenantPackageTypeApi = (data: TenantPackageVO) => {
  return request.post({ url: '/system/tenant-package/create', data })
}

// 修改租户套餐
export const updateTenantPackageTypeApi = (data: TenantPackageVO) => {
  return request.put({ url: '/system/tenant-package/update', data })
}

// 删除租户套餐
export const deleteTenantPackageTypeApi = (id: number) => {
  return request.delete({ url: '/system/tenant-package/delete?id=' + id })
}
// 获取租户套餐精简信息列表
export const getTenantPackageList = () => {
  return request.get({ url: '/system/tenant-package/get-simple-list' })
}
