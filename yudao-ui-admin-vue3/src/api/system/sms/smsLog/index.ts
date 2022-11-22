import request from '@/config/axios'

export interface SmsLogVO {
  id: number
  idchannelId: number
  templateId: number
  mobile: string
  userId: number
  userType: number
  sendStatus: number
  receiveStatus: number
  templateType: number
  templateContent: string
  templateParams: Map<string, object>
  apiTemplateId: string
  sendTime: string
  createTime: string
}

export interface SmsLogPageReqVO extends PageParam {
  signature?: string
  code?: string
  status?: number
}
export interface SmsLogExportReqVO {
  code?: string
  name?: string
  status?: number
}

// 查询短信日志列表
export const getSmsLogPageApi = (params: SmsLogPageReqVO) => {
  return request.get({ url: '/system/sms-log/page', params })
}

// 导出短信日志
export const exportSmsLogApi = (params: SmsLogExportReqVO) => {
  return request.download({ url: '/system/sms-log/export', params })
}
