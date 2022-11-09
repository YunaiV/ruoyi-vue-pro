import { useCache } from '@/hooks/web/useCache'
import { TokenType } from '@/api/login/types'
import { decrypt, encrypt } from '@/utils/jsencrypt'

const { wsCache } = useCache()
const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

// 获取token
export const getAccessToken = () => {
  // 此处与TokenKey相同，此写法解决初始化时Cookies中不存在TokenKey报错
  return wsCache.get('ACCESS_TOKEN')
}

// 刷新token
export const getRefreshToken = () => {
  return wsCache.get(RefreshTokenKey)
}

// 设置token
export const setToken = (token: TokenType) => {
  wsCache.set(RefreshTokenKey, token.refreshToken, { exp: token.expiresTime })
  wsCache.set(AccessTokenKey, token.accessToken)
}

// 删除token
export const removeToken = () => {
  wsCache.delete(AccessTokenKey)
  wsCache.delete(RefreshTokenKey)
}
// ========== 账号相关 ==========

const UsernameKey = 'USERNAME'
const PasswordKey = 'PASSWORD'
const RememberMeKey = 'REMEMBER_ME'

export const getUsername = () => {
  return wsCache.get(UsernameKey)
}

export const setUsername = (username: string) => {
  wsCache.set(UsernameKey, username)
}

export const removeUsername = () => {
  wsCache.delete(UsernameKey)
}

export const getPassword = () => {
  const password = wsCache.get(PasswordKey)
  return password ? decrypt(password) : undefined
}

export const setPassword = (password: string) => {
  wsCache.set(PasswordKey, encrypt(password))
}

export const removePassword = () => {
  wsCache.delete(PasswordKey)
}

export const getRememberMe = () => {
  return wsCache.get(RememberMeKey) === 'true'
}

export const setRememberMe = (rememberMe: string) => {
  wsCache.set(RememberMeKey, rememberMe)
}

export const removeRememberMe = () => {
  wsCache.delete(RememberMeKey)
}

// ========== 租户相关 ==========

const TenantIdKey = 'TENANT_ID'
const TenantNameKey = 'TENANT_NAME'

export const getTenantName = () => {
  return wsCache.get(TenantNameKey)
}

export const setTenantName = (username: string) => {
  wsCache.set(TenantNameKey, username)
}

export const removeTenantName = () => {
  wsCache.delete(TenantNameKey)
}

export const getTenantId = () => {
  return wsCache.get(TenantIdKey)
}

export const setTenantId = (username: string) => {
  wsCache.set(TenantIdKey, username)
}

export const removeTenantId = () => {
  wsCache.delete(TenantIdKey)
}
