import request from '@/utils/request'

// 创建粉丝标签关联
export function createWxAccountFansTag(data) {
  return request({
    url: '/wechatMp/wx-account-fans-tag/create',
    method: 'post',
    data: data
  })
}

// 更新粉丝标签关联
export function updateWxAccountFansTag(data) {
  return request({
    url: '/wechatMp/wx-account-fans-tag/update',
    method: 'put',
    data: data
  })
}

// 删除粉丝标签关联
export function deleteWxAccountFansTag(id) {
  return request({
    url: '/wechatMp/wx-account-fans-tag/delete?id=' + id,
    method: 'delete'
  })
}

// 获得粉丝标签关联
export function getWxAccountFansTag(id) {
  return request({
    url: '/wechatMp/wx-account-fans-tag/get?id=' + id,
    method: 'get'
  })
}

// 获得粉丝标签关联分页
export function getWxAccountFansTagPage(query) {
  return request({
    url: '/wechatMp/wx-account-fans-tag/page',
    method: 'get',
    params: query
  })
}

// 导出粉丝标签关联 Excel
export function exportWxAccountFansTagExcel(query) {
  return request({
    url: '/wechatMp/wx-account-fans-tag/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
