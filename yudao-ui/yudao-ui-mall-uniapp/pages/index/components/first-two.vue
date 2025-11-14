<!-- 分类展示：first-two 风格  -->
<template>
  <view>
    <view class="ss-flex flex-wrap">
      <view class="goods-box" v-for="item in pagination?.list" :key="item.id">
        <view @click="sheep.$router.go('/pages/goods/index', { id: item.id })">
          <view class="goods-img">
            <image class="goods-img" :src="item.picUrl" mode="aspectFit" />
          </view>
          <view class="goods-content">
            <view class="goods-title ss-line-1 ss-m-b-28">{{ item.name }}</view>
            <view class="goods-price">￥{{ fen2yuan(item.price) }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { fen2yuan } from '@/sheep/hooks/useGoods';

  const props = defineProps({
    pagination: Object,
  });
</script>

<style lang="scss" scoped>
  .goods-box {
    width: calc((100% - 20rpx) / 2);
    margin-bottom: 20rpx;

    .goods-img {
      width: 100%;
      height: 246rpx;
      border-radius: 10rpx 10rpx 0px 0px;
    }

    .goods-content {
      width: 100%;
      background: #ffffff;
      box-shadow: 0px 0px 20rpx 4rpx rgba(199, 199, 199, 0.22);
      padding: 20rpx 0 32rpx 16rpx;
      box-sizing: border-box;
      border-radius: 0 0 10rpx 10rpx;

      .goods-title {
        font-size: 26rpx;
        font-weight: bold;
        color: #333333;
      }

      .goods-price {
        font-size: 24rpx;
        font-family: OPPOSANS;
        font-weight: 500;
        color: #e1212b;
      }
    }

    &:nth-child(2n + 1) {
      margin-right: 20rpx;
    }
  }
</style>
