import request from '@/utils/request'

// 创建crm联系人
export function createContact(data) {
  return request({
    url: '/crm/contact/create',
    method: 'post',
    data: data
  })
}

// 更新crm联系人
export function updateContact(data) {
  return request({
    url: '/crm/contact/update',
    method: 'put',
    data: data
  })
}

// 删除crm联系人
export function deleteContact(id) {
  return request({
    url: '/crm/contact/delete?id=' + id,
    method: 'delete'
  })
}

// 获得crm联系人
export function getContact(id) {
  return request({
    url: '/crm/contact/get?id=' + id,
    method: 'get'
  })
}

// 获得crm联系人分页
export function getContactPage(query) {
  return request({
    url: '/crm/contact/page',
    method: 'get',
    params: query
  })
}

// 导出crm联系人 Excel
export function exportContactExcel(query) {
  return request({
    url: '/crm/contact/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
