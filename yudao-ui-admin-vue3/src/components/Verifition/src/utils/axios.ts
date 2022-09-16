import axios from 'axios'

axios.defaults.baseURL = import.meta.env.VITE_BASE_URL

const service = axios.create({
  timeout: 40000,
  headers: {
    'X-Requested-With': 'XMLHttpRequest',
    'Content-Type': 'application/json; charset=UTF-8'
  }
})
service.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    Promise.reject(error)
  }
)

// response interceptor
service.interceptors.response.use((response) => {
  const res = response.data
  return res
})
export default service
