<template>
  <view class="bg-white order-list-card-box ss-r-10 ss-m-t-14 ss-m-20"
        :key="orderData.id">
    <view class="order-card-header ss-flex ss-col-center ss-row-between ss-p-x-20">
      <view class="order-no">订单号：{{ orderData.no }}</view>
      <view class="order-state ss-font-26" :class="formatOrderColor(orderData)">
        {{ formatOrderStatus(orderData) }}
      </view>
    </view>
    <view class="border-bottom" v-for="item in orderData.items" :key="item.id">
      <s-goods-item
        :img="item.picUrl"
        :title="item.spuName"
        :skuText="item.properties.map((property) => property.valueName).join(' ')"
        :price="item.price"
        :num="item.count"
      />
    </view>
    <view class="pay-box ss-m-t-30 ss-flex ss-row-right ss-p-r-20">
      <view class="ss-flex ss-col-center">
        <view class="discounts-title pay-color">共 {{ orderData.productCount }} 件商品,总金额:</view>
        <view class="discounts-money pay-color">
          ￥{{ fen2yuan(orderData.payPrice) }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { fen2yuan, formatOrderColor, formatOrderStatus } from '@/sheep/hooks/useGoods';

  const props = defineProps({
    orderData: {
      type: Object,
      default: {},
    },
  });
</script>

<style lang="scss" scoped>
  .order-list-card-box {
    .order-card-header {
      height: 80rpx;

      .order-no {
        font-size: 26rpx;
        font-weight: 500;
      }

      .order-state {}
    }
    .pay-box {
      .discounts-title {
        font-size: 24rpx;
        line-height: normal;
        color: #999999;
      }

      .discounts-money {
        font-size: 24rpx;
        line-height: normal;
        color: #999;
        font-family: OPPOSANS;
      }

      .pay-color {
        color: #333;
      }
    }
    .order-card-footer {
      height: 100rpx;

      .more-item-box {
        padding: 20rpx;

        .more-item {
          height: 60rpx;

          .title {
            font-size: 26rpx;
          }
        }
      }

      .more-btn {
        color: $dark-9;
        font-size: 24rpx;
      }

      .content {
        width: 154rpx;
        color: #333333;
        font-size: 26rpx;
        font-weight: 500;
      }
    }
  }
  .warning-color {
    color: #faad14;
  }

  .danger-color {
    color: #ff3000;
  }

  .success-color {
    color: #52c41a;
  }

  .info-color {
    color: #999999;
  }
</style>
