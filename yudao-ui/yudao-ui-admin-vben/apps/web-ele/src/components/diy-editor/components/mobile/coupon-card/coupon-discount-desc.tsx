import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { defineComponent } from 'vue';

import { PromotionDiscountTypeEnum } from '@vben/constants';
import { floatToFixed2 } from '@vben/utils';

// 优惠描述
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
        : `打${coupon.discountPercent / 10}折`;
    return () => (
      <div>
        <span>{useCondition}</span>
        <span>{discountDesc}</span>
      </div>
    );
  },
});
