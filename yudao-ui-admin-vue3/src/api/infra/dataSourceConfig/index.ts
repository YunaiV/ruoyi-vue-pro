import { useAxios } from '@/hooks/web/useAxios'
import type { DataSourceConfigVO } from './types'

const request = useAxios()

// 查询数据源配置列表
export const getDataSourceConfigListApi = () => {
  return request.get({ url: '/infra/data-source-config/list' })
}

// 查询数据源配置详情
export const getDataSourceConfigApi = (id: number) => {
  return request.get({ url: '/infra/data-source-config/get?id=' + id })
}

// 新增数据源配置
export const createDataSourceConfigApi = (data: DataSourceConfigVO) => {
  return request.post({ url: '/infra/data-source-config/create', data })
}

// 修改数据源配置
export const updateDataSourceConfigApi = (data: DataSourceConfigVO) => {
  return request.put({ url: '/infra/data-source-config/update', data })
}

// 删除数据源配置
export const deleteDataSourceConfigApi = (id: number) => {
  return request.delete({ url: '/infra/data-source-config/delete?id=' + id })
}
