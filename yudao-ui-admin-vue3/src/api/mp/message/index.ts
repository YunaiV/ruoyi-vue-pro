import request from '@/config/axios'

// 获得公众号消息分页
export function getMessagePage(query) {
  return request.get({
    url: '/mp/message/page',
    params: query
  })
}

// 给粉丝发送消息
export function sendMessage(data) {
  return request.post({
    url: '/mp/message/send',
    data: data
  })
}
