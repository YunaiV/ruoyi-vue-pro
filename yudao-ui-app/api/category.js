//请求工具参考https://ext.dcloud.net.cn/plugin?id=392
const { http } = uni.$u

// 查询分类列表
export const categoryListData = params => http.get('product/category/list', { params })
