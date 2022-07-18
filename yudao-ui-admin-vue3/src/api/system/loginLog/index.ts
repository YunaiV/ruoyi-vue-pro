import { defHttp } from '@/config/axios'
import type { LoginLogVO } from './types'

// 查询登录日志列表
export const getLoginLogPageApi = ({ params }) => {
  return defHttp.get<PageResult<LoginLogVO>>({ url: '/system/login-log/page', params })
}
// 导出登录日志
export const exportLoginLogApi = (params) => {
  return defHttp.get({ url: '/system/login-log/export', params, responseType: 'blob' })
}
