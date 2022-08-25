import request from '@/utils/request'
import {getRefreshToken} from "@/utils/auth";
import service from "@/utils/request";

// 登录方法
export function login(username, password, code, uuid,
                      socialType, socialCode, socialState) {
  const data = {
    username,
    password,
    code,
    uuid,
    // 社交相关
    socialType, socialCode, socialState
  }
  return request({
    url: '/system/auth/login',
    method: 'post',
    data: data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/system/auth/get-permission-info',
    method: 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/system/auth/logout',
    method: 'post'
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/system/captcha/get-image',
    method: 'get',
    timeout: 20000
  })
}

// 社交授权的跳转
export function socialAuthRedirect(type, redirectUri) {
  return request({
    url: '/system/auth/social-auth-redirect?type=' + type + '&redirectUri=' + redirectUri,
    method: 'get'
  })
}

// 社交快捷登录，使用 code 授权码
export function socialLogin(type, code, state) {
  return request({
    url: '/system/auth/social-login',
    method: 'post',
    data: {
      type,
      code,
      state
    }
  })
}

// 获取登录验证码
export function sendSmsCode(mobile, scene) {
  return request({
    url: '/system/auth/send-sms-code',
    method: 'post',
    data: {
      mobile,
      scene
    }
  })
}

// 短信验证码登录
export function smsLogin(mobile, code) {
  return request({
    url: '/system/auth/sms-login',
    method: 'post',
    data: {
      mobile,
      code
    }
  })
}

// 刷新访问令牌
export function refreshToken() {
  return service({
    url: '/system/auth/refresh-token?refreshToken=' + getRefreshToken(),
    method: 'post'
  })
}

// ========== OAUTH 2.0 相关 ==========

export function getAuthorize(clientId) {
  return request({
    url: '/system/oauth2/authorize?clientId=' + clientId,
    method: 'get'
  })
}

export function authorize(responseType, clientId, redirectUri, state,
                          autoApprove, checkedScopes, uncheckedScopes) {
  // 构建 scopes
  const scopes = {};
  for (const scope of checkedScopes) {
    scopes[scope] = true;
  }
  for (const scope of uncheckedScopes) {
    scopes[scope] = false;
  }
  // 发起请求
  return service({
    url: '/system/oauth2/authorize',
    headers:{
      'Content-type': 'application/x-www-form-urlencoded',
    },
    params: {
      response_type: responseType,
      client_id: clientId,
      redirect_uri: redirectUri,
      state: state,
      auto_approve: autoApprove,
      scope: JSON.stringify(scopes)
    },
    method: 'post'
  })
}

export class socialBindLogin {
}
