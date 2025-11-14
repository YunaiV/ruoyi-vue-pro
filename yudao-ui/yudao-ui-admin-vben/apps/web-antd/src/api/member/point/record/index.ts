import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MemberPointRecordApi {
  /** 用户积分记录信息 */
  export interface Record {
    id?: number;
    bizId: string;
    bizType: string;
    title: string;
    description: string;
    point: number;
    totalPoint: number;
    userId: number;
    createDate: Date;
  }
}

/** 查询用户积分记录列表 */
export function getRecordPage(params: PageParam) {
  return requestClient.get<PageResult<MemberPointRecordApi.Record>>(
    '/member/point/record/page',
    {
      params,
    },
  );
}
