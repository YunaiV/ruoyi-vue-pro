import request from '@/utils/request'

// 创建公众号账户
export function createAccount(data) {
  return request({
    url: '/wechatMp/account/create',
    method: 'post',
    data: data
  })
}

// 更新公众号账户
export function updateAccount(data) {
  return request({
    url: '/wechatMp/account/update',
    method: 'put',
    data: data
  })
}

// 删除公众号账户
export function deleteAccount(id) {
  return request({
    url: '/wechatMp/account/delete?id=' + id,
    method: 'delete'
  })
}

// 获得公众号账户
export function getAccount(id) {
  return request({
    url: '/wechatMp/account/get?id=' + id,
    method: 'get'
  })
}

// 获得公众号账户分页
export function getAccountPage(query) {
  return request({
    url: '/wechatMp/account/page',
    method: 'get',
    params: query
  })
}

// 导出公众号账户 Excel
export function exportAccountExcel(query) {
  return request({
    url: '/wechatMp/account/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
