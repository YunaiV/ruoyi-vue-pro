/**
 * 响应拦截
 * @param {Object} http
 */
module.exports = (vm) => {
    uni.$u.http.interceptors.response.use((res) => {
        /* 对响应成功做点什么 可使用async await 做异步操作*/
        const data = res.data
        /*
         可以根据业务情况做相应的处理
        */
        return res
    }, (err) => {
        /*  对响应错误做点什么 （statusCode !== 200）*/
        return Promise.reject(err)
    })
}