import request from '@/config/axios'

export type OperateLogVO = {
  id: number
  userNickname: string
  traceId: string
  userId: number
  module: string
  name: string
  type: number
  content: string
  exts: Map<String, Object>
  requestMethod: string
  requestUrl: string
  userIp: string
  userAgent: string
  javaMethod: string
  javaMethodArgs: string
  startTime: Date
  duration: number
  resultCode: number
  resultMsg: string
  resultData: string
}

export interface OperateLogPageReqVO extends PageParam {
  module?: string
  userNickname?: string
  type?: number
  success?: boolean
  startTime?: Date[]
}

// 查询操作日志列表
export const getOperateLogPageApi = (params: OperateLogPageReqVO) => {
  return request.get({ url: '/system/operate-log/page', params })
}
// 导出操作日志
export const exportOperateLogApi = (params: OperateLogPageReqVO) => {
  return request.download({ url: '/system/operate-log/export', params })
}
