import request from '@/config/axios'
import { UserGroupVO } from './types'

// 创建用户组
export const createUserGroupApi = async (data: UserGroupVO) => {
  return await request.post({
    url: '/bpm/user-group/create',
    data: data
  })
}

// 更新用户组
export const updateUserGroupApi = async (data: UserGroupVO) => {
  return await request.put({
    url: '/bpm/user-group/update',
    data: data
  })
}

// 删除用户组
export const deleteUserGroupApi = async (id: number) => {
  return await request.delete({ url: '/bpm/user-group/delete?id=' + id })
}

// 获得用户组
export const getUserGroupApi = async (id: number) => {
  return await request.get({ url: '/bpm/user-group/get?id=' + id })
}

// 获得用户组分页
export const getUserGroupPageApi = async (params) => {
  return await request.get({ url: '/bpm/user-group/page', params })
}

// 获取用户组精简信息列表
export const listSimpleUserGroupsApi = async () => {
  return await request.get({ url: '/bpm/user-group/list-all-simple' })
}
