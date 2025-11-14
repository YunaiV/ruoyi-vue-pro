import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallHistoryApi {
  /** 商品浏览记录 */
  export interface BrowseHistory {
    /** 记录编号 */
    id?: number;
    /** 用户编号 */
    userId?: number;
    /** 商品 SPU 编号 */
    spuId?: number;
    /** 浏览时间 */
    createTime?: Date;
  }
}

/**
 * 获得商品浏览记录分页
 *
 * @param params 请求参数
 */
export function getBrowseHistoryPage(params: PageParam) {
  return requestClient.get<PageResult<MallHistoryApi.BrowseHistory>>(
    '/product/browse-history/page',
    { params },
  );
}
