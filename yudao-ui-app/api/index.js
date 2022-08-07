//请求工具参考https://ext.dcloud.net.cn/plugin?id=392
const { http } = uni.$u

// 获取滚动图数据
export const getBannerData = params => http.get('/index', { params })
// 获取滚动通知数据
export const getNoticeData = params => http.get('/notice', { params })
