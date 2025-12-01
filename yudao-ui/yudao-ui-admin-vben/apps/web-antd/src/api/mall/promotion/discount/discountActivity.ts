import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallDiscountActivityApi {
  /** 限时折扣相关属性 */
  export interface DiscountProduct {
    spuId: number; // 商品 SPU 编号
    skuId: number; // 商品 SKU 编号
    discountType: number; // 折扣类型
    discountPercent: number; // 折扣百分比
    discountPrice: number; // 折扣价格
  }

  /** 限时折扣活动 */
  export interface DiscountActivity {
    id?: number; // 活动编号
    spuId?: number; // 商品 SPU 编号
    name?: string; // 活动名称
    status?: number; // 状态
    remark?: string; // 备注
    startTime?: Date; // 开始时间
    endTime?: Date; // 结束时间
    products?: DiscountProduct[]; // 商品列表
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
