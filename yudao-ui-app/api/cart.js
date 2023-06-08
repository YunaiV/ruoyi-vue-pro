const { http } = uni.$u

//获取购物车数据
export const getCartProductDetail = () => http.get('/trade/cart/get-detail')

