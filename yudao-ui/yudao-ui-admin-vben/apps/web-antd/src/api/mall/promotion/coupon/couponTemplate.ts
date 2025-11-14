import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallCouponTemplateApi {
  /** 优惠券模板 */
  export interface CouponTemplate {
    id: number; // 模板编号
    name: string; // 模板名称
    status: number; // 状态
    totalCount: number; // 发放数量
    takeLimitCount: number; // 每人限领个数
    takeType: number; // 领取方式
    usePrice: number; // 使用门槛
    productScope: number; // 商品范围
    productScopeValues: number[]; // 商品范围值
    validityType: number; // 有效期类型
    validStartTime: Date; // 固定日期-生效开始时间
    validEndTime: Date; // 固定日期-生效结束时间
    fixedStartTerm: number; // 领取日期-开始天数
    fixedEndTerm: number; // 领取日期-结束天数
    discountType: number; // 优惠类型
    discountPercent?: number; // 折扣百分比
    discountPrice: number; // 优惠金额
    discountLimitPrice?: number; // 折扣上限
    takeCount: number; // 已领取数量
    useCount: number; // 已使用数量
  }
}

/** 创建优惠劵模板 */
export function createCouponTemplate(
  data: MallCouponTemplateApi.CouponTemplate,
) {
  return requestClient.post('/promotion/coupon-template/create', data);
}

/** 更新优惠劵模板 */
export function updateCouponTemplate(
  data: MallCouponTemplateApi.CouponTemplate,
) {
  return requestClient.put('/promotion/coupon-template/update', data);
}

/** 更新优惠劵模板的状态 */
export function updateCouponTemplateStatus(id: number, status: number) {
  return requestClient.put('/promotion/coupon-template/update-status', {
    id,
    status,
  });
}

/** 删除优惠劵模板 */
export function deleteCouponTemplate(id: number) {
  return requestClient.delete(`/promotion/coupon-template/delete?id=${id}`);
}

/** 获得优惠劵模板 */
export function getCouponTemplate(id: number) {
  return requestClient.get<MallCouponTemplateApi.CouponTemplate>(
    `/promotion/coupon-template/get?id=${id}`,
  );
}

/** 获得优惠劵模板分页 */
export function getCouponTemplatePage(params: PageParam) {
  return requestClient.get<PageResult<MallCouponTemplateApi.CouponTemplate>>(
    '/promotion/coupon-template/page',
    { params },
  );
}

/** 获得优惠劵模板列表 */
export function getCouponTemplateList(ids: number[]) {
  return requestClient.get<MallCouponTemplateApi.CouponTemplate[]>(
    `/promotion/coupon-template/list?ids=${ids}`,
  );
}
