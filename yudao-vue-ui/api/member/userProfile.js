import { request } from '@/common/js/request.js'

// 获得用户的基本信息
export function getUserInfo() {
  return request({
    url: 'member/user/profile/get',
    method: 'get'
  })
}