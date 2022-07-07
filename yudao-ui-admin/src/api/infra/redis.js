import request from '@/utils/request'

// 查询缓存详细
export function getCache() {
  return request({
    url: '/infra/redis/get-monitor-info',
    method: 'get'
  })
}

// 获取模块
export function getKeyList() {
  return request({
    url: '/infra/redis/get-key-list',
    method: 'get'
  })
}

// 获取键名列表
export function getKeyDefineKeys(keyDefine) {
  return request({
    url: '/infra/redis/get-key/' + keyDefine,
    method: 'get'
  })
}

// 获取缓存内容
export function getKeyValue(keyDefine, key) {
  return request({
    url: '/infra/redis/get-key/' + keyDefine + "/" + key,
    method: 'get'
  })
}

// 根据键名删除缓存
export function clearCacheKey(key) {
  return request({
    url: '/infra/redis/clearCacheKey/' + key,
    method: 'delete'
  })
}

