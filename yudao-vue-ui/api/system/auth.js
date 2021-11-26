import { request } from '@/common/js/request.js'

// 手机号 + 密码登陆
export function login(mobile, password) {
  const data = {
    mobile,
    password
  }
  return request({
    url: 'login',
    method: 'post',
    data: data
  })
}

// 手机号 + 验证码登陆
export function smsLogin(mobile, code) {
  const data = {
    mobile,
    code
  }
  return request({
    url: 'sms-login',
    method: 'post',
    data: data
  })
}