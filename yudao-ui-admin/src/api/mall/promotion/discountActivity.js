import request from '@/utils/request'

// 创建限时折扣活动
export function createDiscountActivity(data) {
  return request({
    url: '/promotion/discount-activity/create',
    method: 'post',
    data: data
  })
}

// 更新限时折扣活动
export function updateDiscountActivity(data) {
  return request({
    url: '/promotion/discount-activity/update',
    method: 'put',
    data: data
  })
}

// 关闭限时折扣活动
export function closeDiscountActivity(id) {
  return request({
    url: '/promotion/discount-activity/close?id=' + id,
    method: 'put'
  })
}

// 删除限时折扣活动
export function deleteDiscountActivity(id) {
  return request({
    url: '/promotion/discount-activity/delete?id=' + id,
    method: 'delete'
  })
}

// 获得限时折扣活动
export function getDiscountActivity(id) {
  return request({
    url: '/promotion/discount-activity/get?id=' + id,
    method: 'get'
  })
}

// 获得限时折扣活动分页
export function getDiscountActivityPage(query) {
  return request({
    url: '/promotion/discount-activity/page',
    method: 'get',
    params: query
  })
}
