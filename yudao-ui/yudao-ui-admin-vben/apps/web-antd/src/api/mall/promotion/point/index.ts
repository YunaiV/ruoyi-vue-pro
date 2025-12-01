import type { PageParam, PageResult } from '@vben/request';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { requestClient } from '#/api/request';

export namespace MallPointActivityApi {
  /** 积分商城商品 */
  export interface PointProduct {
    id?: number; // 积分商城商品编号
    activityId?: number; // 积分商城活动 id
    spuId?: number; // 商品 SPU 编号
    skuId: number; // 商品 SKU 编号
    count: number; // 可兑换数量
    point: number; // 兑换积分
    price: number; // 兑换金额，单位：分
    stock: number; // 积分商城商品库存
    activityStatus?: number; // 积分商城商品状态
  }

  /** 积分商城活动 */
  export interface PointActivity {
    id: number; // 积分商城活动编号
    spuId: number; // 积分商城活动商品
    status: number; // 活动状态
    stock: number; // 积分商城活动库存
    totalStock: number; // 积分商城活动总库存
    remark?: string; // 备注
    sort: number; // 排序
    createTime: string; // 创建时间
    products: PointProduct[]; // 积分商城商品
    spuName: string; // 商品名称
    picUrl: string; // 商品主图
    marketPrice: number; // 商品市场价，单位：分
    point: number; // 兑换积分
    price: number; // 兑换金额，单位：分
  }

  /** 扩展 SPU 配置（带积分信息） */
  export interface SpuExtensionWithPoint extends MallSpuApi.Spu {
    pointStock: number; // 积分商城活动库存
    pointTotalStock: number; // 积分商城活动总库存
    point: number; // 兑换积分
    pointPrice: number; // 兑换金额，单位：分
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
