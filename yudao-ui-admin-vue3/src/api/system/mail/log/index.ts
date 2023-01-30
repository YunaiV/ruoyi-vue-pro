import request from '@/config/axios'

export interface MailLogVO {
  id: number
  userId: number
  userType: number
  toMail: string
  accountId: number
  fromMail: string
  templateId: number
  templateCode: string
  templateNickname: string
  templateTitle: string
  templateContent: string
  templateParams: string
  sendStatus: number
  sendTime: Date
  sendMessageId: string
  sendException: string
}

export interface MailLogPageReqVO extends PageParam {
  userId?: number
  userType?: number
  toMail?: string
  accountId?: number
  templateId?: number
  sendStatus?: number
  sendTime?: Date[]
}

// 查询邮件日志列表
export const getMailLogPageApi = async (params: MailLogPageReqVO) => {
  return await request.get({ url: '/system/mail-log/page', params })
}

// 查询邮件日志详情
export const getMailLogApi = async (id: number) => {
  return await request.get({ url: '/system/mail-log/get?id=' + id })
}
