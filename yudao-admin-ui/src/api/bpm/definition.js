import request from '@/utils/request'

export function getDefinitionPage(query) {
  return request({
    url: '/bpm/definition/page',
    method: 'get',
    params: query
  })
}

export function exportProcessDefinition(query) {
  return request({
    url: '/workflow/process/definition/export?processDefinitionId='+query.id,
    method: 'get'
  })
}
