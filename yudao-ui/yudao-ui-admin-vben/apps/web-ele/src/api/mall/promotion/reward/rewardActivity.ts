import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallRewardActivityApi {
  /** 优惠规则 */
  export interface RewardRule {
    /** 满足金额 */
    limit?: number;
    /** 优惠金额 */
    discountPrice?: number;
    /** 是否包邮 */
    freeDelivery?: boolean;
    /** 赠送积分 */
    point: number;
    /** 赠送优惠券数量 */
    giveCouponTemplateCounts?: {
      [key: number]: number;
    };
  }

  /** 满减送活动 */
  export interface RewardActivity {
    /** 活动编号 */
    id?: number;
    /** 活动名称 */
    name?: string;
    /** 开始时间 */
    startTime?: Date;
    /** 结束时间 */
    endTime?: Date;
    /** 开始和结束时间（仅前端使用） */
    startAndEndTime?: Date[];
    /** 备注 */
    remark?: string;
    /** 条件类型 */
    conditionType?: number;
    /** 商品范围 */
    productScope?: number;
    /** 优惠规则列表 */
    rules: RewardRule[];
    /** 商品范围值（仅表单使用）：值为品类编号列表、商品编号列表 */
    productScopeValues?: number[];
    /** 商品分类编号列表（仅表单使用） */
    productCategoryIds?: number[];
    /** 商品 SPU 编号列表（仅表单使用） */
    productSpuIds?: number[];
    /** 状态 */
    status?: number;
  }
}

/** 新增满减送活动 */
export function createRewardActivity(
  data: MallRewardActivityApi.RewardActivity,
) {
  return requestClient.post('/promotion/reward-activity/create', data);
}

/** 更新满减送活动 */
export function updateRewardActivity(
  data: MallRewardActivityApi.RewardActivity,
) {
  return requestClient.put('/promotion/reward-activity/update', data);
}

/** 查询满减送活动列表 */
export function getRewardActivityPage(params: PageParam) {
  return requestClient.get<PageResult<MallRewardActivityApi.RewardActivity>>(
    '/promotion/reward-activity/page',
    { params },
  );
}

/** 查询满减送活动详情 */
export function getReward(id: number) {
  return requestClient.get<MallRewardActivityApi.RewardActivity>(
    `/promotion/reward-activity/get?id=${id}`,
  );
}

/** 删除满减送活动 */
export function deleteRewardActivity(id: number) {
  return requestClient.delete(`/promotion/reward-activity/delete?id=${id}`);
}

/** 关闭满减送活动 */
export function closeRewardActivity(id: number) {
  return requestClient.put(`/promotion/reward-activity/close?id=${id}`);
}
