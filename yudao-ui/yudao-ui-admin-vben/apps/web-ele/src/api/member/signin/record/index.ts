import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MemberSignInRecordApi {
  /** 用户签到积分信息 */
  export interface SignInRecord {
    id?: number;
    userId: number;
    day: number;
    point: number;
  }
}

/** 查询用户签到积分列表 */
export function getSignInRecordPage(params: PageParam) {
  return requestClient.get<PageResult<MemberSignInRecordApi.SignInRecord>>(
    '/member/sign-in/record/page',
    {
      params,
    },
  );
}
