import type { PageParam, PageResult } from '@vben/request';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { requestClient } from '#/api/request';

export namespace MallSeckillActivityApi {
  /** 秒杀商品 */
  export interface SeckillProduct {
    skuId: number; // 商品 SKU 编号
    spuId: number; // 商品 SPU 编号
    seckillPrice: number; // 秒杀价格
    stock: number; // 秒杀库存
  }

  /** 秒杀活动 */
  export interface SeckillActivity {
    id?: number; // 活动编号
    spuId?: number; // 商品 SPU 编号
    name?: string; // 活动名称
    status?: number; // 活动状态
    remark?: string; // 备注
    startTime?: Date; // 开始时间
    endTime?: Date; // 结束时间
    sort?: number; // 排序
    configIds?: string; // 配置编号
    orderCount?: number; // 订单数量
    userCount?: number; // 用户数量
    totalPrice?: number; // 总金额
    totalLimitCount?: number; // 总限购数量
    singleLimitCount?: number; // 单次限购数量
    stock?: number; // 秒杀库存
    totalStock?: number; // 秒杀总库存
    seckillPrice?: number; // 秒杀价格
    products?: SeckillProduct[]; // 秒杀商品列表
  }

  /** 扩展 SKU 配置 */
  export type SkuExtension = {
    productConfig: SeckillProduct; // 秒杀商品配置
  } & MallSpuApi.Sku;

  /** 扩展 SPU 配置 */
  export interface SpuExtension extends MallSpuApi.Spu {
    skus: SkuExtension[]; // SKU 列表
  }
}

/** 查询秒杀活动列表 */
export function getSeckillActivityPage(params: PageParam) {
  return requestClient.get<PageResult<MallSeckillActivityApi.SeckillActivity>>(
    '/promotion/seckill-activity/page',
    { params },
  );
}

/** 查询秒杀活动列表，基于活动编号数组 */
export function getSeckillActivityListByIds(ids: number[]) {
  return requestClient.get<MallSeckillActivityApi.SeckillActivity[]>(
    `/promotion/seckill-activity/list-by-ids?ids=${ids}`,
  );
}

/** 查询秒杀活动详情 */
export function getSeckillActivity(id: number) {
  return requestClient.get<MallSeckillActivityApi.SeckillActivity>(
    `/promotion/seckill-activity/get?id=${id}`,
  );
}

/** 新增秒杀活动 */
export function createSeckillActivity(
  data: MallSeckillActivityApi.SeckillActivity,
) {
  return requestClient.post('/promotion/seckill-activity/create', data);
}

/** 修改秒杀活动 */
export function updateSeckillActivity(
  data: MallSeckillActivityApi.SeckillActivity,
) {
  return requestClient.put('/promotion/seckill-activity/update', data);
}

/** 关闭秒杀活动 */
export function closeSeckillActivity(id: number) {
  return requestClient.put(`/promotion/seckill-activity/close?id=${id}`);
}

/** 删除秒杀活动 */
export function deleteSeckillActivity(id: number) {
  return requestClient.delete(`/promotion/seckill-activity/delete?id=${id}`);
}
