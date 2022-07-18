import { defHttp } from '@/config/axios'
import { getRefreshToken } from '@/utils/auth'
import type { UserLoginVO, TokenType, UserInfoVO } from './types'

export interface CodeImgResult {
  captchaOnOff: boolean
  img: string
  uuid: string
}
export interface SmsCodeVO {
  mobile: string
  scene: number
}
export interface SmsLoginVO {
  mobile: string
  code: string
}

// 获取验证码
export const getCodeImgApi = () => {
  return defHttp.get<CodeImgResult>({ url: '/system/captcha/get-image' })
}

// 登录
export const loginApi = (params: UserLoginVO) => {
  return defHttp.post<TokenType>({ url: '/system/auth/login', params })
}

// 刷新访问令牌
export const refreshToken = () => {
  return defHttp.post({ url: '/system/auth/refresh-token?refreshToken=' + getRefreshToken() })
}

// 使用租户名，获得租户编号
export const getTenantIdByNameApi = (name: string) => {
  return defHttp.get({ url: '/system/tenant/get-id-by-name?name=' + name })
}

// 登出
export const loginOutApi = () => {
  return defHttp.delete({ url: '/system/auth/logout' })
}

// 获取用户权限信息
export const getInfoApi = () => {
  return defHttp.get<UserInfoVO>({ url: '/system/auth/get-permission-info' })
}

// 路由
export const getAsyncRoutesApi = () => {
  return defHttp.get({ url: '/system/auth/list-menus' })
}

//获取登录验证码
export const sendSmsCodeApi = (params: SmsCodeVO) => {
  return defHttp.post({ url: '/system/auth/send-sms-code', params })
}

// 短信验证码登录
export const smsLoginApi = (params: SmsLoginVO) => {
  return defHttp.post({ url: '/system/auth/sms-login', params })
}
