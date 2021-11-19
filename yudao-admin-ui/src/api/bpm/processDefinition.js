import request from '@/utils/request'


export function page(query) {
  return request({
    url: '/workflow/process/definition/page',
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
