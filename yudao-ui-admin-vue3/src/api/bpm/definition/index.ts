import request from '@/config/axios'

export const getProcessDefinitionBpmnXMLApi = async (id: number) => {
  return await request.get({
    url: '/bpm/process-definition/get-bpmn-xml?id=' + id
  })
}

export const getProcessDefinitionPageApi = async (params) => {
  return await request.get({
    url: '/bpm/process-definition/page',
    params
  })
}

export const getProcessDefinitionListApi = async (params) => {
  return await request.get({
    url: '/bpm/process-definition/list',
    params
  })
}
