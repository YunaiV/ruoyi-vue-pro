import request from '@/utils/request'

// 创建支付商户信息
export function createMerchant(data) {
  return request({
    url: '/pay/merchant/create',
    method: 'post',
    data: data
  })
}

// 更新支付商户信息
export function updateMerchant(data) {
  return request({
    url: '/pay/merchant/update',
    method: 'put',
    data: data
  })
}

// 支付商户状态修改
export function changeMerchantStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
    url: '/pay/merchant/update-status',
    method: 'put',
    data: data
  })
}

// 删除支付商户信息
export function deleteMerchant(id) {
  return request({
    url: '/pay/merchant/delete?id=' + id,
    method: 'delete'
  })
}

// 获得支付商户信息
export function getMerchant(id) {
  return request({
    url: '/pay/merchant/get?id=' + id,
    method: 'get'
  })
}
// 根据商户名称搜索商户列表
export function getMerchantListByName(name) {
  return request({
    url: '/pay/merchant/list-by-name',
    params:{
      name:name
    },
    method: 'get'
  })
}

// 获得支付商户信息分页
export function getMerchantPage(query) {
  return request({
    url: '/pay/merchant/page',
    method: 'get',
    params: query
  })
}

// 导出支付商户信息 Excel
export function exportMerchantExcel(query) {
  return request({
    url: '/pay/merchant/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
