import request from '@/utils/request'

// 创建商机
export function createBusiness(data) {
  return request({
    url: '/crm/business/create',
    method: 'post',
    data: data
  })
}

// 更新商机
export function updateBusiness(data) {
  return request({
    url: '/crm/business/update',
    method: 'put',
    data: data
  })
}

// 删除商机
export function deleteBusiness(id) {
  return request({
    url: '/crm/business/delete?id=' + id,
    method: 'delete'
  })
}

// 获得商机
export function getBusiness(id) {
  return request({
    url: '/crm/business/get?id=' + id,
    method: 'get'
  })
}

// 获得商机分页
export function getBusinessPage(query) {
  return request({
    url: '/crm/business/page',
    method: 'get',
    params: query
  })
}

// 导出商机 Excel
export function exportBusinessExcel(query) {
  return request({
    url: '/crm/business/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
