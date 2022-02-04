import request from '@/utils/request'

// 创建支付订单
export function createOrder(data) {
  return request({
    url: '/pay/order/create',
    method: 'post',
    data: data
  })
}

// 更新支付订单
export function updateOrder(data) {
  return request({
    url: '/pay/order/update',
    method: 'put',
    data: data
  })
}

// 删除支付订单
export function deleteOrder(id) {
  return request({
    url: '/pay/order/delete?id=' + id,
    method: 'delete'
  })
}

// 获得支付订单
export function getOrder(id) {
  return request({
    url: '/pay/order/get?id=' + id,
    method: 'get'
  })
}

// 获得支付订单分页
export function getOrderPage(query) {
  return request({
    url: '/pay/order/page',
    method: 'get',
    params: query
  })
}

// 导出支付订单 Excel
export function exportOrderExcel(query) {
  return request({
    url: '/pay/order/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
