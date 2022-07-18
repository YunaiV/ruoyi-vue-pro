import { defHttp } from '@/config/axios'
import type { SmsLogVO } from './types'

// 查询短信日志列表
export const getSmsLogPageApi = ({ params }) => {
  return defHttp.get<PageResult<SmsLogVO>>({ url: '/system/sms-log/page', params })
}

// 导出短信日志
export const exportSmsLogApi = (params) => {
  return defHttp.get({ url: '/system/sms-log/export', params, responseType: 'blob' })
}
