import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

// 查询用户个人信息
export const getUserProfileApi = () => {
  return request.get({ url: '/system/user/profile/get' })
}

// 修改用户个人信息
export const updateUserProfileApi = (params) => {
  return request.put({ url: '/system/user/profile/update', params })
}

// 用户密码重置
export const updateUserPwdApi = (oldPassword: string, newPassword: string) => {
  return request.put({
    url: '/system/user/profile/update-password',
    params: {
      oldPassword: oldPassword,
      newPassword: newPassword
    }
  })
}

// 用户头像上传
export const uploadAvatarApi = (data) => {
  return request.upload({ url: '/system/user/profile/update-avatar', data: data })
}
