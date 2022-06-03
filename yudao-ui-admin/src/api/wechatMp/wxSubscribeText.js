import request from '@/utils/request'

// 创建关注欢迎语
export function createWxSubscribeText(data) {
  return request({
    url: '/wechatMp/wx-subscribe-text/create',
    method: 'post',
    data: data
  })
}

// 更新关注欢迎语
export function updateWxSubscribeText(data) {
  return request({
    url: '/wechatMp/wx-subscribe-text/update',
    method: 'put',
    data: data
  })
}

// 删除关注欢迎语
export function deleteWxSubscribeText(id) {
  return request({
    url: '/wechatMp/wx-subscribe-text/delete?id=' + id,
    method: 'delete'
  })
}

// 获得关注欢迎语
export function getWxSubscribeText(id) {
  return request({
    url: '/wechatMp/wx-subscribe-text/get?id=' + id,
    method: 'get'
  })
}

// 获得关注欢迎语分页
export function getWxSubscribeTextPage(query) {
  return request({
    url: '/wechatMp/wx-subscribe-text/page',
    method: 'get',
    params: query
  })
}

// 导出关注欢迎语 Excel
export function exportWxSubscribeTextExcel(query) {
  return request({
    url: '/wechatMp/wx-subscribe-text/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
