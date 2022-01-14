import request from '@/utils/request'

// 创建用户组
export function createUserGroup(data) {
  return request({
    url: '/bpm/user-group/create',
    method: 'post',
    data: data
  })
}

// 更新用户组
export function updateUserGroup(data) {
  return request({
    url: '/bpm/user-group/update',
    method: 'put',
    data: data
  })
}

// 删除用户组
export function deleteUserGroup(id) {
  return request({
    url: '/bpm/user-group/delete?id=' + id,
    method: 'delete'
  })
}

// 获得用户组
export function getUserGroup(id) {
  return request({
    url: '/bpm/user-group/get?id=' + id,
    method: 'get'
  })
}

// 获得用户组分页
export function getUserGroupPage(query) {
  return request({
    url: '/bpm/user-group/page',
    method: 'get',
    params: query
  })
}

// 获取用户组精简信息列表
export function listSimpleUserGroups() {
  return request({
    url: '/bpm/user-group/list-all-simple',
    method: 'get'
  })
}
