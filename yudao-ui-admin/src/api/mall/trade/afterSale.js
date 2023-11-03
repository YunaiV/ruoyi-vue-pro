import request from '@/utils/request'

// 获得交易售后
export function getAfterSale(id) {
  return request({
    url: '/trade/after-sale/get?id=' + id,
    method: 'get'
  })
}

// 获得交易售后分页
export function getAfterSalePage(query) {
  return request({
    url: '/trade/after-sale/page',
    method: 'get',
    params: query
  })
}
