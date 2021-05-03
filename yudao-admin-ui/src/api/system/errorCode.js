import request from '@/utils/request'

// 创建错误码
export function createErrorCode(data) {
  return request({
    url: '/system/error-code/create',
    method: 'post',
    data: data
  })
}

// 更新错误码
export function updateErrorCode(data) {
  return request({
    url: '/system/error-code/update',
    method: 'put',
    data: data
  })
}

// 删除错误码
export function deleteErrorCode(id) {
  return request({
    url: '/system/error-code/delete?id=' + id,
    method: 'delete'
  })
}

// 获得错误码
export function getErrorCode(id) {
  return request({
    url: '/system/error-code/get?id=' + id,
    method: 'get'
  })
}

// 获得错误码分页
export function getErrorCodePage(query) {
  return request({
    url: '/system/error-code/page',
    method: 'get',
    params: query
  })
}

// 导出错误码 Excel
export function exportErrorCodeExcel(query) {
  return request({
    url: '/system/error-code/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
