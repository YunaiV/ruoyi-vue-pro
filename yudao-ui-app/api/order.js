const { http } = uni.$u

// 获得订单交易分页
export const getOrderPage = params => http.get('/trade/order/page', { params })
