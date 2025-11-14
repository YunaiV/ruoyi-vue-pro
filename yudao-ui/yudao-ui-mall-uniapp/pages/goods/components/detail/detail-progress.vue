<!-- 秒杀商品：抢购进度 -->
<template>
  <view class="ss-flex ss-col-center">
    <view class="progress-title ss-m-r-10"> 已抢{{ percent }}% </view>
    <view class="progress-box ss-flex ss-col-center">
      <view class="progerss-active" :style="{ width: percent < 10 ? '10%' : percent + '%' }">
      </view>
    </view>
  </view>
</template>

<script setup>
  const props = defineProps({
    percent: {
      type: Number,
      default: 0,
    },
  });
</script>

<style lang="scss" scoped>
  .progress-title {
    font-size: 20rpx;
    font-weight: 500;
    color: #ffffff;
  }

  .progress-box {
    width: 168rpx;
    height: 18rpx;
    background: #f6f6f6;
    border-radius: 9rpx;
  }

  .progerss-active {
    height: 24rpx;
    background: linear-gradient(86deg, #f60600, #d00500);
    border-radius: 12rpx;
  }
</style>
