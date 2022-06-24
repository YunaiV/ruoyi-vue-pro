import request from '@/utils/request'

// 创建粉丝标签
export function createWxFansTag(data) {
  return request({
    url: '/wechatMp/fans-tag/create',
    method: 'post',
    data: data
  })
}

// 更新粉丝标签
export function updateWxFansTag(data) {
  return request({
    url: '/wechatMp/fans-tag/update',
    method: 'put',
    data: data
  })
}

// 删除粉丝标签
export function deleteWxFansTag(id) {
  return request({
    url: '/wechatMp/fans-tag/delete?id=' + id,
    method: 'delete'
  })
}

// 获得粉丝标签
export function getWxFansTag(id) {
  return request({
    url: '/wechatMp/fans-tag/get?id=' + id,
    method: 'get'
  })
}

// 获得粉丝标签分页
export function getWxFansTagList(query) {
  return request({
    url: '/wechatMp/fans-tag/list',
    method: 'get',
    params: query
  })
}

// 导出粉丝标签 Excel
export function exportWxFansTagExcel(query) {
  return request({
    url: '/wechatMp/fans-tag/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
