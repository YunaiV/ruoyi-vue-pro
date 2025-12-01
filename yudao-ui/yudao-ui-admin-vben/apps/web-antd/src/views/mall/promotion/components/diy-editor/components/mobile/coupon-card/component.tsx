/*  eslint-disable vue/one-component-per-file */
import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { defineComponent } from 'vue';

import {
  CouponTemplateValidityTypeEnum,
  PromotionDiscountTypeEnum,
} from '@vben/constants';
import { floatToFixed2, formatDate } from '@vben/utils';

/** 有效期 */
export const CouponValidTerm = defineComponent({
  name: 'CouponValidTerm',
  props: {
    coupon: {
      type: Object as () => MallCouponTemplateApi.CouponTemplate,
      required: true,
    },
  },
  setup(props) {
    const coupon = props.coupon as MallCouponTemplateApi.CouponTemplate;
    const text =
      coupon.validityType === CouponTemplateValidityTypeEnum.DATE.type
        ? `有效期：${formatDate(coupon.validStartTime, 'YYYY-MM-DD')} 至 ${formatDate(
            coupon.validEndTime,
            'YYYY-MM-DD',
          )}`
        : `领取后第 ${coupon.fixedStartTerm} - ${coupon.fixedEndTerm} 天内可用`;
    return () => <div>{text}</div>;
  },
});

/** 优惠值 */
export const CouponDiscount = defineComponent({
  name: 'CouponDiscount',
  props: {
    coupon: {
      type: Object as () => MallCouponTemplateApi.CouponTemplate,
      required: true,
    },
  },
  setup(props) {
    const coupon = props.coupon as MallCouponTemplateApi.CouponTemplate;
    // 折扣
    let value = `${(coupon.discountPercent ?? 0) / 10}`;
    let suffix = ' 折';
    // 满减
    if (coupon.discountType === PromotionDiscountTypeEnum.PRICE.type) {
      value = floatToFixed2(coupon.discountPrice);
      suffix = ' 元';
    }
    return () => (
      <div>
        <span class={'text-20px font-bold'}>{value}</span>
        <span>{suffix}</span>
      </div>
    );
  },
});

/** 优惠描述 */
export const CouponDiscountDesc = defineComponent({
  name: 'CouponDiscountDesc',
  props: {
    coupon: {
      type: Object as () => MallCouponTemplateApi.CouponTemplate,
      required: true,
    },
  },
  setup(props) {
    const coupon = props.coupon as MallCouponTemplateApi.CouponTemplate;
    // 使用条件
    const useCondition =
      coupon.usePrice > 0 ? `满${floatToFixed2(coupon.usePrice)}元，` : '';
    // 优惠描述
    const discountDesc =
      coupon.discountType === PromotionDiscountTypeEnum.PRICE.type
        ? `减${floatToFixed2(coupon.discountPrice)}元`
        : `打${(coupon.discountPercent ?? 0) / 10}折`;
    return () => (
      <div>
        <span>{useCondition}</span>
        <span>{discountDesc}</span>
      </div>
    );
  },
});
