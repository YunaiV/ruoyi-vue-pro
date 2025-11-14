import type { PageParam, PageResult } from '@vben/request';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { requestClient } from '#/api/request';

export namespace MallCombinationActivityApi {
  /** 拼团活动所需属性 */
  export interface CombinationProduct {
    /** 商品 SPU 编号 */
    spuId: number;
    /** 商品 SKU 编号 */
    skuId: number;
    /** 拼团价格 */
    combinationPrice: number;
  }
  /** 拼团活动 */
  export interface CombinationActivity {
    /** 活动编号 */
    id?: number;
    /** 活动名称 */
    name?: string;
    /** 商品 SPU 编号 */
    spuId?: number;
    /** 总限购数量 */
    totalLimitCount?: number;
    /** 单次限购数量 */
    singleLimitCount?: number;
    /** 开始时间 */
    startTime?: Date;
    /** 结束时间 */
    endTime?: Date;
    /** 用户数量 */
    userSize?: number;
    /** 总数量 */
    totalCount?: number;
    /** 成功数量 */
    successCount?: number;
    /** 订单用户数量 */
    orderUserCount?: number;
    /** 虚拟成团 */
    virtualGroup?: number;
    /** 状态 */
    status?: number;
    /** 限制时长 */
    limitDuration?: number;
    /** 拼团价格 */
    combinationPrice?: number;
    /** 商品列表 */
    products: CombinationProduct[];
    /** 图片 */
    picUrl?: string;
  }

  /** 扩展 SKU 配置 */
  export type SkuExtension = {
    /** 拼团活动配置 */
    productConfig: CombinationProduct;
  } & MallSpuApi.Sku;

  /** 扩展 SPU 配置 */
  export interface SpuExtension extends MallSpuApi.Spu {
    /** SKU 列表 */
    skus: SkuExtension[];
  }
}

/** 查询拼团活动列表 */
export function getCombinationActivityPage(params: PageParam) {
  return requestClient.get<
    PageResult<MallCombinationActivityApi.CombinationActivity>
  >('/promotion/combination-activity/page', { params });
}

/** 查询拼团活动详情 */
export function getCombinationActivity(id: number) {
  return requestClient.get<MallCombinationActivityApi.CombinationActivity>(
    `/promotion/combination-activity/get?id=${id}`,
  );
}

/** 获得拼团活动列表，基于活动编号数组 */
export function getCombinationActivityListByIds(ids: number[]) {
  return requestClient.get<MallCombinationActivityApi.CombinationActivity[]>(
    `/promotion/combination-activity/list-by-ids?ids=${ids}`,
  );
}

/** 新增拼团活动 */
export function createCombinationActivity(
  data: MallCombinationActivityApi.CombinationActivity,
) {
  return requestClient.post('/promotion/combination-activity/create', data);
}

/** 修改拼团活动 */
export function updateCombinationActivity(
  data: MallCombinationActivityApi.CombinationActivity,
) {
  return requestClient.put('/promotion/combination-activity/update', data);
}

/** 关闭拼团活动 */
export function closeCombinationActivity(id: number) {
  return requestClient.put(`/promotion/combination-activity/close?id=${id}`);
}

/** 删除拼团活动 */
export function deleteCombinationActivity(id: number) {
  return requestClient.delete(
    `/promotion/combination-activity/delete?id=${id}`,
  );
}
