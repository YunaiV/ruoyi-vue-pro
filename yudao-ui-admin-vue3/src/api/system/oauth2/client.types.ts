export type OAuth2ClientVo = {
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
  createTime: string
}
