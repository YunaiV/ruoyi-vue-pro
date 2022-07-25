import { useAxios } from '@/hooks/web/useAxios'
import type { MerchantVO } from './types'

const request = useAxios()

// 查询列表支付商户
export const getMerchantPageApi = (params) => {
  return request.get({ url: '/pay/merchant/page', params })
}

// 查询详情支付商户
export const getMerchantApi = (id: number) => {
  return request.get({ url: '/pay/merchant/get?id=' + id })
}

// 根据商户名称搜索商户列表
export const getMerchantListByNameApi = (name: string) => {
  return request.get({
    url: '/pay/merchant/list-by-name?id=',
    params: {
      name: name
    }
  })
}

// 新增支付商户
export const createMerchantApi = (data: MerchantVO) => {
  return request.post({ url: '/pay/merchant/create', data })
}

// 修改支付商户
export const updateMerchantApi = (data: MerchantVO) => {
  return request.put({ url: '/pay/merchant/update', data })
}

// 删除支付商户
export const deleteMerchantApi = (id: number) => {
  return request.delete({ url: '/pay/merchant/delete?id=' + id })
}

// 导出支付商户
export const exportMerchantApi = (params) => {
  return request.download({ url: '/pay/merchant/export-excel', params })
}
// 支付商户状态修改
export const changeMerchantStatusApi = (id: number, status: number) => {
  const data = {
    id,
    status
  }
  return request.put({ url: '/pay/merchant/update-status', data: data })
}
