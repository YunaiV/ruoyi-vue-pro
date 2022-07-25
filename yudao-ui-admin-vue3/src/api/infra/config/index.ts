import { useAxios } from '@/hooks/web/useAxios'
import type { ConfigVO } from './types'

const request = useAxios()

// 查询参数列表
export const getConfigPageApi = (params) => {
  return request.get({ url: '/infra/config/page', params })
}

// 查询参数详情
export const getConfigApi = (id: number) => {
  return request.get({ url: '/infra/config/get?id=' + id })
}

// 根据参数键名查询参数值
export const getConfigKeyApi = (configKey: string) => {
  return request.get({ url: '/infra/config/get-value-by-key?key=' + configKey })
}

// 新增参数
export const createConfigApi = (data: ConfigVO) => {
  return request.post({ url: '/infra/config/create', data })
}

// 修改参数
export const updateConfigApi = (data: ConfigVO) => {
  return request.put({ url: '/infra/config/update', data })
}

// 删除参数
export const deleteConfigApi = (id: number) => {
  return request.delete({ url: '/infra/config/delete?id=' + id })
}

// 导出参数
export const exportConfigApi = (params) => {
  return request.download({ url: '/infra/config/export', params })
}
