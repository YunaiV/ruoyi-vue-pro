import { useAxios } from '@/hooks/web/useAxios'
import type { OrderVO } from './types'

const request = useAxios()

// 查询列表支付订单
export const getOrderPageApi = async (params) => {
  return await request.get({ url: '/pay/order/page', params })
}

// 查询详情支付订单
export const getOrderApi = async (id: number) => {
  return await request.get({ url: '/pay/order/get?id=' + id })
}

// 新增支付订单
export const createOrderApi = async (data: OrderVO) => {
  return await request.post({ url: '/pay/order/create', data })
}

// 修改支付订单
export const updateOrderApi = async (data: OrderVO) => {
  return await request.put({ url: '/pay/order/update', data })
}

// 删除支付订单
export const deleteOrderApi = async (id: number) => {
  return await request.delete({ url: '/pay/order/delete?id=' + id })
}

// 导出支付订单
export const exportOrderApi = async (params) => {
  return await request.download({ url: '/pay/order/export-excel', params })
}
