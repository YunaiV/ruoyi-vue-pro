import request from '@/utils/request'

// 创建规格名称
export function createProperty(data) {
  return request({
    url: '/product/property/create',
    method: 'post',
    data: data
  })
}

// 更新规格名称
export function updateProperty(data) {
  return request({
    url: '/product/property/update',
    method: 'put',
    data: data
  })
}

// 删除规格名称
export function deleteProperty(id) {
  return request({
    url: '/product/property/delete?id=' + id,
    method: 'delete'
  })
}

// 获得规格名称
export function getProperty(id) {
  return request({
    url: '/product/property/get?id=' + id,
    method: 'get'
  })
}

// 获得规格名称分页
export function getPropertyPage(query) {
  return request({
    url: '/product/property/page',
    method: 'get',
    params: query
  })
}

// 导出规格名称 Excel
export function exportPropertyExcel(query) {
  return request({
    url: '/product/property/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
