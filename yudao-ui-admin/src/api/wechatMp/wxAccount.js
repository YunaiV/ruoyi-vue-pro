import request from '@/utils/request'

// 创建公众号账户
export function createWxAccount(data) {
  return request({
    url: '/wechatMp/wx-account/create',
    method: 'post',
    data: data
  })
}

// 更新公众号账户
export function updateWxAccount(data) {
  return request({
    url: '/wechatMp/wx-account/update',
    method: 'put',
    data: data
  })
}

// 删除公众号账户
export function deleteWxAccount(id) {
  return request({
    url: '/wechatMp/wx-account/delete?id=' + id,
    method: 'delete'
  })
}

// 获得公众号账户
export function getWxAccount(id) {
  return request({
    url: '/wechatMp/wx-account/get?id=' + id,
    method: 'get'
  })
}

// 获得公众号账户分页
export function getWxAccountPage(query) {
  return request({
    url: '/wechatMp/wx-account/page',
    method: 'get',
    params: query
  })
}

// 导出公众号账户 Excel
export function exportWxAccountExcel(query) {
  return request({
    url: '/wechatMp/wx-account/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
