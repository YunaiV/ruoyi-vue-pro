import request from '@/utils/request'

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
