import { useCache } from '@/hooks/web/useCache'
import { TokenType } from '@/api/login/types'

const { wsCache } = useCache()
const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

// 获取token
export function getAccessToken() {
  // 此处与TokenKey相同，此写法解决初始化时Cookies中不存在TokenKey报错
  return wsCache.get('ACCESS_TOKEN')
}

// 刷新token
export function getRefreshToken() {
  return wsCache.get(RefreshTokenKey)
}

// 设置token
export function setToken(token: TokenType) {
  wsCache.set(RefreshTokenKey, token.refreshToken, { exp: token.expiresTime })
  wsCache.set(AccessTokenKey, token.accessToken)
}

// 删除token
export function removeToken() {
  wsCache.delete(AccessTokenKey)
  wsCache.delete(RefreshTokenKey)
}
