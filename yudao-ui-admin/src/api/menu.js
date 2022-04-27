import request from '@/utils/request'

// 获取路由
export const getRouters = () => {
  return request({
    url: '/system/auth/list-menus',
    method: 'get'
  })
}
