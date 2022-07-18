import { defHttp } from '@/config/axios'
import type { OrderVO } from './types'

// 查询列表支付订单
export const getOrderPageApi = ({ params }) => {
  return defHttp.get<PageResult<OrderVO>>({ url: '/pay/order/page', params })
}

// 查询详情支付订单
export const getOrderApi = (id: number) => {
  return defHttp.get<OrderVO>({ url: '/pay/order/get?id=' + id })
}

// 新增支付订单
export const createOrderApi = (params: OrderVO) => {
  return defHttp.post({ url: '/pay/order/create', params })
}

// 修改支付订单
export const updateOrderApi = (params: OrderVO) => {
  return defHttp.put({ url: '/pay/order/update', params })
}

// 删除支付订单
export const deleteOrderApi = (id: number) => {
  return defHttp.delete({ url: '/pay/order/delete?id=' + id })
}

// 导出支付订单
export const exportOrderApi = (params) => {
  return defHttp.get({ url: '/pay/order/export-excel', params, responseType: 'blob' })
}
