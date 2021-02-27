import request from '@/utils/request'

// 创建API 访问日志
export function createApiAccessLog(data) {
  return request({
    url: '/infra/api-access-log/create',
    method: 'post',
    data: data
  })
}

// 更新API 访问日志
export function updateApiAccessLog(data) {
  return request({
    url: '/infra/api-access-log/update',
    method: 'put',
    data: data
  })
}


// 删除API 访问日志
export function deleteApiAccessLog(id) {
  return request({
    url: '/infra/api-access-log/delete?id=' + id,
    method: 'delete'
  })
}

// 获得API 访问日志
export function getApiAccessLog(id) {
  return request({
    url: '/infra/api-access-log/get?id=' + id,
    method: 'get'
  })
}

// 获得API 访问日志分页
export function getApiAccessLogPage(query) {
  return request({
    url: '/infra/api-access-log/page',
    method: 'get',
    params: query
  })
}

// 导出API 访问日志 Excel
export function exportApiAccessLogExcel(query) {
  return request({
    url: '/infra/api-access-log/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
