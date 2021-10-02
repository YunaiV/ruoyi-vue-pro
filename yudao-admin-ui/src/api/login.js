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
    url: '/login',
    method: 'post',
    data: data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/get-permission-info',
    method: 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/system/captcha/get-image',
    method: 'get'
  })
}

// 三方登陆的跳转
export function thirdLoginRedirect(type, redirectUri) {
  return request({
    url: '/third-login-redirect?type=' + type + '&redirectUri=' + redirectUri,
    method: 'get'
  })
}

// 三方登陆，使用 code 授权码
export function thirdLogin(type, code, state) {
  return request({
    url: '/third-login',
    method: 'post',
    data: {
      type,
      code,
      state
    }
  })
}
