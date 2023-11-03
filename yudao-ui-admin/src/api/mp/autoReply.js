import request from '@/utils/request'

// 创建公众号的自动回复
export function createAutoReply(data) {
  return request({
    url: '/mp/auto-reply/create',
    method: 'post',
    data: data
  })
}

// 更新公众号的自动回复
export function updateAutoReply(data) {
  return request({
    url: '/mp/auto-reply/update',
    method: 'put',
    data: data
  })
}

// 删除公众号的自动回复
export function deleteAutoReply(id) {
  return request({
    url: '/mp/auto-reply/delete?id=' + id,
    method: 'delete'
  })
}

// 获得公众号的自动回复
export function getAutoReply(id) {
  return request({
    url: '/mp/auto-reply/get?id=' + id,
    method: 'get'
  })
}

// 获得公众号的自动回复分页
export function getAutoReplyPage(query) {
  return request({
    url: '/mp/auto-reply/page',
    method: 'get',
    params: query
  })
}
