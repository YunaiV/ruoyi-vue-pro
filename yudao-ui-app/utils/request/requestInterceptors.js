/**
 * 请求拦截
 * @param {Object} http
 */
module.exports = vm => {
  uni.$u.http.interceptors.request.use(
    config => {
      // 可使用async await 做异步操作
      // 初始化请求拦截器时，会执行此方法，此时data为undefined，赋予默认{}
      config.data = config.data || {}
      if (vm.$store.getters.hasLogin) {
        config.header.Authorization = 'Bearer ' + vm.$store.getters.accessToken
      }
      return config
    },
    (
      config // 可使用async await 做异步操作
    ) => Promise.reject(config)
  )
}
