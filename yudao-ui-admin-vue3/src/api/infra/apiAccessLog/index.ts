import request from '@/config/axios'

export interface ApiAccessLogVO {
  id: number
  traceId: string
  userId: string
  userType: string
  applicationName: string
  requestMethod: string
  requestParams: string
  requestUrl: string
  beginTime: string
  endTIme: string
  duration: string
  resultCode: number
}

// 查询列表API 访问日志
export const getApiAccessLogPageApi = (params) => {
  return request.get({ url: '/infra/api-access-log/page', params })
}

// 导出API 访问日志
export const exportApiAccessLogApi = (params) => {
  return request.download({ url: '/infra/api-access-log/export-excel', params })
}
