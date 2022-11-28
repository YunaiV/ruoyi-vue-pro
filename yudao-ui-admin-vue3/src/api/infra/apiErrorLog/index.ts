import request from '@/config/axios'

export interface ApiErrorLogVO {
  id: number
  userId: string
  userIp: string
  userAgent: string
  userType: string
  applicationName: string
  requestMethod: string
  requestParams: string
  requestUrl: string
  exceptionTime: string
  exceptionName: string
  exceptionStackTrace: string
  processUserId: string
  processStatus: number
  resultCode: number
}

// 查询列表API 访问日志
export const getApiErrorLogPageApi = (params) => {
  return request.get({ url: '/infra/api-error-log/page', params })
}

// 更新 API 错误日志的处理状态
export const updateApiErrorLogPageApi = (id: number, processStatus: number) => {
  return request.put({
    url: '/infra/api-error-log/update-status?id=' + id + '&processStatus=' + processStatus
  })
}

// 导出API 访问日志
export const exportApiErrorLogApi = (params) => {
  return request.download({
    url: '/infra/api-error-log/export-excel',
    params
  })
}
