import { useAxios } from '@/hooks/web/useAxios'
import type {
  PermissionAssignRoleDataScopeReqVO,
  PermissionAssignRoleMenuReqVO,
  PermissionAssignUserRoleReqVO
} from './types'

const request = useAxios()

// 查询角色拥有的菜单权限
export const listRoleMenusApi = async (roleId: number) => {
  return await request.get({ url: '/system/permission/list-role-resources?roleId=' + roleId })
}

// 赋予角色菜单权限
export const assignRoleMenuApi = async (data: PermissionAssignRoleMenuReqVO) => {
  return await request.post({ url: '/system/permission/assign-role-menu', data })
}

// 赋予角色数据权限
export const assignRoleDataScopeApi = async (data: PermissionAssignRoleDataScopeReqVO) => {
  return await request.post({ url: '/system/permission/assign-role-data-scope', data })
}

// 查询用户拥有的角色数组
export const listUserRolesApi = async (userId: number) => {
  return await request.get({ url: '/system/permission/list-user-roles?userId=' + userId })
}

// 赋予用户角色
export const aassignUserRoleApi = async (data: PermissionAssignUserRoleReqVO) => {
  return await request.post({ url: '/system/permission/assign-user-role', data })
}
