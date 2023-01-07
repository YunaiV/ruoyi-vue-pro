import request from '@/utils/request'

// 创建公众号账号
export function createAccount(data) {
  return request({
    url: '/mp/account/create',
    method: 'post',
    data: data
  })
}

// 更新公众号账号
export function updateAccount(data) {
  return request({
    url: '/mp/account/update',
    method: 'put',
    data: data
  })
}

// 删除公众号账号
export function deleteAccount(id) {
  return request({
    url: '/mp/account/delete?id=' + id,
    method: 'delete'
  })
}

// 获得公众号账号
export function getAccount(id) {
  return request({
    url: '/mp/account/get?id=' + id,
    method: 'get'
  })
}

// 获得公众号账号分页
export function getAccountPage(query) {
  return request({
    url: '/mp/account/page',
    method: 'get',
    params: query
  })
}

// 获取公众号账号精简信息列表
export function getSimpleAccounts() {
  return request({
    url: '/mp/account/list-all-simple',
    method: 'get',
  })
}

// 生成公众号二维码
export function generateAccountQrCode(id) {
  return request({
    url: '/mp/account/generate-qr-code?id=' + id,
    method: 'put'
  })
}

// 清空公众号 API 配额
export function clearAccountQuota(id) {
  return request({
    url: '/mp/account/clear-quota?id=' + id,
    method: 'put'
  })
}
