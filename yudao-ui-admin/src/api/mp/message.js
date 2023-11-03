import request from '@/utils/request'

// 获得公众号消息分页
export function getMessagePage(query) {
  return request({
    url: '/mp/message/page',
    method: 'get',
    params: query
  })
}

// 给粉丝发送消息
export function sendMessage(data) {
  return request({
    url: '/mp/message/send',
    method: 'post',
    data: data
  })
}
