import { defHttp } from '@/config/axios'
import { ApiErrorLogVO } from './types'

// 查询列表API 访问日志
export const getApiErrorLogPageApi = ({ params }) => {
  return defHttp.get<PageResult<ApiErrorLogVO>>({ url: '/infra/api-error-log/page', params })
}

// 更新 API 错误日志的处理状态
export const updateApiErrorLogPageApi = (id: number, processStatus: number) => {
  return defHttp.put({
    url: '/infra/api-error-log/update-status?id=' + id + '&processStatus=' + processStatus
  })
}

// 导出API 访问日志
export const exportApiErrorLogApi = ({ params }) => {
  return defHttp.get({ url: '/infra/api-error-log/export-excel', params, responseType: 'blob' })
}
