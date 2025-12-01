import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallRewardActivityApi {
  /** 优惠规则 */
  export interface RewardRule {
    limit?: number; // 满足金额
    discountPrice?: number; // 优惠金额
    freeDelivery?: boolean; // 是否包邮
    point: number; // 赠送积分
    giveCouponTemplateCounts?: {
      [key: number]: number;
    }; // 赠送优惠券数量
  }

  /** 满减送活动 */
  export interface RewardActivity {
    id?: number; // 活动编号
    name?: string; // 活动名称
    status?: number; // 活动状态
    startTime?: Date; // 开始时间
    endTime?: Date; // 结束时间
    startAndEndTime?: Date[]; // 开始和结束时间（仅前端使用）
    remark?: string; // 备注
    conditionType?: number; // 条件类型
    productScope?: number; // 商品范围
    rules: RewardRule[]; // 优惠规则列表
    productScopeValues?: number[]; // 商品范围值（仅表单使用）：值为品类编号列表、商品编号列表
    productCategoryIds?: number[]; // 商品分类编号列表（仅表单使用）
    productSpuIds?: number[]; // 商品 SPU 编号列表（仅表单使用）
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
