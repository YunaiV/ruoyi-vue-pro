import request from '@/utils/request'

// 创建请假申请
export function createLeave(data) {
  return request({
    url: '/bpm/oa/leave/create',
    method: 'post',
    data: data
  })
}

// 获得请假申请
export function getLeave(id) {
  return request({
    url: '/bpm/oa/leave/get?id=' + id,
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
