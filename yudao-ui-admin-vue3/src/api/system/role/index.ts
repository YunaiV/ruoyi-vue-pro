import { useAxios } from '@/hooks/web/useAxios'
import type { RoleVO } from './types'

const request = useAxios()

// 查询角色列表
export const getRolePageApi = (params) => {
  return request.get({ url: '/system/role/page', params })
}

// 查询角色详情
export const getRoleApi = (id: number) => {
  return request.get({ url: '/system/role/get?id=' + id })
}

// 新增角色
export const createRoleApi = (data: RoleVO) => {
  return request.post({ url: '/system/role/create', data })
}

// 修改角色
export const updateRoleApi = (data: RoleVO) => {
  return request.put({ url: '/system/role/update', data })
}

// 修改角色状态
export const updateRoleStatusApi = (data: RoleVO) => {
  return request.put({ url: '/system/role/update-status', data })
}

// 删除角色
export const deleteRoleApi = (id: number) => {
  return request.delete({ url: '/system/role/delete?id=' + id })
}
