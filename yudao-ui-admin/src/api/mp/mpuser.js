import request from '@/utils/request'

// 更新公众号粉丝
export function updateUser(data) {
  return request({
    url: '/mp/user/update',
    method: 'put',
    data: data
  })
}

// 获得公众号粉丝
export function getUser(id) {
  return request({
    url: '/mp/user/get?id=' + id,
    method: 'get'
  })
}

// 获得公众号粉丝分页
export function getUserPage(query) {
  return request({
    url: '/mp/user/page',
    method: 'get',
    params: query
  })
}

// 同步公众号粉丝
export function syncUser(accountId) {
  return request({
    url: '/mp/tag/sync?accountId=' + accountId,
    method: 'post'
  })
}
