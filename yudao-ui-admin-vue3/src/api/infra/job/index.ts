import request from '@/config/axios'

export interface JobVO {
  id: number
  name: string
  status: number
  handlerName: string
  handlerParam: string
  cronExpression: string
  retryCount: number
  retryInterval: number
  monitorTimeout: number
  createTime: Date
}

export interface JobPageReqVO extends PageParam {
  name?: string
  status?: number
  handlerName?: string
}

export interface JobExportReqVO {
  name?: string
  status?: number
  handlerName?: string
}

// 任务列表
export const getJobPageApi = (params: JobPageReqVO) => {
  return request.get({ url: '/infra/job/page', params })
}

// 任务详情
export const getJobApi = (id: number) => {
  return request.get({ url: '/infra/job/get?id=' + id })
}

// 新增任务
export const createJobApi = (data: JobVO) => {
  return request.post({ url: '/infra/job/create', data })
}

// 修改定时任务调度
export const updateJobApi = (data: JobVO) => {
  return request.put({ url: '/infra/job/update', data })
}

// 删除定时任务调度
export const deleteJobApi = (id: number) => {
  return request.delete({ url: '/infra/job/delete?id=' + id })
}

// 导出定时任务调度
export const exportJobApi = (params: JobExportReqVO) => {
  return request.download({ url: '/infra/job/export-excel', params })
}

// 任务状态修改
export const updateJobStatusApi = (id: number, status: number) => {
  const params = {
    id,
    status
  }
  return request.put({ url: '/infra/job/update-status', params })
}

// 定时任务立即执行一次
export const runJobApi = (id: number) => {
  return request.put({ url: '/infra/job/trigger?id=' + id })
}

// 获得定时任务的下 n 次执行时间
export const getJobNextTimesApi = (id: number) => {
  return request.get({ url: '/infra/job/get_next_times?id=' + id })
}
