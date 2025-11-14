import type { PageParam, PageResult } from '@vben/request';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { requestClient } from '#/api/request';

export namespace MallSeckillActivityApi {
  /** 秒杀商品 */
  export interface SeckillProduct {
    /** 商品 SKU 编号 */
    skuId: number;
    /** 商品 SPU 编号 */
    spuId: number;
    /** 秒杀价格 */
    seckillPrice: number;
    /** 秒杀库存 */
    stock: number;
  }

  /** 秒杀活动 */
  export interface SeckillActivity {
    /** 活动编号 */
    id?: number;
    /** 商品 SPU 编号 */
    spuId?: number;
    /** 活动名称 */
    name?: string;
    /** 活动状态 */
    status?: number;
    /** 备注 */
    remark?: string;
    /** 开始时间 */
    startTime: Date;
    /** 结束时间 */
    endTime: Date;
    /** 排序 */
    sort?: number;
    /** 配置编号 */
    configIds?: number[];
    /** 订单数量 */
    orderCount?: number;
    /** 用户数量 */
    userCount?: number;
    /** 总金额 */
    totalPrice?: number;
    /** 总限购数量 */
    totalLimitCount?: number;
    /** 单次限购数量 */
    singleLimitCount?: number;
    /** 秒杀库存 */
    stock?: number;
    /** 秒杀总库存 */
    totalStock?: number;
    /** 秒杀价格 */
    seckillPrice?: number;
    /** 秒杀商品列表 */
    products?: SeckillProduct[];
    /** 图片 */
    picUrl?: string;
  }

  /** 扩展 SKU 配置 */
  export type SkuExtension = {
    /** 秒杀商品配置 */
    productConfig: SeckillProduct;
  } & MallSpuApi.Sku;

  /** 扩展 SPU 配置 */
  export interface SpuExtension extends MallSpuApi.Spu {
    /** SKU 列表 */
    skus: SkuExtension[];
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
