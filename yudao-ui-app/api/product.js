//请求工具参考https://ext.dcloud.net.cn/plugin?id=392
const { http } = uni.$u

// 查询商品spu列表
export const productSpuPage = params => http.get('/product/spu/page', { params })
