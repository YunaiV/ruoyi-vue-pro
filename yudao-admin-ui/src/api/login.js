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

// 社交授权的跳转
export function socialAuthRedirect(type, redirectUri) {
  return request({
    url: '/social-auth-redirect?type=' + type + '&redirectUri=' + redirectUri,
    method: 'get'
  })
}

// 社交登录，使用 code 授权码
export function socialLogin(type, code, state) {
  return request({
    url: '/social-login',
    method: 'post',
    data: {
      type,
      code,
      state
    }
  })
}

// 社交登录，使用 code 授权码 + + 账号密码
export function socialLogin2(type, code, state, username, password) {
  return request({
    url: '/social-login2',
    method: 'post',
    data: {
      type,
      code,
      state,
      username,
      password
    }
  })
}

// 社交绑定，使用 code 授权码
export function socialBind(type, code, state) {
  return request({
    url: '/social-bind',
    method: 'post',
    data: {
      type,
      code,
      state,
    }
  })
}

// 取消社交绑定
export function socialUnbind(type, unionId) {
  return request({
    url: '/social-unbind',
    method: 'delete',
    data: {
      type,
      unionId
    }
  })
}
