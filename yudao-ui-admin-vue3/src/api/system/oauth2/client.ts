import { useAxios } from '@/hooks/web/useAxios'
import { OAuth2ClientVo } from './client.types'

const request = useAxios()

// 查询 OAuth2列表
export const getOAuth2ClientPageApi = (params) => {
  return request.get({ url: '/system/oauth2-client/page', params })
}

// 查询 OAuth2详情
export const getOAuth2ClientApi = (id: number) => {
  return request.get({ url: '/system/oauth2-client/get?id=' + id })
}

// 新增 OAuth2
export const createOAuth2ClientApi = (data: OAuth2ClientVo) => {
  return request.post({ url: '/system/oauth2-client/create', data })
}

// 修改 OAuth2
export const updateOAuth2ClientApi = (data: OAuth2ClientVo) => {
  return request.put({ url: '/system/oauth2-client/update', data })
}

// 删除 OAuth2
export const deleteOAuth2ClientApi = (id: number) => {
  return request.delete({ url: '/system/oauth2-client/delete?id=' + id })
}
