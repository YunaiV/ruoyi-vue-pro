import request from '@/utils/request'

// 获得访问令牌分页
export function getAccessTokenPage(query) {
  return request({
    url: '/system/oauth2-token/page',
    method: 'get',
    params: query
  })
}

// 删除访问令牌
export function deleteAccessToken(accessToken) {
  return request({
    url: '/system/oauth2-token/delete?accessToken=' + accessToken,
    method: 'delete'
  })
}
