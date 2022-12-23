import request from '@/config/axios'

export interface OAuth2ClientVO {
  id: number
  clientId: string
  secret: string
  name: string
  logo: string
  description: string
  status: number
  accessTokenValiditySeconds: number
  refreshTokenValiditySeconds: number
  redirectUris: string[]
  autoApprove: boolean
  authorizedGrantTypes: string[]
  scopes: string[]
  authorities: string[]
  resourceIds: string[]
  additionalInformation: string
  isAdditionalInformationJson: boolean
  createTime: Date
}

export interface OAuth2ClientPageReqVO extends PageParam {
  name?: string
  status?: number
}
// 查询 OAuth2列表
export const getOAuth2ClientPageApi = (params: OAuth2ClientPageReqVO) => {
  return request.get({ url: '/system/oauth2-client/page', params })
}

// 查询 OAuth2详情
export const getOAuth2ClientApi = (id: number) => {
  return request.get({ url: '/system/oauth2-client/get?id=' + id })
}

// 新增 OAuth2
export const createOAuth2ClientApi = (data: OAuth2ClientVO) => {
  return request.post({ url: '/system/oauth2-client/create', data })
}

// 修改 OAuth2
export const updateOAuth2ClientApi = (data: OAuth2ClientVO) => {
  return request.put({ url: '/system/oauth2-client/update', data })
}

// 删除 OAuth2
export const deleteOAuth2ClientApi = (id: number) => {
  return request.delete({ url: '/system/oauth2-client/delete?id=' + id })
}
