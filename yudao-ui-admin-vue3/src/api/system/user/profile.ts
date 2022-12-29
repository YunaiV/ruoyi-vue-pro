import request from '@/config/axios'

export interface ProfileDept {
  id: number
  name: string
}
export interface ProfileRole {
  id: number
  name: string
}
export interface ProfilePost {
  id: number
  name: string
}
export interface SocialUser {
  id: number
  type: number
  openid: string
  token: string
  rawTokenInfo: string
  nickname: string
  avatar: string
  rawUserInfo: string
  code: string
  state: string
}
export interface ProfileVO {
  id: number
  username: string
  nickname: string
  dept: ProfileDept
  roles: ProfileRole[]
  posts: ProfilePost[]
  socialUsers: SocialUser[]
  email: string
  mobile: string
  sex: number
  avatar: string
  status: number
  remark: string
  loginIp: string
  loginDate: Date
  createTime: Date
}

export interface UserProfileUpdateReqVO {
  nickname: string
  email: string
  mobile: string
  sex: number
}

// 查询用户个人信息
export const getUserProfileApi = () => {
  return request.get({ url: '/system/user/profile/get' })
}

// 修改用户个人信息
export const updateUserProfileApi = (data: UserProfileUpdateReqVO) => {
  return request.put({ url: '/system/user/profile/update', data })
}

// 用户密码重置
export const updateUserPwdApi = (oldPassword: string, newPassword: string) => {
  return request.put({
    url: '/system/user/profile/update-password',
    data: {
      oldPassword: oldPassword,
      newPassword: newPassword
    }
  })
}

// 用户头像上传
export const uploadAvatarApi = (data) => {
  return request.upload({ url: '/system/user/profile/update-avatar', data: data })
}
