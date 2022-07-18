import { defHttp } from '@/config/axios'
import type { UserVO } from './types'

// 查询用户管理列表
export const getUserPageApi = ({ params }) => {
  return defHttp.get<PageResult<UserVO>>({ url: '/system/user/page', params })
}

// 查询用户详情
export const getUserApi = (id: number) => {
  return defHttp.get<UserVO>({ url: '/system/user/get?id=' + id })
}

// 新增用户
export const createUserApi = (params: UserVO) => {
  return defHttp.post({ url: '/system/user/create', params })
}

// 修改用户
export const updateUserApi = (params: UserVO) => {
  return defHttp.put({ url: '/system/user/update', params })
}

// 删除用户
export const deleteUserApi = (id: number) => {
  return defHttp.delete({ url: '/system/user/delete?id=' + id })
}

// 导出用户
export const exportUserApi = (params) => {
  return defHttp.get({ url: '/system/user/export', params, responseType: 'blob' })
}

// 下载用户导入模板
export const importUserTemplateApi = () => {
  return defHttp.get({ url: '/system/user/get-import-template', responseType: 'blob' })
}

// 用户密码重置
export const resetUserPwdApi = (userId: number, password: number) => {
  const data = {
    userId,
    password
  }
  return defHttp.put({
    url: '/system/user/resetPwd',
    data: data
  })
}

// 用户状态修改
export const updateUserStatusApi = (id: number, status: number) => {
  const data = {
    id,
    status
  }
  return defHttp.put({ url: '/system/user/update-status', data: data })
}

// 查询授权角色
export const getAuthRoleApi = (userId: string) => {
  return defHttp.get({ url: '/system/user/authRole/' + userId })
}

// 保存授权角色
export const updateAuthRoleApi = (data: any) => {
  return defHttp.put({ url: '/system/user/authRole', params: data })
}
