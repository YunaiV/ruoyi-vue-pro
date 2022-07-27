export type ApiErrorLogVO = {
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
