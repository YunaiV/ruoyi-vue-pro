import { defHttp } from '@/config/axios'
import type { ConfigVO } from './types'

// 查询参数列表
export const getConfigPageApi = ({ params }) => {
  return defHttp.get<PageResult<ConfigVO>>({ url: '/infra/config/page', params })
}

// 查询参数详情
export const getConfigApi = (id: number) => {
  return defHttp.get<ConfigVO>({ url: '/infra/config/get?id=' + id })
}

// 根据参数键名查询参数值
export const getConfigKeyApi = (configKey: string) => {
  return defHttp.get<ConfigVO>({ url: '/infra/config/get-value-by-key?key=' + configKey })
}

// 新增参数
export const createConfigApi = (params: ConfigVO) => {
  return defHttp.post({ url: '/infra/config/create', params })
}

// 修改参数
export const updateConfigApi = (params: ConfigVO) => {
  return defHttp.put({ url: '/infra/config/update', params })
}

// 删除参数
export const deleteConfigApi = (id: number) => {
  return defHttp.delete({ url: '/infra/config/delete?id=' + id })
}

// 导出参数
export const exportConfigApi = ({ params }) => {
  return defHttp.get({ url: '/infra/config/export', params, responseType: 'blob' })
}
