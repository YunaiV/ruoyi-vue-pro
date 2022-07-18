import { defHttp } from '@/config/axios'
import type { RefundVO } from './types'

// 查询列表退款订单
export const getRefundPageApi = ({ params }) => {
  return defHttp.get<PageResult<RefundVO>>({ url: '/pay/refund/page', params })
}

// 查询详情退款订单
export const getRefundApi = (id: number) => {
  return defHttp.get<RefundVO>({ url: '/pay/refund/get?id=' + id })
}

// 新增退款订单
export const createRefundApi = (params: RefundVO) => {
  return defHttp.post({ url: '/pay/refund/create', params })
}

// 修改退款订单
export const updateRefundApi = (params: RefundVO) => {
  return defHttp.put({ url: '/pay/refund/update', params })
}

// 删除退款订单
export const deleteRefundApi = (id: number) => {
  return defHttp.delete({ url: '/pay/refund/delete?id=' + id })
}

// 导出退款订单
export const exportRefundApi = (params) => {
  return defHttp.get({ url: '/pay/refund/export-excel', params, responseType: 'blob' })
}
