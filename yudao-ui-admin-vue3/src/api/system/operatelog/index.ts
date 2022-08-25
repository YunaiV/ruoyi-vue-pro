import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

// 查询操作日志列表
export const getOperateLogPageApi = (params) => {
  return request.get({ url: '/system/operate-log/page', params })
}
// 导出操作日志
export const exportOperateLogApi = (params) => {
  return request.download({ url: '/system/operate-log/export', params })
}
