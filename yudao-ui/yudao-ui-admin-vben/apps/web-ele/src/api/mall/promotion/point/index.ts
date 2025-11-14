import type { PageParam, PageResult } from '@vben/request';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { requestClient } from '#/api/request';

export namespace MallPointActivityApi {
  /** 积分商城商品 */
  export interface PointProduct {
    /** 积分商城商品编号 */
    id?: number;
    /** 积分商城活动 id */
    activityId?: number;
    /** 商品 SPU 编号 */
    spuId?: number;
    /** 商品 SKU 编号 */
    skuId: number;
    /** 可兑换数量 */
    count: number;
    /** 兑换积分 */
    point: number;
    /** 兑换金额，单位：分 */
    price: number;
    /** 积分商城商品库存 */
    stock: number;
    /** 积分商城商品状态 */
    activityStatus?: number;
  }

  /** 积分商城活动 */
  export interface PointActivity {
    /** 积分商城活动编号 */
    id: number;
    /** 积分商城活动商品 */
    spuId: number;
    /** 活动状态 */
    status: number;
    /** 积分商城活动库存 */
    stock: number;
    /** 积分商城活动总库存 */
    totalStock: number;
    /** 备注 */
    remark?: string;
    /** 排序 */
    sort: number;
    /** 创建时间 */
    createTime: string;
    /** 积分商城商品 */
    products: PointProduct[];
    /** 商品名称 */
    spuName: string;
    /** 商品主图 */
    picUrl: string;
    /** 商品市场价，单位：分 */
    marketPrice: number;
    /** 兑换积分 */
    point: number;
    /** 兑换金额，单位：分 */
    price: number;
  }

  /** 扩展 SKU 配置 */
  export type SkuExtension = {
    /** 积分商城商品配置 */
    productConfig: PointProduct;
  } & MallSpuApi.Sku;

  /** 扩展 SPU 配置 */
  export interface SpuExtension extends MallSpuApi.Spu {
    /** SKU 列表 */
    skus: SkuExtension[];
  }

  /** 扩展 SPU 配置（带积分信息） */
  export interface SpuExtensionWithPoint extends MallSpuApi.Spu {
    /** 积分商城活动库存 */
    pointStock: number;
    /** 积分商城活动总库存 */
    pointTotalStock: number;
    /** 兑换积分 */
    point: number;
    /** 兑换金额，单位：分 */
    pointPrice: number;
  }
}

/** 查询积分商城活动分页 */
export function getPointActivityPage(params: PageParam) {
  return requestClient.get<PageResult<MallPointActivityApi.PointActivity>>(
    '/promotion/point-activity/page',
    { params },
  );
}

/** 查询积分商城活动详情 */
export function getPointActivity(id: number) {
  return requestClient.get<MallPointActivityApi.PointActivity>(
    `/promotion/point-activity/get?id=${id}`,
  );
}

/** 查询积分商城活动列表，基于活动编号数组 */
export function getPointActivityListByIds(ids: number[]) {
  return requestClient.get<MallPointActivityApi.PointActivity[]>(
    `/promotion/point-activity/list-by-ids?ids=${ids}`,
  );
}

/** 新增积分商城活动 */
export function createPointActivity(data: MallPointActivityApi.PointActivity) {
  return requestClient.post('/promotion/point-activity/create', data);
}

/** 修改积分商城活动 */
export function updatePointActivity(data: MallPointActivityApi.PointActivity) {
  return requestClient.put('/promotion/point-activity/update', data);
}

/** 删除积分商城活动 */
export function deletePointActivity(id: number) {
  return requestClient.delete(`/promotion/point-activity/delete?id=${id}`);
}

/** 关闭积分商城活动 */
export function closePointActivity(id: number) {
  return requestClient.put(`/promotion/point-activity/close?id=${id}`);
}
