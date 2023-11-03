import request from '@/utils/request'

// 创建 OAuth2 客户端
export function createOAuth2Client(data) {
  return request({
    url: '/system/oauth2-client/create',
    method: 'post',
    data: data
  })
}

// 更新 OAuth2 客户端
export function updateOAuth2Client(data) {
  return request({
    url: '/system/oauth2-client/update',
    method: 'put',
    data: data
  })
}

// 删除 OAuth2 客户端
export function deleteOAuth2Client(id) {
  return request({
    url: '/system/oauth2-client/delete?id=' + id,
    method: 'delete'
  })
}

// 获得 OAuth2 客户端
export function getOAuth2Client(id) {
  return request({
    url: '/system/oauth2-client/get?id=' + id,
    method: 'get'
  })
}

// 获得 OAuth2 客户端分页
export function getOAuth2ClientPage(query) {
  return request({
    url: '/system/oauth2-client/page',
    method: 'get',
    params: query
  })
}
