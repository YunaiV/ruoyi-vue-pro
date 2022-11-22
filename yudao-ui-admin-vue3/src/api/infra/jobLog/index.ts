import request from '@/config/axios'

export type JobLogVO = {
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

// 任务日志列表
export const getJobLogPageApi = (params) => {
  return request.get({ url: '/infra/job-log/page', params })
}

// 任务日志详情
export const getJobLogApi = (id: number) => {
  return request.get({ url: '/infra/job-log/get?id=' + id })
}

// 导出定时任务日志
export const exportJobLogApi = (params) => {
  return request.download({
    url: '/infra/job-log/export-excel',
    params
  })
}
