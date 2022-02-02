import request from '@/utils/request'

// 获得短信日志分页
export function getSmsLogPage(query) {
  return request({
    url: '/system/sms-log/page',
    method: 'get',
    params: query
  })
}

// 导出短信日志 Excel
export function exportSmsLogExcel(query) {
  return request({
    url: '/system/sms-log/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
