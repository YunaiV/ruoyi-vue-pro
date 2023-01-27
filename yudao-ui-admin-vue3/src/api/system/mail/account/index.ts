import request from '@/config/axios'

export interface MailAccountVO {
  id: number
  mail: string
  username: string
  password: string
  host: string
  port: number
  sslEnable: boolean
}

export interface MailAccountPageReqVO extends PageParam {
  mail?: string
  username?: string
}

// 查询邮箱账号列表
export const getMailAccountPageApi = async (params: MailAccountPageReqVO) => {
  return await request.get({ url: '/system/mail-account/page', params })
}

// 查询邮箱账号详情
export const getMailAccountApi = async (id: number) => {
  return await request.get({ url: '/system/mail-account/get?id=' + id })
}

// 新增邮箱账号
export const createMailAccountApi = async (data: MailAccountVO) => {
  return await request.post({ url: '/system/mail-account/create', data })
}

// 修改邮箱账号
export const updateMailAccountApi = async (data: MailAccountVO) => {
  return await request.put({ url: '/system/mail-account/update', data })
}

// 删除邮箱账号
export const deleteMailAccountApi = async (id: number) => {
  return await request.delete({ url: '/system/mail-account/delete?id=' + id })
}

// 获得邮箱账号精简列表
export const getSimpleMailAccounts = async () => {
  return request.get({ url: '/system/mail-account/list-all-simple' })
}
