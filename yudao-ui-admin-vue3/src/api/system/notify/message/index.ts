import request from '@/config/axios'

export interface NotifyMessageVO {
  id: number
  userId: number
  userType: number
  templateId: number
  templateCode: string
  templateNickname: string
  templateContent: string
  templateType: number
  templateParams: string
  readStatus: boolean
  readTime: Date
}

export interface NotifyMessagePageReqVO extends PageParam {
  userId?: number
  userType?: number
  templateCode?: string
  templateType?: number
  createTime?: Date[]
}

// 查询站内信消息列表
export const getNotifyMessagePageApi = async (params: NotifyMessagePageReqVO) => {
  return await request.get({ url: '/system/notify-message/page', params })
}

// 查询站内信消息详情
export const getNotifyMessageApi = async (id: number) => {
  return await request.get({ url: '/system/notify-message/get?id=' + id })
}
