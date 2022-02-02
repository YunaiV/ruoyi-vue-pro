import request from '@/utils/request'

export function getModelPage(query) {
  return request({
    url: '/bpm/model/page',
    method: 'get',
    params: query
  })
}

export function getModel(id) {
  return request({
    url: '/bpm/model/get?id=' + id,
    method: 'get'
  })
}

export function updateModel(data) {
  return request({
    url: '/bpm/model/update',
    method: 'PUT',
    data: data
  })
}

// 任务状态修改
export function updateModelState(id, state) {
  return request({
    url: '/bpm/model/update-state',
    method: 'put',
    data: {
      id,
      state
    }
  })
}

export function createModel(data) {
  return request({
    url: '/bpm/model/create',
    method: 'POST',
    data: data
  })
}

export function deleteModel(id) {
  return request({
    url: '/bpm/model/delete?id=' + id,
    method: 'DELETE'
  })
}

export function deployModel(id) {
  return request({
    url: '/bpm/model/deploy?id=' + id,
    method: 'POST'
  })
}
