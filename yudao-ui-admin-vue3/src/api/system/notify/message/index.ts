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

// 获得我的站内信分页
// export function getMyNotifyMessagePage(query) {
//   return request({
//     url: '/system/notify-message/my-page',
//     method: 'get',
//     params: query
//   })
// }

// 批量标记已读
// export function updateNotifyMessageRead(ids) {
//   return request({
//     url: '/system/notify-message/update-read?' + qs.stringify({ids: ids}, { indices: false }),
//     method: 'put'
//   })
// }

// 标记所有站内信为已读
// export function updateAllNotifyMessageRead() {
//   return request({
//     url: '/system/notify-message/update-all-read',
//     method: 'put'
//   })
// }

// 获取当前用户的最新站内信列表
export const getUnreadNotifyMessageListApi = async () => {
  return await request.get({ url: '/system/notify-message/get-unread-list' })
}

// 获得当前用户的未读站内信数量
export const getUnreadNotifyMessageCountApi = async () => {
  return await request.get({ url: '/system/notify-message/get-unread-count' })
}
