import { defHttp } from '@/config/axios'
import type { JobLogVO } from './types'

// 任务日志列表
export const getJobLogPageApi = ({ params }) => {
  return defHttp.get<PageResult<JobLogVO>>({ url: '/infra/job-log/page', params })
}

// 任务日志详情
export const getJobLogApi = (id: number) => {
  return defHttp.get<JobLogVO>({ url: '/infra/job-log/get?id=' + id })
}

// 导出定时任务日志
export const exportJobLogApi = (params) => {
  return defHttp.get({
    url: '/infra/job-log/export-excel',
    params,
    responseType: 'blob'
  })
}
