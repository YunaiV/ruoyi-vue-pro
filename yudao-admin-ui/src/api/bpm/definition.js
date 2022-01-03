import request from '@/utils/request'

export function getDefinitionPage(query) {
  return request({
    url: '/bpm/definition/page',
    method: 'get',
    params: query
  })
}

export function getDefinitionBpmnXML(id) {
  return request({
    url: '/bpm/definition/get-bpmn-xml?id=' + id,
    method: 'get'
  })
}
