import Cookies from 'js-cookie'
import { TokenType } from '@/api/login/types'
import { decrypt, encrypt } from '@/utils/jsencrypt'

const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

// 获取token
export const getAccessToken = () => {
  // 此处与TokenKey相同，此写法解决初始化时Cookies中不存在TokenKey报错
  return Cookies.get(AccessTokenKey) ? Cookies.get(AccessTokenKey) : Cookies.get('ACCESS_TOKEN')
}

// 刷新token
export const getRefreshToken = () => {
  return Cookies.get(RefreshTokenKey)
}

// 设置token
export const setToken = (token: TokenType) => {
  Cookies.set(RefreshTokenKey, token.refreshToken, token.expiresTime)
  Cookies.set(AccessTokenKey, token.accessToken)
}

// 删除token
export const removeToken = () => {
  Cookies.remove(AccessTokenKey)
  Cookies.remove(RefreshTokenKey)
}

/** 格式化token（jwt格式） */
export const formatToken = (token: string): string => {
  return 'Bearer ' + token
}
// ========== 账号相关 ==========

const UsernameKey = 'USERNAME'
const PasswordKey = 'PASSWORD'
const RememberMeKey = 'REMEMBER_ME'

export const getUsername = () => {
  return Cookies.get(UsernameKey)
}

export const setUsername = (username: string) => {
  Cookies.set(UsernameKey, username)
}

export const removeUsername = () => {
  Cookies.remove(UsernameKey)
}

export const getPassword = () => {
  const password = Cookies.get(PasswordKey)
  return password ? decrypt(password) : undefined
}

export const setPassword = (password: string) => {
  Cookies.set(PasswordKey, encrypt(password))
}

export const removePassword = () => {
  Cookies.remove(PasswordKey)
}

export const getRememberMe = () => {
  return Cookies.get(RememberMeKey) === 'true'
}

export const setRememberMe = (rememberMe: string) => {
  Cookies.set(RememberMeKey, rememberMe)
}

export const removeRememberMe = () => {
  Cookies.remove(RememberMeKey)
}

// ========== 租户相关 ==========

const TenantIdKey = 'TENANT_ID'
const TenantNameKey = 'TENANT_NAME'

export const getTenantName = () => {
  return Cookies.get(TenantNameKey)
}

export const setTenantName = (username: string) => {
  Cookies.set(TenantNameKey, username)
}

export const removeTenantName = () => {
  Cookies.remove(TenantNameKey)
}

export const getTenantId = () => {
  return Cookies.get(TenantIdKey)
}

export const setTenantId = (username: string) => {
  Cookies.set(TenantIdKey, username)
}

export const removeTenantId = () => {
  Cookies.remove(TenantIdKey)
}
