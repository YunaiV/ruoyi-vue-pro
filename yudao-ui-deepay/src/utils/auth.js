/**
 * auth.js — 登录态管理
 *
 * token 存储在 localStorage，key = 'deepay_token'。
 * 整个 app 通过 isLoggedIn() 判断登录态，通过 getToken() 拿凭证。
 * 登出时同步清除 token（不清除 deepay_uid，保持设备唯一 ID）。
 */

const KEY_TOKEN = 'deepay_token'

/** 保存 token（登录成功后调用） */
export function setToken(token) {
  localStorage.setItem(KEY_TOKEN, token)
}

/** 读取 token；未登录时返回 null */
export function getToken() {
  return localStorage.getItem(KEY_TOKEN)
}

/** 清除 token（登出时调用） */
export function removeToken() {
  localStorage.removeItem(KEY_TOKEN)
}

/** 是否已登录 */
export function isLoggedIn() {
  return !!getToken()
}
