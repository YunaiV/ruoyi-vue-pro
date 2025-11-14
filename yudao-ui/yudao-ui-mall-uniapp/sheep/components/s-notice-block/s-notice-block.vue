<template>
  <view class="ss-flex ss-col-center notice-wrap">
    <image class="icon-img" :src="sheep.$url.cdn(data.iconUrl)" mode="heightFix"></image>
    <!-- todo：@owen 暂时只支持一个公告   -->
    <su-notice-bar
      style="flex: 1"
      :showIcon="false"
      scrollable
      single
      :text="data.contents[0].text"
      :speed="50"
      :color="data.textColor"
      @tap="sheep.$router.go(data.contents[0].url)"
    />
  </view>
</template>

<script setup>
  /**
   * 装修组件  - 通知栏
   *
   */
  import sheep from '@/sheep';
  const props = defineProps({
    data: {
      type: Object,
      default() {},
    },
  });
</script>

<style lang="scss" scoped>
  .notice-wrap {
    .icon-img {
      height: 56rpx;
    }
  }
</style>
