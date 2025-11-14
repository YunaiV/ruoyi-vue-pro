import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { defineComponent } from 'vue';

import { CouponTemplateValidityTypeEnum } from '@vben/constants';
import { formatDate } from '@vben/utils';

// 有效期
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
