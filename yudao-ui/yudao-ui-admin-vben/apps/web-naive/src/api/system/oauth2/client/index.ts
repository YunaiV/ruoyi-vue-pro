import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemOAuth2ClientApi {
  /** OAuth2.0 客户端信息 */
  export interface OAuth2Client {
    id?: number;
    clientId: string;
    secret: string;
    name: string;
    logo: string;
    description: string;
    status: number;
    accessTokenValiditySeconds: number;
    refreshTokenValiditySeconds: number;
    redirectUris: string[];
    autoApprove: boolean;
    authorizedGrantTypes: string[];
    scopes: string[];
    authorities: string[];
    resourceIds: string[];
    additionalInformation: string;
    isAdditionalInformationJson: boolean;
    createTime?: Date;
  }
}

/** 查询 OAuth2.0 客户端列表 */
export function getOAuth2ClientPage(params: PageParam) {
  return requestClient.get<PageResult<SystemOAuth2ClientApi.OAuth2Client>>(
    '/system/oauth2-client/page',
    { params },
  );
}

/** 查询 OAuth2.0 客户端详情 */
export function getOAuth2Client(id: number) {
  return requestClient.get<SystemOAuth2ClientApi.OAuth2Client>(
    `/system/oauth2-client/get?id=${id}`,
  );
}

/** 新增 OAuth2.0 客户端 */
export function createOAuth2Client(data: SystemOAuth2ClientApi.OAuth2Client) {
  return requestClient.post('/system/oauth2-client/create', data);
}

/** 修改 OAuth2.0 客户端 */
export function updateOAuth2Client(data: SystemOAuth2ClientApi.OAuth2Client) {
  return requestClient.put('/system/oauth2-client/update', data);
}

/** 删除 OAuth2.0 客户端 */
export function deleteOAuth2Client(id: number) {
  return requestClient.delete(`/system/oauth2-client/delete?id=${id}`);
}

/** 批量删除 OAuth2.0 客户端 */
export function deleteOAuth2ClientList(ids: number[]) {
  return requestClient.delete(
    `/system/oauth2-client/delete-list?ids=${ids.join(',')}`,
  );
}
