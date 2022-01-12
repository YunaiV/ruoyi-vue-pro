import request from '@/utils/request'

export function getTaskAssignRuleList(query) {
  return request({
    url: '/bpm/task-assign-rule/list',
    method: 'get',
    params: query
  })
}
