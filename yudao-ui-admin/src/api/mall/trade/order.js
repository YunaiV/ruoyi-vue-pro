import request from '@/utils/request'

// 获得交易订单分页
export function getOrderPage(query) {
  return request({
    url: '/trade/order/page',
    method: 'get',
    params: query
  })
}

// 获得交易订单详情
export function getOrderDetail(id) {
  return request({
    url: '/trade/order/get-detail?id=' + id,
    method: 'get',
  })
}
