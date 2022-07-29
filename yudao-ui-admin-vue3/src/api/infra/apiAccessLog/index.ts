import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

// 查询列表API 访问日志
export const getApiAccessLogPageApi = (params) => {
  return request.get({ url: '/infra/api-access-log/page', params })
}

// 导出API 访问日志
export const exportApiAccessLogApi = (params) => {
  return request.download({ url: '/infra/api-access-log/export-excel', params })
}
