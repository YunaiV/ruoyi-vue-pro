import request from '@/utils/request'

// 创建优惠劵模板
export function createCouponTemplate(data) {
  return request({
    url: '/promotion/coupon-template/create',
    method: 'post',
    data: data
  })
}

// 更新优惠劵模板
export function updateCouponTemplate(data) {
  return request({
    url: '/promotion/coupon-template/update',
    method: 'put',
    data: data
  })
}

// 更新优惠劵模板的状态
export function updateCouponTemplateStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
    url: '/promotion/coupon-template/update-status',
    method: 'put',
    data: data
  })
}

// 删除优惠劵模板
export function deleteCouponTemplate(id) {
  return request({
    url: '/promotion/coupon-template/delete?id=' + id,
    method: 'delete'
  })
}

// 获得优惠劵模板
export function getCouponTemplate(id) {
  return request({
    url: '/promotion/coupon-template/get?id=' + id,
    method: 'get'
  })
}

// 获得优惠劵模板分页
export function getCouponTemplatePage(query) {
  return request({
    url: '/promotion/coupon-template/page',
    method: 'get',
    params: query
  })
}

// 导出优惠劵模板 Excel
export function exportCouponTemplateExcel(query) {
  return request({
    url: '/promotion/coupon-template/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
