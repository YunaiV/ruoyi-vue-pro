<!-- 分销权限弹窗：再没有权限时，进行提示  -->
<template>
  <su-popup
    :show="state.show"
    type="center"
    round="10"
    @close="state.show = false"
    :isMaskClick="false"
    maskBackgroundColor="rgba(0, 0, 0, 0.7)"
  >
    <view class="notice-box">
      <view class="img-wrap">
        <image
          class="notice-img"
          :src="sheep.$url.static('/static/img/shop/commission/forbidden.png')"
          mode="aspectFill"
        />
      </view>
      <view class="notice-title"> 抱歉！您没有分销权限 </view>
      <view class="notice-detail"> 该功能暂不可用 </view>
      <button
        class="ss-reset-button notice-btn ui-Shadow-Main ui-BG-Main-Gradient"
        @tap="sheep.$router.back()"
      >
        知道了
      </button>
      <button class="ss-reset-button back-btn" @tap="sheep.$router.back()"> 返回 </button>
    </view>
  </su-popup>
</template>

<script setup>
  import { onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { reactive } from 'vue';
  import BrokerageApi from '@/sheep/api/trade/brokerage';

  const state = reactive({
    show: false,
  });

  onShow(async () => {
    // 读取是否有分销权限
    const { code, data } = await BrokerageApi.getBrokerageUser();
    if (code === 0 && !data?.brokerageEnabled) {
      state.show = true;
    }
  });
</script>

<style lang="scss" scoped>
  .notice-box {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background-color: #fff;
    width: 612rpx;
    min-height: 658rpx;
    background: #ffffff;
    padding: 30rpx;
    border-radius: 20rpx;
    .img-wrap {
      margin-bottom: 50rpx;
      .notice-img {
        width: 180rpx;
        height: 170rpx;
      }
    }
    .notice-title {
      font-size: 35rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 28rpx;
    }
    .notice-detail {
      font-size: 28rpx;
      font-weight: 400;
      color: #999999;
      line-height: 36rpx;
      margin-bottom: 50rpx;
    }
    .notice-btn {
      width: 492rpx;
      line-height: 70rpx;
      border-radius: 35rpx;
      font-size: 28rpx;
      font-weight: 500;
      color: #ffffff;
      margin-bottom: 10rpx;
    }
    .back-btn {
      width: 492rpx;
      line-height: 70rpx;
      font-size: 28rpx;
      font-weight: 500;
      color: var(--ui-BG-Main-gradient);
      background: none;
    }
  }
</style>
