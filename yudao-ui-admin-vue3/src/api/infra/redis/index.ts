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
