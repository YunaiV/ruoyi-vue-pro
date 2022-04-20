/**
 * 响应拦截
 * @param {Object} http
 */
module.exports = vm => {
  uni.$u.http.interceptors.response.use(
    res => {
      //对响应成功做点什么 可使用async await 做异步操作
      //可以根据业务情况做相应的处理
      if (res.data.code === 0) {
        return res.data
      } else {
        console.log(res)
        //错误信息统一处理
        uni.$u.toast(res.data.msg)
        return Promise.reject(res)
      }
    },
    err => {
      //对响应错误做点什么 （statusCode !== 200）
      console.log(err)
      uni.$u.toast('响应错误' + err.statusCode)
      return Promise.reject(err)
    }
  )
}
