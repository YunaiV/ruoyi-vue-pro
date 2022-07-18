import { defHttp } from '@/config/axios'
import { RedisKeyInfo, RedisMonitorInfoVO } from '@/api/infra/redis/types'

/**
 * 获取redis 监控信息
 */
export const redisMonitorInfo = () => {
  return defHttp.get<RedisMonitorInfoVO>({ url: '/infra/redis/get-monitor-info' })
}

/**
 * 获取redis key列表
 */
export const redisKeysInfo = () => {
  return defHttp.get<RedisKeyInfo[]>({ url: '/infra/redis/get-key-list' })
}
