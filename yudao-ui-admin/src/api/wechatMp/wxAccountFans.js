import request from '@/utils/request'

// 创建微信公众号粉丝
export function createWxAccountFans(data) {
  return request({
    url: '/wechatMp/wx-account-fans/create',
    method: 'post',
    data: data
  })
}

// 更新微信公众号粉丝
export function updateWxAccountFans(data) {
  return request({
    url: '/wechatMp/wx-account-fans/update',
    method: 'put',
    data: data
  })
}

// 删除微信公众号粉丝
export function deleteWxAccountFans(id) {
  return request({
    url: '/wechatMp/wx-account-fans/delete?id=' + id,
    method: 'delete'
  })
}

// 获得微信公众号粉丝
export function getWxAccountFans(id) {
  return request({
    url: '/wechatMp/wx-account-fans/get?id=' + id,
    method: 'get'
  })
}

// 获得微信公众号粉丝分页
export function getWxAccountFansPage(query) {
  return request({
    url: '/wechatMp/wx-account-fans/page',
    method: 'get',
    params: query
  })
}

// 导出微信公众号粉丝 Excel
export function exportWxAccountFansExcel(query) {
  return request({
    url: '/wechatMp/wx-account-fans/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
