import request from '@/utils/request'

// 创建邮箱账号
export function createMailAccount(data) {
  return request({
    url: '/system/mail-account/create',
    method: 'post',
    data: data
  })
}

// 更新邮箱账号
export function updateMailAccount(data) {
  return request({
    url: '/system/mail-account/update',
    method: 'put',
    data: data
  })
}

// 删除邮箱账号
export function deleteMailAccount(id) {
  return request({
    url: '/system/mail-account/delete?id=' + id,
    method: 'delete'
  })
}

// 获得邮箱账号
export function getMailAccount(id) {
  return request({
    url: '/system/mail-account/get?id=' + id,
    method: 'get'
  })
}

// 获得邮箱账号分页
export function getMailAccountPage(query) {
  return request({
    url: '/system/mail-account/page',
    method: 'get',
    params: query
  })
}

// 获取邮箱账号的精简信息列表
export function getSimpleMailAccountList() {
  return request({
    url: '/system/mail-account/list-all-simple',
    method: 'get',
  })
}
