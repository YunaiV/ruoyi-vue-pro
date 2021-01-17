import request from '@/utils/request'

// 查询操作日志列表
export function listOperateLog(query) {
  return request({
    url: '/system/operate-log/page',
    method: 'get',
    params: query
  })
}

// 导出操作日志
export function exportOperateLog(query) {
  return request({
    url: '/system/operate-log/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
