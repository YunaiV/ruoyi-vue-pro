<!-- 页面；暂时没用到  -->
<template>
  <view class="list-goods-card ss-flex-col" @tap="onClick">
    <view class="md-img-box">
      <image class="goods-img md-img-box" :src="sheep.$url.cdn(img)" mode="aspectFill"></image>
    </view>
    <view class="md-goods-content ss-flex-col ss-row-around">
      <view class="md-goods-title ss-line-2 ss-m-x-20 ss-m-t-6 ss-m-b-16">{{ title }}</view>
      <view class="md-goods-subtitle ss-line-1 ss-p-y-10 ss-p-20">{{ subTitle }}</view>
      <view class="ss-flex ss-col-center ss-row-between ss-m-b-16 ss-m-x-20">
        <view class="md-goods-price text-price">{{ price }}</view>
        <view class="goods-origin-price text-price">{{ originPrice }}</view>
        <view class="sales-text">已售{{ sales }}件</view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { onLoad } from '@dcloudio/uni-app';
  import { computed, reactive } from 'vue';

  const props = defineProps({
    img: {
      type: String,
      default: '',
    },
    subTitle: {
      type: String,
      default: '',
    },
    title: {
      type: String,
      default: '',
    },
    price: {
      type: [String, Number],
      default: '',
    },
    originPrice: {
      type: [String, Number],
      default: '',
    },
    sales: {
      type: [String, Number],
      default: '',
    },
  });
  const emits = defineEmits(['click']);
  const onClick = () => {
    emits('click');
  };
</script>

<style lang="scss" scoped>
  .goods-img {
    width: 100%;
    height: 100%;
    background-color: #f5f5f5;
  }

  .sales-text {
    font-size: 20rpx;
    color: #c4c4c4;
  }

  .goods-origin-price {
    font-size: 20rpx;
    color: #c4c4c4;
    text-decoration: line-through;
  }

  .list-goods-card {
    overflow: hidden;
    width: 344rpx;
    position: relative;
    z-index: 1;
    background-color: $white;
    box-shadow: 0 0 20rpx 4rpx rgba(199, 199, 199, 0.22);
    border-radius: 20rpx;

    .md-img-box {
      width: 344rpx;
      height: 344rpx;
    }

    .md-goods-title {
      font-size: 26rpx;
      color: #333;
    }
    .md-goods-subtitle {
      background-color: var(--ui-BG-Main-tag);
      color: var(--ui-BG-Main);
      font-size: 20rpx;
    }

    .md-goods-price {
      font-size: 30rpx;
      color: $red;
    }
  }
</style>
