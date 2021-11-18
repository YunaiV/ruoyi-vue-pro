import request from '@/utils/request'


export function page(query) {
  return request({
    url: '/workflow/models/page',
    method: 'get',
    params: query
  })
}
export function exportBpmnXml(query) {
  return request({
    url: '/workflow/models/exportBpmnXml',
    method: 'get',
    params: query
  })
}

export function modelUpdate(data) {
  return request({
    url: '/workflow/models/update',
    method: 'POST',
    data: data
  })
}

export function modelSave(data) {
  return request({
    url: '/workflow/models/create',
    method: 'POST',
    data: data
  })
}

export function modelDelete(data) {
  return request({
    url: '/workflow/models/delete?modelId='+ data.modelId,
    method: 'POST',
    data: data
  })
}
export function modelDeploy(data) {
  return request({
    url: '/workflow/models/deploy?modelId='+ data.modelId,
    method: 'POST',
    data: data
  })
}
