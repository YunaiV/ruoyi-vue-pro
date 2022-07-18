import { defHttp } from '@/config/axios'
import type { RoleVO } from './types'

// 查询角色列表
export const getRolePageApi = ({ params }) => {
  return defHttp.get<PageResult<RoleVO>>({ url: '/system/role/page', params })
}

// 查询角色详情
export const getRoleApi = (id: number) => {
  return defHttp.get<RoleVO>({ url: '/system/role/get?id=' + id })
}

// 新增角色
export const createRoleApi = (params: RoleVO) => {
  return defHttp.post({ url: '/system/role/create', params })
}

// 修改角色
export const updateRoleApi = (params: RoleVO) => {
  return defHttp.put({ url: '/system/role/update', params })
}

// 修改角色状态
export const updateRoleStatusApi = (params: RoleVO) => {
  return defHttp.put({ url: '/system/role/update-status', params })
}

// 删除角色
export const deleteRoleApi = (id: number) => {
  return defHttp.delete({ url: '/system/role/delete?id=' + id })
}
