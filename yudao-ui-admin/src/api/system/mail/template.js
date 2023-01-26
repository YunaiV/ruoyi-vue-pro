import request from '@/utils/request'

// 创建邮件模版
export function createMailTemplate(data) {
  return request({
    url: '/system/mail-template/create',
    method: 'post',
    data: data
  })
}

// 更新邮件模版
export function updateMailTemplate(data) {
  return request({
    url: '/system/mail-template/update',
    method: 'put',
    data: data
  })
}

// 删除邮件模版
export function deleteMailTemplate(id) {
  return request({
    url: '/system/mail-template/delete?id=' + id,
    method: 'delete'
  })
}

// 获得邮件模版
export function getMailTemplate(id) {
  return request({
    url: '/system/mail-template/get?id=' + id,
    method: 'get'
  })
}

// 获得邮件模版分页
export function getMailTemplatePage(query) {
  return request({
    url: '/system/mail-template/page',
    method: 'get',
    params: query
  })
}

// 发送测试邮件
export function sendMail(data) {
  return request({
    url: '/system/mail-template/send-mail',
    method: 'post',
    data: data
  })
}
