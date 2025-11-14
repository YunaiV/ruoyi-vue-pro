import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { defineComponent } from 'vue';

import { PromotionDiscountTypeEnum } from '@vben/constants';
import { floatToFixed2 } from '@vben/utils';

// 优惠值
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
    let value = `${coupon.discountPercent / 10}`;
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
