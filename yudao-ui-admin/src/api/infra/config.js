import request from '@/utils/request'

// 查询参数列表
export function listConfig(query) {
  return request({
    url: '/infra/config/page',
    method: 'get',
    params: query
  })
}

// 查询参数详细
export function getConfig(configId) {
  return request({
    url: '/infra/config/get?id=' + configId,
    method: 'get'
  })
}

// 根据参数键名查询参数值
export function getConfigKey(configKey) {
  return request({
    url: '/infra/config/get-value-by-key?key=' + configKey,
    method: 'get'
  })
}

// 新增参数配置
export function addConfig(data) {
  return request({
    url: '/infra/config/create',
    method: 'post',
    data: data
  })
}

// 修改参数配置
export function updateConfig(data) {
  return request({
    url: '/infra/config/update',
    method: 'put',
    data: data
  })
}

// 删除参数配置
export function delConfig(configId) {
  return request({
    url: '/infra/config/delete?id=' + configId,
    method: 'delete'
  })
}

// 导出参数
export function exportConfig(query) {
  return request({
    url: '/infra/config/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
