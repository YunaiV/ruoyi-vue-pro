import request from '@/config/axios'

export interface SmsChannelVO {
  id: number
  code: string
  status: number
  signature: string
  remark: string
  apiKey: string
  apiSecret: string
  callbackUrl: string
  createTime: Date
}

export interface SmsChannelPageReqVO extends PageParam {
  signature?: string
  code?: string
  status?: number
  createTime?: Date[]
}

// 查询短信渠道列表
export const getSmsChannelPageApi = (params: SmsChannelPageReqVO) => {
  return request.get({ url: '/system/sms-channel/page', params })
}

// 获得短信渠道精简列表
export function getSimpleSmsChannels() {
  return request.get({ url: '/system/sms-channel/list-all-simple' })
}

// 查询短信渠道详情
export const getSmsChannelApi = (id: number) => {
  return request.get({ url: '/system/sms-channel/get?id=' + id })
}

// 新增短信渠道
export const createSmsChannelApi = (data: SmsChannelVO) => {
  return request.post({ url: '/system/sms-channel/create', data })
}

// 修改短信渠道
export const updateSmsChannelApi = (data: SmsChannelVO) => {
  return request.put({ url: '/system/sms-channel/update', data })
}

// 删除短信渠道
export const deleteSmsChannelApi = (id: number) => {
  return request.delete({ url: '/system/sms-channel/delete?id=' + id })
}
