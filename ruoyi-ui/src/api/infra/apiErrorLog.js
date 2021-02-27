import request from '@/utils/request'

// 更新 API 错误日志的处理状态
export function updateApiErrorLogProcess(id, processStatus) {
  return request({
    url: '/infra/api-error-log/update-status?id=' + id + '&processStatus=' + processStatus,
    method: 'put',
  })
}

// 获得API 错误日志分页
export function getApiErrorLogPage(query) {
  return request({
    url: '/infra/api-error-log/page',
    method: 'get',
    params: query
  })
}

// 导出API 错误日志 Excel
export function exportApiErrorLogExcel(query) {
  return request({
    url: '/infra/api-error-log/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
