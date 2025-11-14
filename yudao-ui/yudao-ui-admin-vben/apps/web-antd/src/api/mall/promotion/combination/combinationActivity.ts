import type { PageParam, PageResult } from '@vben/request';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { requestClient } from '#/api/request';

export namespace MallCombinationActivityApi {
  /** 拼团活动所需属性 */
  export interface CombinationProduct {
    spuId: number; // 商品 SPU 编号
    skuId: number; // 商品 SKU 编号
    combinationPrice: number; // 拼团价格
  }
  /** 拼团活动 */
  export interface CombinationActivity {
    id?: number; // 活动编号
    name?: string; // 活动名称
    spuId?: number; // 商品 SPU 编号
    totalLimitCount?: number; // 总限购数量
    singleLimitCount?: number; // 单次限购数量
    startTime?: Date; // 开始时间
    endTime?: Date; // 结束时间
    userSize?: number; // 用户数量
    totalCount?: number; // 总数量
    successCount?: number; // 成功数量
    orderUserCount?: number; // 订单用户数量
    virtualGroup?: number; // 虚拟成团
    status?: number; // 状态
    limitDuration?: number; // 限制时长
    combinationPrice?: number; // 拼团价格
    products: CombinationProduct[]; // 商品列表
  }

  /** 扩展 SKU 配置 */
  export type SkuExtension = {
    productConfig: CombinationProduct; // 拼团活动配置
  } & MallSpuApi.Sku;

  /** 扩展 SPU 配置 */
  export interface SpuExtension extends MallSpuApi.Spu {
    skus: SkuExtension[]; // SKU 列表
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
