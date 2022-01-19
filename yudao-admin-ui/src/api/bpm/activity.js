import request from '@/utils/request'

export function getActivityList(query) {
  return request({
    url: '/bpm/activity/list',
    method: 'get',
    params: query
  })
}
