import request from '@/config/axios'

export interface ApiErrorLogVO {
  id: number
  traceId: string
  userId: number
  userType: number
  applicationName: string
  requestMethod: string
  requestParams: string
  requestUrl: string
  userIp: string
  userAgent: string
  exceptionTime: Date
  exceptionName: string
  exceptionMessage: string
  exceptionRootCauseMessage: string
  exceptionStackTrace: string
  exceptionClassName: string
  exceptionFileName: string
  exceptionMethodName: string
  exceptionLineNumber: number
  processUserId: number
  processStatus: number
  processTime: Date
  resultCode: number
  createTime: Date
}

export interface ApiErrorLogPageReqVO extends PageParam {
  userId?: number
  userType?: number
  applicationName?: string
  requestUrl?: string
  exceptionTime?: Date[]
  processStatus: number
}

export interface ApiErrorLogExportReqVO {
  userId?: number
  userType?: number
  applicationName?: string
  requestUrl?: string
  exceptionTime?: Date[]
  processStatus: number
}

// 查询列表API 访问日志
export const getApiErrorLogPageApi = (params: ApiErrorLogPageReqVO) => {
  return request.get({ url: '/infra/api-error-log/page', params })
}

// 更新 API 错误日志的处理状态
export const updateApiErrorLogPageApi = (id: number, processStatus: number) => {
  return request.put({
    url: '/infra/api-error-log/update-status?id=' + id + '&processStatus=' + processStatus
  })
}

// 导出API 访问日志
export const exportApiErrorLogApi = (params: ApiErrorLogExportReqVO) => {
  return request.download({
    url: '/infra/api-error-log/export-excel',
    params
  })
}
