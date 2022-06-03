import request from '@/utils/request'

// 创建图文消息模板
export function createWxNewsTemplate(data) {
  return request({
    url: '/wechatMp/wx-news-template/create',
    method: 'post',
    data: data
  })
}

// 更新图文消息模板
export function updateWxNewsTemplate(data) {
  return request({
    url: '/wechatMp/wx-news-template/update',
    method: 'put',
    data: data
  })
}

// 删除图文消息模板
export function deleteWxNewsTemplate(id) {
  return request({
    url: '/wechatMp/wx-news-template/delete?id=' + id,
    method: 'delete'
  })
}

// 获得图文消息模板
export function getWxNewsTemplate(id) {
  return request({
    url: '/wechatMp/wx-news-template/get?id=' + id,
    method: 'get'
  })
}

// 获得图文消息模板分页
export function getWxNewsTemplatePage(query) {
  return request({
    url: '/wechatMp/wx-news-template/page',
    method: 'get',
    params: query
  })
}

// 导出图文消息模板 Excel
export function exportWxNewsTemplateExcel(query) {
  return request({
    url: '/wechatMp/wx-news-template/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
