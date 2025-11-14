<template>
  <view>
    <view>
      <slot name="top"></slot>
    </view>
    <view
      class="ss-order-card-warp ss-flex ss-col-stretch ss-row-between bg-white"
      :style="[{ borderRadius: radius + 'rpx', marginBottom: marginBottom + 'rpx' }]"
    >
      <view class="img-box ss-m-r-24">
        <image class="order-img" :src="sheep.$url.cdn(img)" mode="aspectFill"></image>
      </view>
      <view
        class="box-right ss-flex-col ss-row-between"
        :style="[{ width: titleWidth ? titleWidth + 'rpx' : '' }]"
      >
        <view class="title-text ss-line-2" v-if="title">{{ title }}</view>
        <view v-if="skuString" class="spec-text ss-m-t-8 ss-m-b-12">{{ skuString }}</view>
        <view class="groupon-box">
          <slot name="groupon"></slot>
        </view>
        <view class="ss-flex">
          <view class="ss-flex ss-col-center">
            <view
              class="price-text ss-flex ss-col-center"
              :style="[{ color: priceColor }]"
              v-if="price && Number(price) > 0"
            >
              ￥{{ fen2yuan(price) }}
            </view>
            <view v-if="point && Number(price) > 0">+</view>
            <view class="price-text ss-flex ss-col-center" v-if="point">
              <image
                :src="sheep.$url.static('/static/img/shop/goods/score1.svg')"
                class="point-img"
              ></image>
              <view>{{ point }}</view>
            </view>
            <view v-if="num" class="total-text ss-flex ss-col-center">x {{ num }}</view>
            <slot name="priceSuffix"></slot>
          </view>
        </view>
        <view class="tool-box">
          <slot name="tool"></slot>
        </view>
        <view>
          <slot name="rightBottom"></slot>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { computed } from 'vue';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  /**
   * 订单卡片
   *
   * @property {String} img 											- 图片
   * @property {String} title 										- 标题
   * @property {Number} titleWidth = 0								- 标题宽度，默认0，单位rpx
   * @property {String} skuText 										- 规格
   * @property {String | Number} price 								- 价格
   * @property {String} priceColor 									- 价格颜色
   * @property {Number | String} num									- 数量
   *
   */
  const props = defineProps({
    img: {
      type: String,
      default: 'https://img1.baidu.com/it/u=1601695551,235775011&fm=26&fmt=auto',
    },
    title: {
      type: String,
      default: '',
    },
    titleWidth: {
      type: Number,
      default: 0,
    },
    skuText: {
      type: [String, Array],
      default: '',
    },
    price: {
      type: [String, Number],
      default: '',
    },
    priceColor: {
      type: [String],
      default: '',
    },
    num: {
      type: [String, Number],
      default: 0,
    },
    point: {
      type: [String, Number],
      default: '',
    },
    radius: {
      type: [String],
      default: '',
    },
    marginBottom: {
      type: [String],
      default: '',
    },
  });
  const skuString = computed(() => {
    if (!props.skuText) {
      return '';
    }
    if (typeof props.skuText === 'object') {
      return props.skuText.join(',');
    }
    return props.skuText;
  });
</script>

<style lang="scss" scoped>
  .point-img {
    width: 36rpx;
    height: 36rpx;
    margin: 0 4rpx;
  }
  .ss-order-card-warp {
    padding: 20rpx;

    .img-box {
      width: 164rpx;
      height: 164rpx;
      border-radius: 10rpx;
      overflow: hidden;

      .order-img {
        width: 164rpx;
        height: 164rpx;
      }
    }

    .box-right {
      flex: 1;
      // width: 500rpx;
      // height: 164rpx;
      position: relative;

      .tool-box {
        position: absolute;
        right: 0rpx;
        bottom: -10rpx;
      }
    }

    .title-text {
      font-size: 28rpx;
      font-weight: 500;
      line-height: 40rpx;
    }

    .spec-text {
      font-size: 24rpx;
      font-weight: 400;
      color: $dark-9;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
    }

    .price-text {
      font-size: 24rpx;
      font-weight: 500;
      font-family: OPPOSANS;
    }

    .total-text {
      font-size: 24rpx;
      font-weight: 400;
      line-height: 24rpx;
      color: $dark-9;
      margin-left: 8rpx;
    }
  }
</style>
