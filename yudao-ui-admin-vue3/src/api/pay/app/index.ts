import { defHttp } from '@/config/axios'
import type { AppVO } from './types'

// 查询列表支付应用
export const getAppPageApi = ({ params }) => {
  return defHttp.get<PageResult<AppVO>>({ url: '/pay/app/page', params })
}

// 查询详情支付应用
export const getAppApi = (id: number) => {
  return defHttp.get<AppVO>({ url: '/pay/app/get?id=' + id })
}

// 新增支付应用
export const createAppApi = (params: AppVO) => {
  return defHttp.post({ url: '/pay/app/create', params })
}

// 修改支付应用
export const updateAppApi = (params: AppVO) => {
  return defHttp.put({ url: '/pay/app/update', params })
}

// 支付应用信息状态修改
export const changeAppStatusApi = (id: number, status: number) => {
  const data = {
    id,
    status
  }
  return defHttp.put({ url: '/pay/app/update-status', data: data })
}

// 删除支付应用
export const deleteAppApi = (id: number) => {
  return defHttp.delete({ url: '/pay/app/delete?id=' + id })
}

// 导出支付应用
export const exportAppApi = (params) => {
  return defHttp.get({ url: '/pay/app/export-excel', params, responseType: 'blob' })
}

// 根据商ID称搜索应用列表
export const getAppListByMerchantIdApi = (merchantId: number) => {
  return defHttp.get({ url: '/pay/app/list-merchant-id', params: { merchantId: merchantId } })
}
