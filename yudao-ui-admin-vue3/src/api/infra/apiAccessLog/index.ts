import { defHttp } from '@/config/axios'
import { ApiAccessLogVO } from './types'

// 查询列表API 访问日志
export const getApiAccessLogPageApi = ({ params }) => {
  return defHttp.get<PageResult<ApiAccessLogVO>>({ url: '/infra/api-access-log/page', params })
}

// 导出API 访问日志
export const exportApiAccessLogApi = (params) => {
  return defHttp.get({ url: '/infra/api-access-log/export-excel', params, responseType: 'blob' })
}
