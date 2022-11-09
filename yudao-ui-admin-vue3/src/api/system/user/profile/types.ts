export type ProfileDept = {
  id: number
  name: string
}
export type ProfileRole = {
  id: number
  name: string
}
export type ProfilePost = {
  id: number
  name: string
}
export type SocialUser = {
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
export type ProfileVO = {
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
