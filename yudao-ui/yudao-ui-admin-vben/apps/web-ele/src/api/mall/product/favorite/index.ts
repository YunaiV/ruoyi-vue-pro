import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallFavoriteApi {
  /** 商品收藏 */
  export interface Favorite {
    id?: number; // 收藏编号
    userId?: string; // 用户编号
    spuId?: number; // 商品 SPU 编号
  }
}

/** 获得商品收藏列表 */
export function getFavoritePage(params: PageParam) {
  return requestClient.get<PageResult<MallFavoriteApi.Favorite>>(
    '/product/favorite/page',
    { params },
  );
}
