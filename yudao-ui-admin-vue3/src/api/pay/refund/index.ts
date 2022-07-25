import { useAxios } from '@/hooks/web/useAxios'
import type { RefundVO } from './types'

const request = useAxios()

// 查询列表退款订单
export const getRefundPageApi = (params) => {
  return request.get({ url: '/pay/refund/page', params })
}

// 查询详情退款订单
export const getRefundApi = (id: number) => {
  return request.get({ url: '/pay/refund/get?id=' + id })
}

// 新增退款订单
export const createRefundApi = (data: RefundVO) => {
  return request.post({ url: '/pay/refund/create', data })
}

// 修改退款订单
export const updateRefundApi = (data: RefundVO) => {
  return request.put({ url: '/pay/refund/update', data })
}

// 删除退款订单
export const deleteRefundApi = (id: number) => {
  return request.delete({ url: '/pay/refund/delete?id=' + id })
}

// 导出退款订单
export const exportRefundApi = (params) => {
  return request.download({ url: '/pay/refund/export-excel', params })
}
