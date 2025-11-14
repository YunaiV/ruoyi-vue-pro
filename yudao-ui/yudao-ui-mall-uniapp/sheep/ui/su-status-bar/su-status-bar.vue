<!-- 自定义状态栏 -->
<template>
  <view :style="{ height: statusBarHeight }" class="uni-status-bar"><slot /></view>
</template>

<script setup>
  import sheep from '@/sheep';

  const statusBarHeight = sheep.$platform.device.statusBarHeight + 'px';
</script>
<style lang="scss">
  .uni-status-bar {
    // width: 750rpx;
    height: var(--status-bar-height);
  }
</style>
