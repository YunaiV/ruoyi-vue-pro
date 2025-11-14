import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemOAuth2TokenApi {
  /** OAuth2.0 令牌信息 */
  export interface OAuth2Token {
    id?: number;
    accessToken: string;
    refreshToken: string;
    userId: number;
    userType: number;
    clientId: string;
    createTime?: Date;
    expiresTime?: Date;
  }
}

/** 查询 OAuth2.0 令牌列表 */
export function getOAuth2TokenPage(params: PageParam) {
  return requestClient.get<PageResult<SystemOAuth2TokenApi.OAuth2Token>>(
    '/system/oauth2-token/page',
    {
      params,
    },
  );
}

/** 删除 OAuth2.0 令牌 */
export function deleteOAuth2Token(accessToken: string) {
  return requestClient.delete(
    `/system/oauth2-token/delete?accessToken=${accessToken}`,
  );
}
