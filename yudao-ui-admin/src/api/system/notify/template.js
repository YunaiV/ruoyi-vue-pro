import request from '@/utils/request'

// 创建站内信模板
export function createNotifyTemplate(data) {
  return request({
    url: '/system/notify-template/create',
    method: 'post',
    data: data
  })
}

// 更新站内信模板
export function updateNotifyTemplate(data) {
  return request({
    url: '/system/notify-template/update',
    method: 'put',
    data: data
  })
}

// 删除站内信模板
export function deleteNotifyTemplate(id) {
  return request({
    url: '/system/notify-template/delete?id=' + id,
    method: 'delete'
  })
}

// 获得站内信模板
export function getNotifyTemplate(id) {
  return request({
    url: '/system/notify-template/get?id=' + id,
    method: 'get'
  })
}

// 获得站内信模板分页
export function getNotifyTemplatePage(query) {
  return request({
    url: '/system/notify-template/page',
    method: 'get',
    params: query
  })
}

// 创建站内信模板
export function sendNotify(data) {
  return request({
    url: '/system/notify-template/send-notify',
    method: 'post',
    data: data
  })
}

// 导出站内信模板 Excel
export function exportNotifyTemplateExcel(query) {
  return request({
    url: '/system/notify-template/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

