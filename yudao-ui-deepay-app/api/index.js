import { useThemeStore } from '@/store/theme'

const BASE_URL = 'https://api.deepay.ai'

function request(method, path, data = {}) {
  const token = uni.getStorageSync('deepay_token') || ''
  return new Promise((resolve, reject) => {
    uni.request({
      url:    BASE_URL + path,
      method: method.toUpperCase(),
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: 'Bearer ' + token } : {}),
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          reject(res)
        }
      },
      fail: reject,
    })
  })
}

export const api = {
  get:    (path)        => request('GET',    path),
  post:   (path, data)  => request('POST',   path, data),
  put:    (path, data)  => request('PUT',    path, data),
  delete: (path)        => request('DELETE', path),
}

// ── Auth ──────────────────────────────────────────────
export const authApi = {
  login:  (data) => api.post('/api/auth/login', data),
  logout: ()     => api.post('/api/auth/logout'),
}

// ── AI Design ─────────────────────────────────────────
export const designApi = {
  getQuota:    (uid)   => api.get(`/api/quota/${uid}`),
  generate:    (data)  => api.post('/api/ai/generate', data),
  getTask:     (id)    => api.get(`/api/ai/task/${id}`),
  getHistory:  ()      => api.get('/api/chat/history'),
}

export default api
