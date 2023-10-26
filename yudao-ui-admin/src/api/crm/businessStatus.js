import request from '@/utils/request'

// 创建商机状态
export function createBusinessStatus(data) {
  return request({
    url: '/crm/business-status/create',
    method: 'post',
    data: data
  })
}

// 更新商机状态
export function updateBusinessStatus(data) {
  return request({
    url: '/crm/business-status/update',
    method: 'put',
    data: data
  })
}

// 删除商机状态
export function deleteBusinessStatus(id) {
  return request({
    url: '/crm/business-status/delete?id=' + id,
    method: 'delete'
  })
}

// 获得商机状态
export function getBusinessStatus(id) {
  return request({
    url: '/crm/business-status/get?id=' + id,
    method: 'get'
  })
}

// 获得商机状态分页
export function getBusinessStatusPage(query) {
  return request({
    url: '/crm/business-status/page',
    method: 'get',
    params: query
  })
}

// 导出商机状态 Excel
export function exportBusinessStatusExcel(query) {
  return request({
    url: '/crm/business-status/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 根据类型ID获取商机状态信息列表
export function getBusinessStatusListByTypeId(typeId) {
  return request({
    url: '/crm/business-status/get-simple-list?typeId=' + typeId,
    method: 'get'
  })
}

// 获取商机状态信息列表
export function getBusinessStatusList() {
  return request({
    url: '/crm/business-status/get-all-list',
    method: 'get'
  })
}
