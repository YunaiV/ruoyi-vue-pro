import { defHttp } from '@/config/axios'
import { OAuth2ClientVo } from './client.types'

// 查询 OAuth2列表
export const getOAuth2ClientPageApi = ({ params }) => {
  return defHttp.get<PageResult<OAuth2ClientVo>>({ url: '/system/oauth2-client/page', params })
}

// 查询 OAuth2详情
export const getOAuth2ClientApi = (id: number) => {
  return defHttp.get<OAuth2ClientVo>({ url: '/system/oauth2-client/get?id=' + id })
}

// 新增 OAuth2
export const createOAuth2ClientApi = (params: OAuth2ClientVo) => {
  return defHttp.post({ url: '/system/oauth2-client/create', params })
}

// 修改 OAuth2
export const updateOAuth2ClientApi = (params: OAuth2ClientVo) => {
  return defHttp.put({ url: '/system/oauth2-client/update', params })
}

// 删除 OAuth2
export const deleteOAuth2ClientApi = (id: number) => {
  return defHttp.delete({ url: '/system/oauth2-client/delete?id=' + id })
}
