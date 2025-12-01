import { requestClient } from '#/api/request';

/** OAuth2.0 授权信息响应 */
export namespace SystemOAuth2ClientApi {
  /** 授权信息 */
  export interface AuthorizeInfoRespVO {
    client: {
      logo: string;
      name: string;
    };
    scopes: {
      key: string;
      value: boolean;
    }[];
  }
}

/** 获得授权信息 */
export function getAuthorize(clientId: string) {
  return requestClient.get<SystemOAuth2ClientApi.AuthorizeInfoRespVO>(
    `/system/oauth2/authorize?clientId=${clientId}`,
  );
}

/** 发起授权 */
export function authorize(
  responseType: string,
  clientId: string,
  redirectUri: string,
  state: string,
  autoApprove: boolean,
  checkedScopes: string[],
  uncheckedScopes: string[],
) {
  // 构建 scopes
  const scopes: Record<string, boolean> = {};
  for (const scope of checkedScopes) {
    scopes[scope] = true;
  }
  for (const scope of uncheckedScopes) {
    scopes[scope] = false;
  }

  // 发起请求
  return requestClient.post<string>('/system/oauth2/authorize', null, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    params: {
      response_type: responseType,
      client_id: clientId,
      redirect_uri: redirectUri,
      state,
      auto_approve: autoApprove,
      scope: JSON.stringify(scopes),
    },
  });
}
