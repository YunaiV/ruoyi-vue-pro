//请求工具参考https://ext.dcloud.net.cn/plugin?id=392
const { http } = uni.$u

//使用手机 + 密码登录
export const passwordLogin = data => http.post('/app-api/member/login', data)
//退出登录
export const logout = data => http.post('/app-api/member/logout', data)
//发送手机验证码
export const sendSmsCode = data => http.post('/app-api/member/send-sms-code', data)
//使用手机 + 验证码登录
export const smsLogin = data => http.post('/app-api/member/sms-login', data)

