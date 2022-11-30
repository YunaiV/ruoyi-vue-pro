import request from '@/config/axios'

export interface NoticeVO {
  id: number
  title: string
  type: number
  content: string
  status: number
  remark: string
  creator: string
  createTime: Date
}

export interface NoticePageReqVO extends PageParam {
  title?: string
  status?: number
}

// 查询公告列表
export const getNoticePageApi = (params: NoticePageReqVO) => {
  return request.get({ url: '/system/notice/page', params })
}

// 查询公告详情
export const getNoticeApi = (id: number) => {
  return request.get({ url: '/system/notice/get?id=' + id })
}

// 新增公告
export const createNoticeApi = (data: NoticeVO) => {
  return request.post({ url: '/system/notice/create', data })
}

// 修改公告
export const updateNoticeApi = (data: NoticeVO) => {
  return request.put({ url: '/system/notice/update', data })
}

// 删除公告
export const deleteNoticeApi = (id: number) => {
  return request.delete({ url: '/system/notice/delete?id=' + id })
}
