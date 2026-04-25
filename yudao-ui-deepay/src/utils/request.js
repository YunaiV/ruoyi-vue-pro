/**
 * request.js — 统一 HTTP 客户端
 *
 * 特性：
 *   ① 优先发送 Authorization: Bearer <token>（已登录）
 *   ② 未登录时降级发送 X-User-Id（匿名设备 ID，保持向后兼容）
 *   ③ 自动解包 CommonResult { code, data, msg }
 *   ④ 统一 30s 超时
 *   ⑤ 整个 app 共用一个实例，不重复创建
 *
 * 用法：
 *   import http from '@/utils/request'
 *   const res = await http.get('/api/xxx')
 */
import axios from 'axios'
import { getToken } from '@/utils/auth'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 30_000,
  headers: { 'Content-Type': 'application/json' },
})

// ── 请求拦截：注入用户身份 ───────────────────────────────────────────
http.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    // 已登录 → 用真实 token
    config.headers['Authorization'] = `Bearer ${token}`
  } else {
    // 未登录 → 降级到匿名设备 ID（MVP 兼容）
    const uid = localStorage.getItem('deepay_uid')
    if (uid) config.headers['X-User-Id'] = uid
  }
  return config
})

// ── 响应拦截：解包 CommonResult ──────────────────────────────────────
http.interceptors.response.use(
  res => res.data?.data ?? res.data,
  err => Promise.reject(err),
)

export default http
