const { http } = uni.$u

// 获得订单交易分页
export const getOrderPage = params => http.get('/trade/order/page', { params })
// 获得交易订单详情
export const getOrderDetail = params => http.get('/trade/order/get-detail', { params })
