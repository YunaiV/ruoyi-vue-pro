import { defHttp } from '@/config/axios'
import type { NoticeVO } from './types'

// 查询公告列表
export const getNoticePageApi = ({ params }) => {
  return defHttp.get<PageResult<NoticeVO>>({ url: '/system/notice/page', params })
}

// 查询公告详情
export const getNoticeApi = (id: number) => {
  return defHttp.get<NoticeVO>({ url: '/system/notice/get?id=' + id })
}

// 新增公告
export const createNoticeApi = (params: NoticeVO) => {
  return defHttp.post({ url: '/system/notice/create', params })
}

// 修改公告
export const updateNoticeApi = (params: NoticeVO) => {
  return defHttp.put({ url: '/system/notice/update', params })
}

// 删除公告
export const deleteNoticeApi = (id: number) => {
  return defHttp.delete({ url: '/system/notice/delete?id=' + id })
}
