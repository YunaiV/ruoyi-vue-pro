import { useAxios } from '@/hooks/web/useAxios'
import type { UserVO } from './types'

const request = useAxios()

// 查询用户管理列表
export const getUserPageApi = (params) => {
  return request.get({ url: '/system/user/page', params })
}

// 查询用户详情
export const getUserApi = (id: number) => {
  return request.get({ url: '/system/user/get?id=' + id })
}

// 新增用户
export const createUserApi = (data: UserVO) => {
  return request.post({ url: '/system/user/create', data })
}

// 修改用户
export const updateUserApi = (data: UserVO) => {
  return request.put({ url: '/system/user/update', data })
}

// 删除用户
export const deleteUserApi = (id: number) => {
  return request.delete({ url: '/system/user/delete?id=' + id })
}

// 导出用户
export const exportUserApi = (params) => {
  return request.download({ url: '/system/user/export', params })
}

// 下载用户导入模板
export const importUserTemplateApi = () => {
  return request.download({ url: '/system/user/get-import-template' })
}

// 用户密码重置
export const resetUserPwdApi = (id: number, password: string) => {
  const data = {
    id,
    password
  }
  return request.put({ url: '/system/user/update-password', data: data })
}

// 用户状态修改
export const updateUserStatusApi = (id: number, status: number) => {
  const data = {
    id,
    status
  }
  return request.put({ url: '/system/user/update-status', data: data })
}

// 获取用户精简信息列表
export const getListSimpleUsersApi = () => {
  return request.get({ url: '/system/user/list-all-simple' })
}
