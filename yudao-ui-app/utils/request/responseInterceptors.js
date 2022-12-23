import errorCode from '@/utils/request/errorCode'
import { refreshToken } from '@/api/auth'

// 需要忽略的提示。忽略后，自动 Promise.reject('error')
const ignoreMsgs = [
  '无效的刷新令牌', // 刷新令牌被删除时，不用提示
  '刷新令牌已过期' // 使用刷新令牌，刷新获取新的访问令牌时，结果因为过期失败，此时需要忽略。否则，会导致继续 401，无法跳转到登出界面
]

// 请求队列
let requestList = []
// 是否正在刷新中
let isRefreshToken = false

/**
 * 响应拦截
 * @param {Object} http
 */
module.exports = vm => {
  uni.$u.http.interceptors.response.use(
    async res => {
      const code = res.data.code || 0
      const msg = res.data.msg || errorCode[code] || errorCode['default']

      if (ignoreMsgs.indexOf(msg) !== -1) {
        // 如果是忽略的错误码，直接返回 msg 异常
        return Promise.reject(msg)
      } else if (code === 401) {
        // 如果未认证，并且未进行刷新令牌，说明可能是访问令牌过期了
        if (!isRefreshToken) {
          isRefreshToken = true
          // 1. 如果获取不到刷新令牌，则只能执行登出操作
          if (!vm.$store.getters.refreshToken()) {
            vm.$store.commit('CLEAR_LOGIN_INFO')
            return Promise.reject(res)
          }
          // 2. 进行刷新访问令牌
          try {
            const refreshTokenRes = await refreshToken()
            // 2.1 刷新成功，则回放队列的请求 + 当前请求
            vm.$store.commit('SET_TOKEN', refreshTokenRes.data)
            requestList.forEach(cb => cb())
            return uni.$u.http.request(res.config)
          } catch (e) {
            // 为什么需要 catch 异常呢？刷新失败时，请求因为 Promise.reject 触发异常。
            // 2.2 刷新失败，只回放队列的请求
            requestList.forEach(cb => cb())
            // 登出。即不回放当前请求！不然会形成递归
            vm.$store.commit('CLEAR_LOGIN_INFO')
            return Promise.reject(res)
          } finally {
            requestList = []
            isRefreshToken = false
          }
        } else {
          // 添加到队列，等待刷新获取到新的令牌
          return new Promise(resolve => {
            requestList.push(() => {
              res.config.header.Authorization = 'Bearer ' + vm.$store.getters.accessToken // 让每个请求携带自定义token 请根据实际情况自行修改
              resolve(uni.$u.http.request(res.config))
            })
          })
        }
      } else if (code === 500) {
        uni.$u.toast(msg)
        return Promise.reject(res)
      } else if (code === 901) {
        uni.$u.toast('演示模式，无法进行写操作')
        return Promise.reject(res)
      } else if (code !== 0) {
        if (msg === '无效的刷新令牌') {
          // hard coding：忽略这个提示，直接登出
          console.log(msg)
        } else {
          uni.$u.toast(msg)
        }
        return Promise.reject(res)
      } else {
        return res.data
      }
    },
    err => {
      console.log(err)
      let { message } = err
      if (!message) {
        message = '系统发生未知错误'
      }else if (message === 'Network Error') {
        message = '后端接口连接异常'
      } else if (message.includes('timeout')) {
        message = '系统接口请求超时'
      } else if (message.includes('Request failed with status code')) {
        message = '系统接口' + message.substring(message.length - 3) + '异常'
      }
      uni.$u.toast(message)
      return Promise.reject(err)
    }
  )
}
