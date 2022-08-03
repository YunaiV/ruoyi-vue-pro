import request from '@/utils/request'

// 登录方法
export function login(username, password, code, uuid) {
  const data = {
    username,
    password,
    code,
    uuid
  }
  return request({
    url: '/system/auth/login',
    headers: {
      isToken: false
    },
    'method': 'post',
    'data': data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/system/auth/get-permission-info',
    'method': 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/system/auth/logout',
    'method': 'post'
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/system/captcha/get-image',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}
