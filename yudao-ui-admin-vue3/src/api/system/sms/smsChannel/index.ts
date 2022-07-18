import { defHttp } from '@/config/axios'
import type { SmsChannelVO } from './types'

// 查询短信渠道列表
export const getSmsChannelPageApi = ({ params }) => {
  return defHttp.get<PageResult<SmsChannelVO>>({ url: '/system/sms-channel/page', params })
}

// 获得短信渠道精简列表
export function getSimpleSmsChannels() {
  return defHttp.get({ url: '/system/sms-channel/list-all-simple' })
}

// 查询短信渠道详情
export const getSmsChannelApi = (id: number) => {
  return defHttp.get<SmsChannelVO>({ url: '/system/sms-channel/get?id=' + id })
}

// 新增短信渠道
export const createSmsChannelApi = (params: SmsChannelVO) => {
  return defHttp.post({ url: '/system/sms-channel/create', params })
}

// 修改短信渠道
export const updateSmsChannelApi = (params: SmsChannelVO) => {
  return defHttp.put({ url: '/system/sms-channel/update', params })
}

// 删除短信渠道
export const deleteSmsChannelApi = (id: number) => {
  return defHttp.delete({ url: '/system/sms-channel/delete?id=' + id })
}
