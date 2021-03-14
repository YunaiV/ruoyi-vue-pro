import request from '@/utils/request'

// 查询在线用户列表
export function list(query) {
  return request({
    url: '/system/user-session/page',
    method: 'get',
    params: query
  })
}

// 强退用户
export function forceLogout(tokenId) {
  return request({
    url: '/system/user-session/delete?id=' + tokenId,
    method: 'delete'
  })
}
