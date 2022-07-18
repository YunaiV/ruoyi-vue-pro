export type ApiAccessLogVO = {
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
