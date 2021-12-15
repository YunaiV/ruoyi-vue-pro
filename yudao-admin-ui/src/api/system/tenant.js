import request from '@/utils/request'

// 使用租户名，获得租户编号
export function getTenantIdByName(name) {
  return request({
    url: '/system/tenant/get-id-by-name',
    method: 'get',
    params: {
      name
    }
  })
}

// 创建租户
export function createTenant(data) {
  return request({
    url: '/system/tenant/create',
    method: 'post',
    data: data
  })
}

// 更新租户
export function updateTenant(data) {
  return request({
    url: '/system/tenant/update',
    method: 'put',
    data: data
  })
}

// 删除租户
export function deleteTenant(id) {
  return request({
    url: '/system/tenant/delete?id=' + id,
    method: 'delete'
  })
}

// 获得租户
export function getTenant(id) {
  return request({
    url: '/system/tenant/get?id=' + id,
    method: 'get'
  })
}

// 获得租户分页
export function getTenantPage(query) {
  return request({
    url: '/system/tenant/page',
    method: 'get',
    params: query
  })
}

// 导出租户 Excel
export function exportTenantExcel(query) {
  return request({
    url: '/system/tenant/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
