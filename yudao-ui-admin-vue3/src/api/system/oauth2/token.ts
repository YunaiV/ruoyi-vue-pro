import { defHttp } from '@/config/axios'
import { OAuth2TokenVo } from './token.types'

// 查询 token列表
export const getAccessTokenPageApi = ({ params }) => {
  return defHttp.get<PageResult<OAuth2TokenVo>>({ url: '/system/oauth2-token/page', params })
}

// 删除 token
export const deleteAccessTokenApi = (accessToken: number) => {
  return defHttp.delete({ url: '/system/oauth2-token/delete?accessToken=' + accessToken })
}
