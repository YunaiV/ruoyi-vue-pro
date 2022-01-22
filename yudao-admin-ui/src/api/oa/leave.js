import request from '@/utils/request'

// 创建请假申请
export function createLeave(data) {
  return request({
    url: '/oa/leave/create',
    method: 'post',
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
    url: '/bpm/oa/leave/page',
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

export function getLeaveApplyMembers() {
  return request({
    url: '/oa/leave/getLeaveApplyMembers',
    method: 'get'
  })
}
