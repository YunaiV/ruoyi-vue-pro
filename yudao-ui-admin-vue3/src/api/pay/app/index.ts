import { useAxios } from '@/hooks/web/useAxios'
import type { AppVO } from './types'

const request = useAxios()

// 查询列表支付应用
export const getAppPageApi = (params) => {
  return request.get({ url: '/pay/app/page', params })
}

// 查询详情支付应用
export const getAppApi = (id: number) => {
  return request.get({ url: '/pay/app/get?id=' + id })
}

// 新增支付应用
export const createAppApi = (data: AppVO) => {
  return request.post({ url: '/pay/app/create', data })
}

// 修改支付应用
export const updateAppApi = (data: AppVO) => {
  return request.put({ url: '/pay/app/update', data })
}

// 支付应用信息状态修改
export const changeAppStatusApi = (id: number, status: number) => {
  const data = {
    id,
    status
  }
  return request.put({ url: '/pay/app/update-status', data: data })
}

// 删除支付应用
export const deleteAppApi = (id: number) => {
  return request.delete({ url: '/pay/app/delete?id=' + id })
}

// 导出支付应用
export const exportAppApi = (params) => {
  return request.download({ url: '/pay/app/export-excel', params })
}

// 根据商ID称搜索应用列表
export const getAppListByMerchantIdApi = (merchantId: number) => {
  return request.get({ url: '/pay/app/list-merchant-id', params: { merchantId: merchantId } })
}
