import Cookies from 'js-cookie'

const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

export function getAccessToken() {
  return Cookies.get(AccessTokenKey)
}

export function getRefreshToken() {
  return Cookies.get(AccessTokenKey)
}

export function setToken(token) {
  Cookies.set(AccessTokenKey, token.accessToken)
  Cookies.set(RefreshTokenKey, token.refreshToken)
}

export function removeToken() {
  Cookies.remove(AccessTokenKey)
  Cookies.remove(RefreshTokenKey)
}
