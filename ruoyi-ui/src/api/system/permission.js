import request from '@/utils/request'

// 查询角色拥有的菜单数组
export function listRoleMenus(roleId) {
  return request({
    url: '/system/permission/list-role-resources?roleId=' + roleId,
    method: 'get'
  })
}

// 赋予角色菜单
export function assignRoleMenu(data) {
  return request({
    url: '/system/permission/assign-role-menu',
    method: 'post',
    data: data
  })
}
