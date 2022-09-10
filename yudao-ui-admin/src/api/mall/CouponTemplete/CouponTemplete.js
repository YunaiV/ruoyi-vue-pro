import request from '@/utils/request'

// 创建优惠券模板
export function create(data) {
  return request({
    url: '/coupon/template/create',
    method: 'post',
    data: data
  })
}

// 更新优惠券模板
export function update(data) {
  return request({
    url: '/coupon/template/update',
    method: 'put',
    data: data
  })
}

// 删除优惠券模板
export function deleteCouponTemplete (id) {
  return request({
    url: '/coupon/template/delete?id=' + id,
    method: 'delete'
  })
}

// 获得优惠券模板
export function get(id) {
  return request({
    url: '/coupon/template/get?id=' + id,
    method: 'get'
  })
}

// 获得优惠券模板分页
export function getPage(query) {
  return request({
    url: '/coupon/template/page',
    method: 'get',
    params: query
  })
}

// 导出优惠券模板 Excel
export function exportExcel(query) {
  return request({
    url: '/coupon/template/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
