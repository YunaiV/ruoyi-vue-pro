const { http } = uni.$u

/* index */
// 获取滚动图数据
export const getBannerData = params => http.get('/api/index', params)
// 获取滚动通知数据
export const getNoticeData = params => http.get('/api/notice', params)
