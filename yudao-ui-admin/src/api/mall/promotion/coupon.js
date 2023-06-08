import request from '@/utils/request'

// 删除优惠劵
export function deleteCoupon(id) {
  return request({
    url: '/promotion/coupon/delete?id=' + id,
    method: 'delete'
  })
}

// 获得优惠劵分页
export function getCouponPage(query) {
  return request({
    url: '/promotion/coupon/page',
    method: 'get',
    params: query
  })
}
