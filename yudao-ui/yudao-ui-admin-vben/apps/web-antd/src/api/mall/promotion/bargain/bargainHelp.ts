import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallBargainHelpApi {
  /** 砍价记录 */
  export interface BargainHelp {
    id: number; // 记录编号
    record: number; // 砍价记录编号
    userId: number; // 用户编号
    reducePrice: number; // 砍掉金额
    endTime: Date; // 结束时间
  }
}

/** 查询砍价记录列表 */
export function getBargainHelpPage(params: PageParam) {
  return requestClient.get<PageResult<MallBargainHelpApi.BargainHelp>>(
    '/promotion/bargain-help/page',
    { params },
  );
}
