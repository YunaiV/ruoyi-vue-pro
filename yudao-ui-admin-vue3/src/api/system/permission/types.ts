export type PermissionAssignUserRoleReqVO = {
  userId: number
  roleIds: number[]
}

export type PermissionAssignRoleMenuReqVO = {
  roleId: number
  menuIds: number[]
}

export type PermissionAssignRoleDataScopeReqVO = {
  roleId: number
  dataScope: number
  dataScopeDeptIds: number[]
}
