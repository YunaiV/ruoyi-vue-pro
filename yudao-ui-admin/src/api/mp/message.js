import request from '@/utils/request'

// 创建粉丝消息表
export function createMessage(data) {
  return request({
    url: '/mp/message/create',
    method: 'post',
    data: data
  })
}

// 获得粉丝消息表
export function getMessage(id) {
  return request({
    url: '/mp/message/get?id=' + id,
    method: 'get'
  })
}

// 获得粉丝消息表 分页
export function getMessagePage(query) {
  return request({
    url: '/mp/message/page',
    method: 'get',
    params: query
  })
}
