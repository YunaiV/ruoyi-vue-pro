import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallHistoryApi {
  /** 商品浏览记录 */
  export interface BrowseHistory {
    id?: number; // 记录编号
    userId?: number; // 用户编号
    spuId?: number; // 商品 SPU 编号
    createTime?: Date; // 浏览时间
  }
}

/** 获得商品浏览记录分页 */
export function getBrowseHistoryPage(params: PageParam) {
  return requestClient.get<PageResult<MallHistoryApi.BrowseHistory>>(
    '/product/browse-history/page',
    { params },
  );
}
