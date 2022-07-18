import { defHttp } from '@/config/axios'
import type { JobVO } from './types'

// 任务列表
export const getJobPageApi = ({ params }) => {
  return defHttp.get<PageResult<JobVO>>({ url: '/infra/job/page', params })
}

// 任务详情
export const getJobApi = (id: number) => {
  return defHttp.get<JobVO>({ url: '/infra/job/get?id=' + id })
}

// 新增任务
export const createJobApi = (params: JobVO) => {
  return defHttp.post({ url: '/infra/job/create', params })
}

// 修改定时任务调度
export const updateJobApi = (params: JobVO) => {
  return defHttp.put({ url: '/infra/job/update', params })
}

// 删除定时任务调度
export const deleteJobApi = (id: number) => {
  return defHttp.delete({ url: '/infra/job/delete?id=' + id })
}

// 导出定时任务调度
export const exportJobApi = (params) => {
  return defHttp.get({
    url: '/infra/job/export-excel',
    params,
    responseType: 'blob'
  })
}

// 任务状态修改
export const updateJobStatusApi = (id: number, status: number) => {
  const data = {
    id,
    status
  }
  return defHttp.put({ url: '/infra/job/update-status', data: data })
}

// 定时任务立即执行一次
export const runJobApi = (id: number) => {
  return defHttp.put({ url: '/infra/job/trigger?id=' + id })
}

// 获得定时任务的下 n 次执行时间
export const getJobNextTimesApi = (id: number) => {
  return defHttp.get({ url: '/infra/job/get_next_times?id=' + id })
}
