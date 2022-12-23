import request from '@/config/axios'

export interface JobLogVO {
  id: number
  jobId: number
  handlerName: string
  handlerParam: string
  cronExpression: string
  executeIndex: string
  beginTime: string
  endTime: string
  duration: string
  status: number
  createTime: string
}

export interface JobLogPageReqVO extends PageParam {
  jobId?: number
  handlerName?: string
  beginTime?: string
  endTime?: string
  status?: number
}

export interface JobLogExportReqVO {
  jobId?: number
  handlerName?: string
  beginTime?: string
  endTime?: string
  status?: number
}

// 任务日志列表
export const getJobLogPageApi = (params: JobLogPageReqVO) => {
  return request.get({ url: '/infra/job-log/page', params })
}

// 任务日志详情
export const getJobLogApi = (id: number) => {
  return request.get({ url: '/infra/job-log/get?id=' + id })
}

// 导出定时任务日志
export const exportJobLogApi = (params: JobLogExportReqVO) => {
  return request.download({
    url: '/infra/job-log/export-excel',
    params
  })
}
