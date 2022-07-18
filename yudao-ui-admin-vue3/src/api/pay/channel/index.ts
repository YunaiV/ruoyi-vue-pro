import { defHttp } from '@/config/axios'
import type { ChannelVO } from './types'

// 查询列表支付渠道
export const getChannelPageApi = ({ params }) => {
  return defHttp.get<PageResult<ChannelVO>>({ url: '/pay/channel/page', params })
}

// 查询详情支付渠道
export const getChannelApi = (merchantId: number, appId: string, code: string) => {
  const params = {
    merchantId: merchantId,
    appId: appId,
    code: code
  }
  return defHttp.get<ChannelVO>({ url: '/pay/channel/get-channel', params: params })
}

// 新增支付渠道
export const createChannelApi = (params: ChannelVO) => {
  return defHttp.post({ url: '/pay/channel/create', params })
}

// 修改支付渠道
export const updateChannelApi = (params: ChannelVO) => {
  return defHttp.put({ url: '/pay/channel/update', params })
}

// 删除支付渠道
export const deleteChannelApi = (id: number) => {
  return defHttp.delete({ url: '/pay/channel/delete?id=' + id })
}

// 导出支付渠道
export const exportChannelApi = (params) => {
  return defHttp.get({ url: '/pay/channel/export-excel', params, responseType: 'blob' })
}
