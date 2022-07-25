import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

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
