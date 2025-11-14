import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemSocialUserApi {
  /** 社交用户信息 */
  export interface SocialUser {
    id?: number;
    type: number;
    openid: string;
    token: string;
    rawTokenInfo: string;
    nickname: string;
    avatar: string;
    rawUserInfo: string;
    code: string;
    state: string;
    createTime?: Date;
    updateTime?: Date;
  }

  /** 社交绑定请求 */
  export interface SocialUserBindReqVO {
    type: number;
    code: string;
    state: string;
  }

  /** 取消社交绑定请求 */
  export interface SocialUserUnbindReqVO {
    type: number;
    openid: string;
  }
}

/** 查询社交用户列表 */
export function getSocialUserPage(params: PageParam) {
  return requestClient.get<PageResult<SystemSocialUserApi.SocialUser>>(
    '/system/social-user/page',
    { params },
  );
}

/** 查询社交用户详情 */
export function getSocialUser(id: number) {
  return requestClient.get<SystemSocialUserApi.SocialUser>(
    `/system/social-user/get?id=${id}`,
  );
}

/** 社交绑定，使用 code 授权码 */
export function socialBind(data: SystemSocialUserApi.SocialUserBindReqVO) {
  return requestClient.post<boolean>('/system/social-user/bind', data);
}

/** 取消社交绑定 */
export function socialUnbind(data: SystemSocialUserApi.SocialUserUnbindReqVO) {
  return requestClient.delete<boolean>('/system/social-user/unbind', { data });
}

/** 获得绑定社交用户列表 */
export function getBindSocialUserList() {
  return requestClient.get<SystemSocialUserApi.SocialUser[]>(
    '/system/social-user/get-bind-list',
  );
}
