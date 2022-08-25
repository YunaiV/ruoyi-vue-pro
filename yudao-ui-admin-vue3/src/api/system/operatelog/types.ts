export type OperateLogVO = {
  id: number
  userNickname: string
  traceId: string
  userId: number
  module: string
  name: string
  type: number
  content: string
  exts: object
  requestMethod: string
  requestUrl: string
  userIp: string
  userAgent: string
  javaMethod: string
  javaMethodArgs: string
  startTime: string
  duration: number
  resultCode: number
  resultMsg: string
  resultData: string
}
