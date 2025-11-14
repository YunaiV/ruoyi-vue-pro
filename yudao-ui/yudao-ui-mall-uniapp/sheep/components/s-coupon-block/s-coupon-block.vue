<!-- 装修营销组件：优惠券  -->
<template>
  <scroll-view
    class="scroll-box"
    scroll-x
    scroll-anchoring
    :style="[bgStyle, { marginLeft: `${data.space}px` }]"
  >
    <view
      class="coupon-box ss-flex"
      :style="couponList.length === 2 ? couponBoxStyleTwo : couponBoxStyleNormal"
    >
      <view
        class="coupon-item"
        :style="[couponBg, { marginLeft: `${data.space}px` }]"
        v-for="(item, index) in couponList"
        :key="index"
      >
        <su-coupon
          :size="SIZE_LIST[columns - 1]"
          :textColor="data.textColor"
          background=""
          :couponId="item.id"
          :title="formatCouponTitle(item)"
          :type="formatCouponDiscountType(item)"
          :value="formatCouponDiscountValue(item)"
          :sellBy="formatValidityType(item)"
          :surplus="item.totalCount === -1 ? -1 : item.totalCount - item.takeCount"
        >
          <template v-slot:btn>
            <!-- 两列时，领取按钮坚排 -->
            <button
              v-if="columns === 2"
              @click.stop="onGetCoupon(item.id)"
              class="ss-reset-button card-btn vertical"
              :style="[btnStyles]"
            >
              <view class="btn-text">立即领取</view>
            </button>
            <button
              v-else
              class="ss-reset-button card-btn"
              :style="[btnStyles]"
              @click.stop="onGetCoupon(item.id)"
            >
              立即领取
            </button>
          </template>
        </su-coupon>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
  import sheep from '@/sheep';
  import CouponApi from '@/sheep/api/promotion/coupon';
  import { ref, onMounted, computed } from 'vue';
  import { CouponTemplateValidityTypeEnum, PromotionDiscountTypeEnum } from '@/sheep/helper/const';
  import { floatToFixed2, formatDate } from '@/sheep/helper/utils';
  import { formatDiscountPercent } from '@/sheep/hooks/useGoods';

  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    styles: {
      type: Object,
      default: () => ({}),
    },
  });
  const { columns, button } = props.data;
  const SIZE_LIST = ['lg', 'md', 'xs'];
  const couponBg = {
    background: `url(${sheep.$url.cdn(props.data.bgImg)}) no-repeat top center / 100% 100%`,
  };
  const btnStyles = {
    background: button.bgColor,
    color: button.color,
  };
  // 两列优惠券时的排版方式
  const couponBoxStyleNormal = {
    display: 'flex',
    'justify-content': 'space-between',
  };
  // 非两列优惠券时的排版方式
  const couponBoxStyleTwo = {
    display: 'flex',
    'justify-content': 'space-around',
  };
  // 设置背景样式
  const bgStyle = computed(() => {
    // 直接从 props.styles 解构
    const { bgType, bgImg, bgColor } = props.styles;

    // 根据 bgType 返回相应的样式
    return {
      background: bgType === 'img' ? `url(${bgImg}) no-repeat top center / 100% 100%` : bgColor,
    };
  });
  // 格式化【折扣类型】
  const formatCouponDiscountType = (coupon) => {
    if (coupon.discountType === PromotionDiscountTypeEnum.PRICE.type) {
      return 'reduce';
    }
    if (coupon.discountType === PromotionDiscountTypeEnum.PERCENT.type) {
      return 'percent';
    }
    return `未知【${coupon.discountType}】`;
  };

  // 格式化【折扣】
  const formatCouponDiscountValue = (coupon) => {
    if (coupon.discountType === PromotionDiscountTypeEnum.PRICE.type) {
      return floatToFixed2(coupon.discountPrice);
    }
    if (coupon.discountType === PromotionDiscountTypeEnum.PERCENT.type) {
      return formatDiscountPercent(coupon.discountPercent);
    }
    return `未知【${coupon.discountType}】`;
  };

  // 格式化【有效期限】
  const formatValidityType = (row) => {
    if (row.validityType === CouponTemplateValidityTypeEnum.DATE.type) {
      return `${formatDate(row.validStartTime)} 至 ${formatDate(row.validEndTime)}`;
    }
    if (row.validityType === CouponTemplateValidityTypeEnum.TERM.type) {
      return `领取后第 ${row.fixedStartTerm} - ${row.fixedEndTerm} 天内可用`;
    }
    return '未知【' + row.validityType + '】';
  };

  // 格式化【优惠券标题】
  const formatCouponTitle = (coupon) => {
    return `满${floatToFixed2(coupon.usePrice)}元${
      coupon.discountType === PromotionDiscountTypeEnum.PRICE.type
        ? ',减' + floatToFixed2(coupon.discountPrice) + '元'
        : ',打' + formatDiscountPercent(coupon.discountPercent) + '折'
    }`;
  };

  const couponList = ref([]);
  // 立即领取优惠券
  async function onGetCoupon(id) {
    const { error, msg } = await CouponApi.takeCoupon(id);
    if (error === 0) {
      uni.showToast({
        title: msg,
        icon: 'none',
      });
      return;
    }
    await getCouponTemplateList();
  }
  const getCouponTemplateList = async () => {
    const { data } = await CouponApi.getCouponTemplateListByIds(props.data.couponIds.join(','));
    couponList.value = data;
  };
  onMounted(() => {
    getCouponTemplateList();
  });
</script>

<style lang="scss" scoped>
  .card-btn {
    width: 140rpx;
    height: 50rpx;
    border-radius: 25rpx;
    font-size: 24rpx;
    line-height: 50rpx;

    &.vertical {
      width: 50rpx;
      height: 140rpx;
      margin: auto 20rpx auto 0;

      .btn-text {
        font-size: 24rpx;
        text-align: center;
        writing-mode: vertical-lr;
      }
    }
  }

  .coupon-item {
    &:nth-of-type(1) {
      margin-left: 0 !important;
    }
  }
</style>
