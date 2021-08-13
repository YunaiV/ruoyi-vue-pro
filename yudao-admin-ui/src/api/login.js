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

// 接入第三方登录
export function giteeLogin() {
  return request({
    url: '/auth2/authorization/gitee',
    method: 'get'
  })
}

export function getToken(path) {
  console.log({path});
  return request({
    url: '/auth2/login/gitee' +  path,
    method: 'get'
  })
}
