import request from '@/utils/request'

// 创建请假申请
export function createLeave(data) {
  return request({
    url: '/oa/leave/create',
    method: 'post',
    data: data
  })
}

// 更新请假申请
export function updateLeave(data) {
  return request({
    url: '/oa/leave/update',
    method: 'put',
    data: data
  })
}

// 删除请假申请
export function deleteLeave(id) {
  return request({
    url: '/oa/leave/delete?id=' + id,
    method: 'delete'
  })
}

// 获得请假申请
export function getLeave(id) {
  return request({
    url: '/oa/leave/get?id=' + id,
    method: 'get'
  })
}

// 获得请假申请分页
export function getLeavePage(query) {
  return request({
    url: '/oa/leave/page',
    method: 'get',
    params: query
  })
}

export function createFormKeyLeave(data) {
  return request({
    url: '/oa/leave/form-key/create',
    method: 'post',
    data: data
  })
}

// 导出请假申请 Excel
export function exportLeaveExcel(query) {
  return request({
    url: '/oa/leave/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
