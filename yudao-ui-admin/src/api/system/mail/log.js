import request from '@/utils/request'

// 获得邮件日志
export function getMailLog(id) {
  return request({
    url: '/system/mail-log/get?id=' + id,
    method: 'get'
  })
}

// 获得邮件日志分页
export function getMailLogPage(query) {
  return request({
    url: '/system/mail-log/page',
    method: 'get',
    params: query
  })
}
