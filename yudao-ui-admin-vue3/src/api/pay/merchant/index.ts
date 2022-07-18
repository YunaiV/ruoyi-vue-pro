import { defHttp } from '@/config/axios'
import type { MerchantVO } from './types'

// 查询列表支付商户
export const getMerchantPageApi = ({ params }) => {
  return defHttp.get<PageResult<MerchantVO>>({ url: '/pay/merchant/page', params })
}

// 查询详情支付商户
export const getMerchantApi = (id: number) => {
  return defHttp.get<MerchantVO>({ url: '/pay/merchant/get?id=' + id })
}

// 根据商户名称搜索商户列表
export const getMerchantListByNameApi = (name: string) => {
  return defHttp.get<MerchantVO>({
    url: '/pay/merchant/list-by-name?id=',
    params: {
      name: name
    }
  })
}

// 新增支付商户
export const createMerchantApi = (params: MerchantVO) => {
  return defHttp.post({ url: '/pay/merchant/create', params })
}

// 修改支付商户
export const updateMerchantApi = (params: MerchantVO) => {
  return defHttp.put({ url: '/pay/merchant/update', params })
}

// 删除支付商户
export const deleteMerchantApi = (id: number) => {
  return defHttp.delete({ url: '/pay/merchant/delete?id=' + id })
}

// 导出支付商户
export const exportMerchantApi = (params) => {
  return defHttp.get({ url: '/pay/merchant/export-excel', params, responseType: 'blob' })
}
// 支付商户状态修改
export const changeMerchantStatusApi = (id: number, status: number) => {
  const data = {
    id,
    status
  }
  return defHttp.put({ url: '/pay/merchant/update-status', data: data })
}
