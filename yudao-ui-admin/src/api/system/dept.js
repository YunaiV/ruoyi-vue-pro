import request from '@/utils/request'

// 查询部门列表
export function listDept(query) {
  return request({
    url: '/system/dept/list',
    method: 'get',
    params: query
  })
}

// 查询部门列表（排除节点）
export function listDeptExcludeChild(deptId) {
  return request({
    url: '/system/dept/list/exclude/' + deptId,
    method: 'get'
  })
}

// 查询部门详细
export function getDept(deptId) {
  return request({
    url: '/system/dept/get?id=' + deptId,
    method: 'get'
  })
}

// 获取部门精简信息列表
export function listSimpleDepts() {
  return request({
    url: '/system/dept/list-all-simple',
    method: 'get'
  })
}

// 新增部门
export function addDept(data) {
  return request({
    url: '/system/dept/create',
    method: 'post',
    data: data
  })
}

// 修改部门
export function updateDept(data) {
  return request({
    url: '/system/dept/update',
    method: 'put',
    data: data
  })
}

// 删除部门
export function delDept(id) {
  return request({
    url: '/system/dept/delete?id=' + id,
    method: 'delete'
  })
}
