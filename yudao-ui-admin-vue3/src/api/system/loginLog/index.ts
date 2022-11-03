import request from '@/config/axios'

// 查询登录日志列表
export const getLoginLogPageApi = (params) => {
  return request.get({ url: '/system/login-log/page', params })
}
// 导出登录日志
export const exportLoginLogApi = (params) => {
  return request.download({ url: '/system/login-log/export', params })
}
