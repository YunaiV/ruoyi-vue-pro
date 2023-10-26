import request from '@/utils/request'

// 创建商机状态类型
export function createBusinessStatusType(data) {
  return request({
    url: '/crm/business-status-type/create',
    method: 'post',
    data: data
  })
}

// 更新商机状态类型
export function updateBusinessStatusType(data) {
  return request({
    url: '/crm/business-status-type/update',
    method: 'put',
    data: data
  })
}

// 删除商机状态类型
export function deleteBusinessStatusType(id) {
  return request({
    url: '/crm/business-status-type/delete?id=' + id,
    method: 'delete'
  })
}

// 获得商机状态类型
export function getBusinessStatusType(id) {
  return request({
    url: '/crm/business-status-type/get?id=' + id,
    method: 'get'
  })
}

// 获得商机状态类型分页
export function getBusinessStatusTypePage(query) {
  return request({
    url: '/crm/business-status-type/page',
    method: 'get',
    params: query
  })
}

// 导出商机状态类型 Excel
export function exportBusinessStatusTypeExcel(query) {
  return request({
    url: '/crm/business-status-type/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 获取商机状态类型信息列表
export function getBusinessStatusTypeList() {
  return request({
    url: '/crm/business-status-type/get-simple-list',
    method: 'get'
  })
}
