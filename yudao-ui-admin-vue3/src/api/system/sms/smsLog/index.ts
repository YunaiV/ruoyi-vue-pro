import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

// 查询短信日志列表
export const getSmsLogPageApi = (params) => {
  return request.get({ url: '/system/sms-log/page', params })
}

// 导出短信日志
export const exportSmsLogApi = (params) => {
  return request.download({ url: '/system/sms-log/export', params })
}
