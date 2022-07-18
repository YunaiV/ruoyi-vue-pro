import { defHttp } from '@/config/axios'
import type { OperateLogVO } from './types'

// 查询操作日志列表
export const getOperateLogPageApi = ({ params }) => {
  return defHttp.get<PageResult<OperateLogVO>>({ url: '/system/operate-log/page', params })
}
// 导出操作日志
export const exportOperateLogApi = (params) => {
  return defHttp.get({ url: '/system/operate-log/export', params, responseType: 'blob' })
}
