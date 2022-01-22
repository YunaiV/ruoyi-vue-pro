import request from '@/utils/request'

// 创建退款订单
export function createRefund(data) {
  return request({
    url: '/pay/refund/create',
    method: 'post',
    data: data
  })
}

// 更新退款订单
export function updateRefund(data) {
  return request({
    url: '/pay/refund/update',
    method: 'put',
    data: data
  })
}

// 删除退款订单
export function deleteRefund(id) {
  return request({
    url: '/pay/refund/delete?id=' + id,
    method: 'delete'
  })
}

// 获得退款订单
export function getRefund(id) {
  return request({
    url: '/pay/refund/get?id=' + id,
    method: 'get'
  })
}

// 获得退款订单分页
export function getRefundPage(query) {
  return request({
    url: '/pay/refund/page',
    method: 'get',
    params: query
  })
}

// 导出退款订单 Excel
export function exportRefundExcel(query) {
  return request({
    url: '/pay/refund/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
