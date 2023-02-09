import request from '@/config/axios'

// 创建公众号的自动回复
export function createAutoReply(data) {
  return request.post({
    url: '/mp/auto-reply/create',
    data: data
  })
}

// 更新公众号的自动回复
export function updateAutoReply(data) {
  return request.put({
    url: '/mp/auto-reply/update',
    data: data
  })
}

// 删除公众号的自动回复
export function deleteAutoReply(id) {
  return request.delete({
    url: '/mp/auto-reply/delete?id=' + id
  })
}

// 获得公众号的自动回复
export function getAutoReply(id) {
  return request.get({
    url: '/mp/auto-reply/get?id=' + id
  })
}

// 获得公众号的自动回复分页
export function getAutoReplyPage(query) {
  return request.get({
    url: '/mp/auto-reply/page',
    params: query
  })
}
