import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MpUserApi {
  /** 用户信息 */
  export interface User {
    id?: number;
    accountId: number;
    openid: string;
    nickname: string;
    avatar: string;
    sex: number;
    country: string;
    province: string;
    city: string;
    language: string;
    subscribe: boolean;
    subscribeTime?: Date;
    remark?: string;
    tagIds?: number[];
    createTime?: Date;
  }

  /** 用户分页查询参数 */
  export interface UserPageQuery extends PageParam {
    accountId?: number;
    nickname?: string;
    tagId?: number;
  }
}

/** 更新公众号粉丝 */
export function updateUser(data: MpUserApi.User) {
  return requestClient.put('/mp/user/update', data);
}

/** 获取公众号粉丝 */
export function getUser(id: number) {
  return requestClient.get<MpUserApi.User>('/mp/user/get', {
    params: { id },
  });
}

/** 获取公众号粉丝分页 */
export function getUserPage(params: MpUserApi.UserPageQuery) {
  return requestClient.get<PageResult<MpUserApi.User>>('/mp/user/page', {
    params,
  });
}

/** 同步公众号粉丝 */
export function syncUser(accountId: number) {
  return requestClient.post('/mp/user/sync', null, {
    params: { accountId },
  });
}
