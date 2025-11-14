import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallBargainHelpApi {
  /** 砍价记录 */
  export interface BargainHelp {
    /** 记录编号 */
    id: number;
    /** 砍价记录编号 */
    record: number;
    /** 用户编号 */
    userId: number;
    /** 砍掉金额 */
    reducePrice: number;
    /** 结束时间 */
    endTime: Date;
  }
}

/** 查询砍价记录列表 */
export function getBargainHelpPage(params: PageParam) {
  return requestClient.get<PageResult<MallBargainHelpApi.BargainHelp>>(
    '/promotion/bargain-help/page',
    { params },
  );
}
