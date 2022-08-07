import { useAxios } from '@/hooks/web/useAxios'
import type { JobVO } from './types'

const request = useAxios()

// 任务列表
export const getJobPageApi = (params) => {
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
export const exportJobApi = (params) => {
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
