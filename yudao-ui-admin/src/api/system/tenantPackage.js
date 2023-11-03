import request from '@/utils/request'

// 创建租户套餐
export function createTenantPackage(data) {
  return request({
    url: '/system/tenant-package/create',
    method: 'post',
    data: data
  })
}

// 更新租户套餐
export function updateTenantPackage(data) {
  return request({
    url: '/system/tenant-package/update',
    method: 'put',
    data: data
  })
}

// 删除租户套餐
export function deleteTenantPackage(id) {
  return request({
    url: '/system/tenant-package/delete?id=' + id,
    method: 'delete'
  })
}

// 获得租户套餐
export function getTenantPackage(id) {
  return request({
    url: '/system/tenant-package/get?id=' + id,
    method: 'get'
  })
}

// 获得租户套餐分页
export function getTenantPackagePage(query) {
  return request({
    url: '/system/tenant-package/page',
    method: 'get',
    params: query
  })
}

// 获取租户套餐精简信息列表
export function getTenantPackageList() {
  return request({
    url: '/system/tenant-package/get-simple-list',
    method: 'get'
  })
}
