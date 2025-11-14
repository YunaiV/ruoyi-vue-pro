import type { PageParam, PageResult } from '@vben/request';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { requestClient } from '#/api/request';

export namespace MallBargainActivityApi {
  /** 砍价活动 */
  export interface BargainActivity {
    /** 活动编号 */
    id?: number;
    /** 活动名称 */
    name?: string;
    /** 开始时间 */
    startTime?: Date;
    /** 结束时间 */
    endTime?: Date;
    /** 状态 */
    status?: number;
    /** 达到该人数，才能砍到低价 */
    helpMaxCount?: number;
    /** 最大帮砍次数 */
    bargainCount?: number;
    /** 最大购买次数 */
    totalLimitCount?: number;
    /** 商品 SPU 编号 */
    spuId: number;
    /** 商品 SKU 编号 */
    skuId: number;
    /** 砍价起始价格，单位分 */
    bargainFirstPrice: number;
    /** 砍价底价 */
    bargainMinPrice: number;
    /** 活动库存 */
    stock: number;
    /** 用户每次砍价的最小金额，单位：分 */
    randomMinPrice?: number;
    /** 用户每次砍价的最大金额，单位：分 */
    randomMaxPrice?: number;
  }

  /** 砍价活动所需属性。选择的商品和属性的时候使用方便使用活动的通用封装 */
  export interface BargainProduct {
    /** 商品 SPU 编号 */
    spuId: number;
    /** 商品 SKU 编号 */
    skuId: number;
    /** 砍价起始价格，单位分 */
    bargainFirstPrice: number;
    /** 砍价底价 */
    bargainMinPrice: number;
    /** 活动库存 */
    stock: number;
  }

  /** 扩展 SKU 配置 */
  export type SkuExtension = {
    /** 砍价活动配置 */
    productConfig: BargainProduct;
  } & MallSpuApi.Sku;

  /** 扩展 SPU 配置 */
  export interface SpuExtension extends MallSpuApi.Spu {
    /** SKU 列表 */
    skus: SkuExtension[];
  }
}

/** 查询砍价活动列表 */
export function getBargainActivityPage(params: PageParam) {
  return requestClient.get<PageResult<MallBargainActivityApi.BargainActivity>>(
    '/promotion/bargain-activity/page',
    { params },
  );
}

/** 查询砍价活动详情 */
export function getBargainActivity(id: number) {
  return requestClient.get<MallBargainActivityApi.BargainActivity>(
    `/promotion/bargain-activity/get?id=${id}`,
  );
}

/** 新增砍价活动 */
export function createBargainActivity(
  data: MallBargainActivityApi.BargainActivity,
) {
  return requestClient.post('/promotion/bargain-activity/create', data);
}

/** 修改砍价活动 */
export function updateBargainActivity(
  data: MallBargainActivityApi.BargainActivity,
) {
  return requestClient.put('/promotion/bargain-activity/update', data);
}

/** 关闭砍价活动 */
export function closeBargainActivity(id: number) {
  return requestClient.put(`/promotion/bargain-activity/close?id=${id}`);
}

/** 删除砍价活动 */
export function deleteBargainActivity(id: number) {
  return requestClient.delete(`/promotion/bargain-activity/delete?id=${id}`);
}
