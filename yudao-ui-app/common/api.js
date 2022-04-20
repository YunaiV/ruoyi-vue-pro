const { http } = uni.$u

/* login */
//使用手机 + 密码登录
export const passwordLogin = params => http.post('/app-api/member/login', params)
//退出登录
export const logout = params => http.post('/app-api/member/logout', params)
//发送手机验证码
export const sendSmsCode = params => http.post('/app-api/member/send-sms-code', params)
//使用手机 + 验证码登录
export const smsLogin = params => http.post('/app-api/member/sms-login', params)

//获取用户信息
export const getUserInfo = params => http.get('/app-api/member/user/get', params)
//修改用户头像
export const updateAvatar = filePath =>
  http.upload('/app-api/member/user/update-avatar', {
    name: 'avatarFile',
    fileType: 'image',
    filePath: filePath
  })

/* index */
// 获取滚动图数据
export const getBannerData = params => http.get('/app-api/index', params)
// 获取滚动通知数据
export const getNoticeData = params => http.get('/app-api/notice', params)
