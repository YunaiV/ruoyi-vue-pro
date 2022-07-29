import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

/**
 * 获取redis 监控信息
 */
export const getCacheApi = () => {
  return request.get({ url: '/infra/redis/get-monitor-info' })
}
// 获取模块
export const getKeyDefineListApi = () => {
  return request.get({ url: '/infra/redis/get-key-define-list' })
}
/**
 * 获取redis key列表
 */
export const getKeyListApi = (keyTemplate: string) => {
  return request.get({
    url: '/infra/redis/get-key-list',
    params: {
      keyTemplate
    }
  })
}
// 获取缓存内容
export const getKeyValueApi = (key: string) => {
  return request.get({ url: '/infra/redis/get-key-value?key=' + key })
}

// 根据键名删除缓存
export const deleteKeyApi = (key: string) => {
  return request.delete({ url: '/infra/redis/delete-key?key=' + key })
}

export const deleteKeysApi = (keyTemplate: string) => {
  return request.delete({
    url: '/infra/redis/delete-keys?',
    params: {
      keyTemplate
    }
  })
}
