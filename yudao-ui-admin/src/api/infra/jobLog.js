import request from '@/utils/request'

// 获得定时任务
export function getJobLog(id) {
  return request({
    url: '/infra/job-log/get?id=' + id,
    method: 'get'
  })
}

// 获得定时任务分页
export function getJobLogPage(query) {
  return request({
    url: '/infra/job-log/page',
    method: 'get',
    params: query
  })
}

// 导出定时任务 Excel
export function exportJobLogExcel(query) {
  return request({
    url: '/infra/job-log/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
