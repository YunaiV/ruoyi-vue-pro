import { defHttp } from '@/config/axios'
import type { DataSourceConfigVO } from './types'

// 查询数据源配置列表
export const getDataSourceConfigListApi = () => {
  return defHttp.get<DataSourceConfigVO[]>({ url: '/infra/data-source-config/list' })
}

// 查询数据源配置详情
export const getDataSourceConfigApi = (id: number) => {
  return defHttp.get<DataSourceConfigVO>({ url: '/infra/data-source-config/get?id=' + id })
}

// 新增数据源配置
export const createDataSourceConfigApi = (params: DataSourceConfigVO) => {
  return defHttp.post({ url: '/infra/data-source-config/create', params })
}

// 修改数据源配置
export const updateDataSourceConfigApi = (params: DataSourceConfigVO) => {
  return defHttp.put({ url: '/infra/data-source-config/update', params })
}

// 删除数据源配置
export const deleteDataSourceConfigApi = (id: number) => {
  return defHttp.delete({ url: '/infra/data-source-config/delete?id=' + id })
}
