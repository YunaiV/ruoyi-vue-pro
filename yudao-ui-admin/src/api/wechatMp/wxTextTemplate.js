import request from '@/utils/request'

// 创建文本模板
export function createWxTextTemplate(data) {
  return request({
    url: '/wechatMp/wx-text-template/create',
    method: 'post',
    data: data
  })
}

// 更新文本模板
export function updateWxTextTemplate(data) {
  return request({
    url: '/wechatMp/wx-text-template/update',
    method: 'put',
    data: data
  })
}

// 删除文本模板
export function deleteWxTextTemplate(id) {
  return request({
    url: '/wechatMp/wx-text-template/delete?id=' + id,
    method: 'delete'
  })
}

// 获得文本模板
export function getWxTextTemplate(id) {
  return request({
    url: '/wechatMp/wx-text-template/get?id=' + id,
    method: 'get'
  })
}

// 获得文本模板分页
export function getWxTextTemplatePage(query) {
  return request({
    url: '/wechatMp/wx-text-template/page',
    method: 'get',
    params: query
  })
}

// 导出文本模板 Excel
export function exportWxTextTemplateExcel(query) {
  return request({
    url: '/wechatMp/wx-text-template/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
