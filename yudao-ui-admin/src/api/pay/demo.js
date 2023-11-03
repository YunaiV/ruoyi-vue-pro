import request from '@/utils/request'

// 创建示例订单
export function createDemoOrder(data) {
  return request({
    url: '/pay/demo-order/create',
    method: 'post',
    data: data
  })
}

// 获得示例订单
export function getDemoOrder(id) {
  return request({
    url: '/pay/demo-order/get?id=' + id,
    method: 'get'
  })
}

// 获得示例订单分页
export function getDemoOrderPage(query) {
  return request({
    url: '/pay/demo-order/page',
    method: 'get',
    params: query
  })
}

// 退款示例订单
export function refundDemoOrder(id) {
  return request({
    url: '/pay/demo-order/refund?id=' + id,
    method: 'put'
  })
}
