import type { PageParam, PageResult } from '@vben/request';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { requestClient } from '#/api/request';

export namespace MallDiscountActivityApi {
  /** 限时折扣相关属性 */
  export interface DiscountProduct {
    /** 商品 SPU 编号 */
    spuId: number;
    /** 商品 SKU 编号 */
    skuId: number;
    /** 折扣类型 */
    discountType: number;
    /** 折扣百分比 */
    discountPercent: number;
    /** 折扣价格 */
    discountPrice: number;
  }

  /** 限时折扣活动 */
  export interface DiscountActivity {
    /** 活动编号 */
    id?: number;
    /** 商品 SPU 编号 */
    spuId?: number;
    /** 活动名称 */
    name?: string;
    /** 状态 */
    status?: number;
    /** 备注 */
    remark?: string;
    /** 开始时间 */
    startTime?: Date;
    /** 结束时间 */
    endTime?: Date;
    /** 商品列表 */
    products?: DiscountProduct[];
  }

  /** 扩展 SKU 配置 */
  export type SkuExtension = {
    /** 限时折扣配置 */
    productConfig: DiscountProduct;
  } & MallSpuApi.Sku;

  /** 扩展 SPU 配置 */
  export interface SpuExtension extends MallSpuApi.Spu {
    /** SKU 列表 */
    skus: SkuExtension[];
  }
}

/** 查询限时折扣活动列表 */
export function getDiscountActivityPage(params: PageParam) {
  return requestClient.get<
    PageResult<MallDiscountActivityApi.DiscountActivity>
  >('/promotion/discount-activity/page', { params });
}

/** 查询限时折扣活动详情 */
export function getDiscountActivity(id: number) {
  return requestClient.get<MallDiscountActivityApi.DiscountActivity>(
    `/promotion/discount-activity/get?id=${id}`,
  );
}

/** 新增限时折扣活动 */
export function createDiscountActivity(
  data: MallDiscountActivityApi.DiscountActivity,
) {
  return requestClient.post('/promotion/discount-activity/create', data);
}

/** 修改限时折扣活动 */
export function updateDiscountActivity(
  data: MallDiscountActivityApi.DiscountActivity,
) {
  return requestClient.put('/promotion/discount-activity/update', data);
}

/** 关闭限时折扣活动 */
export function closeDiscountActivity(id: number) {
  return requestClient.put(`/promotion/discount-activity/close?id=${id}`);
}

/** 删除限时折扣活动 */
export function deleteDiscountActivity(id: number) {
  return requestClient.delete(`/promotion/discount-activity/delete?id=${id}`);
}
