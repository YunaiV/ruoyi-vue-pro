//请求工具参考https://ext.dcloud.net.cn/plugin?id=392
const { http } = uni.$u

//获得用户收件地址列表
export const getAddressList = params => http.get('/member/address/list', params)
//创建用户收件地址
export const createAddress = data => http.post('/member/address/create', data)
//通过ID获得用户收件地址
export const getAddressById = params => http.get('/member/address/get', { params })
//获得默认的用户收件地址
export const getDefaultUserAddress = params => http.get('/member/address/get-default', { params })
//更新用户收件地址
export const updateAddress = params => http.put('/member/address/update', params)
//删除用户收件地址
export const deleteAddress = params => http.delete('/member/address/delete', {}, { params })
