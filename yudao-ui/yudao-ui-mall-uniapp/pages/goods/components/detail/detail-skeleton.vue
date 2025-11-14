<template>
  <view
    class="skeleton-wrap"
    :class="['theme-' + sys.mode, 'main-' + sys.theme, 'font-' + sys.fontSize]"
  >
    <view class="skeleton-banner"></view>
    <view class="container-box">
      <view class="container-box-strip title ss-m-b-58"></view>
      <view class="container-box-strip ss-m-b-20"></view>
      <view class="container-box-strip ss-m-b-20"></view>
      <view class="container-box-strip w-364"></view>
    </view>
    <view class="container-box">
      <view class="ss-flex ss-row-between ss-m-b-34">
        <view class="container-box-strip w-380"></view>
        <view class="circle"></view>
      </view>
      <view class="ss-flex ss-row-between ss-m-b-34">
        <view class="container-box-strip w-556"></view>
        <view class="circle"></view>
      </view>
      <view class="ss-flex ss-row-between">
        <view class="container-box-strip w-556"></view>
        <view class="circle"></view>
      </view>
    </view>
    <view class="container-box">
      <view class="container-box-strip w-198 ss-m-b-42"></view>
      <view class="ss-flex">
        <view class="circle ss-m-r-12"></view>
        <view class="container-box-strip w-252"></view>
      </view>
    </view>
    <su-fixed bottom placeholder bg="bg-white">
      <view class="ui-tabbar-box">
        <view class="foot ss-flex ss-col-center">
          <view class="ss-m-r-54 ss-m-l-32">
            <view class="rec ss-m-b-8"></view>
            <view class="oval"></view>
          </view>
          <view class="ss-m-r-54">
            <view class="rec ss-m-b-8"></view>
            <view class="oval"></view>
          </view>
          <view class="ss-m-r-50">
            <view class="rec ss-m-b-8"></view>
            <view class="oval"></view>
          </view>
          <button class="ss-reset-button add-btn ui-Shadow-Main"></button>
          <button class="ss-reset-button buy-btn ui-Shadow-Main"></button>
        </view>
      </view>
    </su-fixed>
  </view>
</template>

<script setup>
  import { computed } from 'vue';
  import sheep from '@/sheep';

  const sys = computed(() => sheep.$store('sys'));
</script>

<style lang="scss" scoped>
  @keyframes loading {
    0% {
      opacity: 0.5;
    }

    50% {
      opacity: 1;
    }

    100% {
      opacity: 0.5;
    }
  }

  .skeleton-wrap {
    width: 100%;
    height: 100vh;
    position: relative;

    .skeleton-banner {
      width: 100%;
      height: calc(100vh - 882rpx);
      background: #f4f4f4;
    }

    .container-box {
      padding: 24rpx 18rpx;
      margin: 14rpx 20rpx;
      background: var(--ui-BG);
      animation: loading 1.4s ease infinite;

      .container-box-strip {
        height: 40rpx;
        background: #f3f3f1;
        border-radius: 20rpx;
      }

      .title {
        width: 470rpx;
        height: 50rpx;
        border-radius: 25rpx;
      }

      .w-364 {
        width: 364rpx;
      }

      .w-380 {
        width: 380rpx;
      }

      .w-556 {
        width: 556rpx;
      }

      .w-198 {
        width: 198rpx;
      }

      .w-252 {
        width: 252rpx;
      }

      .circle {
        width: 40rpx;
        height: 40rpx;
        background: #f3f3f1;
        border-radius: 50%;
      }
    }
    .ui-tabbar-box {
      box-shadow: 0px -6px 10px 0px rgba(51, 51, 51, 0.2);
    }

    .foot {
      height: 100rpx;
      background: var(--ui-BG);
      .rec {
        width: 38rpx;
        height: 38rpx;
        background: #f3f3f1;
        border-radius: 8rpx;
      }

      .oval {
        width: 38rpx;
        height: 22rpx;
        background: #f3f3f1;
        border-radius: 11rpx;
      }
      .add-btn {
        width: 214rpx;
        height: 72rpx;
        font-weight: 500;
        font-size: 28rpx;
        border-radius: 40rpx 0 0 40rpx;
        background-color: var(--ui-BG-Main-light);
        color: var(--ui-BG-Main);
      }

      .buy-btn {
        width: 214rpx;
        height: 72rpx;
        font-weight: 500;
        font-size: 28rpx;

        border-radius: 0 40rpx 40rpx 0;
        background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
        color: $white;
      }
    }
  }
</style>
