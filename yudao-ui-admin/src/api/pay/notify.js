import request from '@/utils/request'

// 获得支付通知明细
export function getNotifyTaskDetail(id) {
  return request({
    url: '/pay/notify/get-detail?id=' + id,
    method: 'get'
  })
}

// 获得支付通知分页
export function getNotifyTaskPage(query) {
  return request({
    url: '/pay/notify/page',
    method: 'get',
    params: query
  })
}
