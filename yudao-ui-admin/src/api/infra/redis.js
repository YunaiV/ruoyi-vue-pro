import request from '@/utils/request'

// 查询缓存详细
export function getCache() {
  return request({
    url: '/infra/redis/get-monitor-info',
    method: 'get'
  })
}

// 获取模块
export function getKeyDefineList() {
  return request({
    url: '/infra/redis/get-key-define-list',
    method: 'get'
  })
}

// 获取键名列表
export function getKeyList(keyTemplate) {
  return request({
    url: '/infra/redis/get-key-list',
    method: 'get',
    params: {
      keyTemplate
    }
  })
}

// 获取缓存内容
export function getKeyValue(key) {
  return request({
    url: '/infra/redis/get-key-value?key=' + key,
    method: 'get'
  })
}

// 根据键名删除缓存
export function deleteKey(key) {
  return request({
    url: '/infra/redis/delete-key?key=' + key,
    method: 'delete'
  })
}

export function deleteKeys(keyTemplate) {
  return request({
    url: '/infra/redis/delete-keys?',
    method: 'delete',
    params: {
      keyTemplate
    }
  })
}
