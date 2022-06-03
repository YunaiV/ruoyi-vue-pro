import request from '@/utils/request'

// 创建回复关键字
export function createWxReceiveText(data) {
  return request({
    url: '/wechatMp/wx-receive-text/create',
    method: 'post',
    data: data
  })
}

// 更新回复关键字
export function updateWxReceiveText(data) {
  return request({
    url: '/wechatMp/wx-receive-text/update',
    method: 'put',
    data: data
  })
}

// 删除回复关键字
export function deleteWxReceiveText(id) {
  return request({
    url: '/wechatMp/wx-receive-text/delete?id=' + id,
    method: 'delete'
  })
}

// 获得回复关键字
export function getWxReceiveText(id) {
  return request({
    url: '/wechatMp/wx-receive-text/get?id=' + id,
    method: 'get'
  })
}

// 获得回复关键字分页
export function getWxReceiveTextPage(query) {
  return request({
    url: '/wechatMp/wx-receive-text/page',
    method: 'get',
    params: query
  })
}

// 导出回复关键字 Excel
export function exportWxReceiveTextExcel(query) {
  return request({
    url: '/wechatMp/wx-receive-text/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
