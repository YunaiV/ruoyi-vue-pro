import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallBargainRecordApi {
  /** 砍价记录 */
  export interface BargainRecord {
    /** 记录编号 */
    id: number;
    /** 活动编号 */
    activityId: number;
    /** 用户编号 */
    userId: number;
    /** 商品 SPU 编号 */
    spuId: number;
    /** 商品 SKU 编号 */
    skuId: number;
    /** 砍价起始价格 */
    bargainFirstPrice: number;
    /** 砍价价格 */
    bargainPrice: number;
    /** 状态 */
    status: number;
    /** 订单编号 */
    orderId: number;
    /** 结束时间 */
    endTime: Date;
  }
}

/** 查询砍价记录列表 */
export function getBargainRecordPage(params: PageParam) {
  return requestClient.get<PageResult<MallBargainRecordApi.BargainRecord>>(
    '/promotion/bargain-record/page',
    { params },
  );
}
