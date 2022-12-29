import request from '@/config/axios'

export interface SmsTemplateVO {
  id: number
  type: number
  status: number
  code: string
  name: string
  content: string
  remark: string
  apiTemplateId: string
  channelId: number
  channelCode: string
  params: string[]
  createTime: Date
}

export interface SendSmsReqVO {
  mobile: string
  templateCode: string
  templateParams: Map<String, Object>
}

export interface SmsTemplatePageReqVO {
  type?: number
  status?: number
  code?: string
  content?: string
  apiTemplateId?: string
  channelId?: number
  createTime?: Date[]
}

export interface SmsTemplateExportReqVO {
  type?: number
  status?: number
  code?: string
  content?: string
  apiTemplateId?: string
  channelId?: number
  createTime?: Date[]
}

// 查询短信模板列表
export const getSmsTemplatePageApi = (params: SmsTemplatePageReqVO) => {
  return request.get({ url: '/system/sms-template/page', params })
}

// 查询短信模板详情
export const getSmsTemplateApi = (id: number) => {
  return request.get({ url: '/system/sms-template/get?id=' + id })
}

// 新增短信模板
export const createSmsTemplateApi = (data: SmsTemplateVO) => {
  return request.post({ url: '/system/sms-template/create', data })
}

// 修改短信模板
export const updateSmsTemplateApi = (data: SmsTemplateVO) => {
  return request.put({ url: '/system/sms-template/update', data })
}

// 删除短信模板
export const deleteSmsTemplateApi = (id: number) => {
  return request.delete({ url: '/system/sms-template/delete?id=' + id })
}

// 发送短信
export const sendSmsApi = (data: SendSmsReqVO) => {
  return request.post({ url: '/system/sms-template/send-sms', data })
}

// 导出短信模板
export const exportPostApi = (params: SmsTemplateExportReqVO) => {
  return request.download({
    url: '/system/sms-template/export-excel',
    params
  })
}
