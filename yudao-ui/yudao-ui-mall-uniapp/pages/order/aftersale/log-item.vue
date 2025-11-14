<!-- 售后日志的每一项展示 -->
<template>
  <view class="log-item ss-flex">
    <view class="log-icon ss-flex-col ss-col-center ss-m-r-20">
      <text class="cicon-title" :class="index === 0 ? 'activity-color' : ''" />
      <view v-if="data.length - 1 !== index" class="line" />
    </view>
    <view>
      <view class="text">{{ item.content }}</view>
      <view class="date">
        {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}
      </view>
    </view>
  </view>
</template>
<script setup>
  import sheep from '@/sheep';

  const props = defineProps({
    item: {
      type: Object, // 当前日志
      default() {},
    },
    index: {
      type: Number, // item 在 data 的下标
      default: 0,
    },
    data: {
      type: Object, // 日志列表
      default() {},
    },
  });
</script>
<style lang="scss" scoped>
  .log-item {
    align-items: stretch;
  }
  .log-icon {
    height: inherit;
    .cicon-title {
      font-size: 30rpx;
      color: #dfdfdf;
    }
    .activity-color {
      color: #60bd45;
    }
    .line {
      width: 1px;
      height: 100%;
      background: #dfdfdf;
    }
  }
  .text {
    font-size: 28rpx;
    font-weight: 500;
    color: #333333;
  }
  .richtext {
    font-size: 24rpx;
    font-weight: 500;
    color: #999999;
    margin: 20rpx 0 0 0;
  }
  .content-img {
    margin-top: 20rpx;
    width: 200rpx;
    height: 200rpx;
  }
  .date {
    margin-top: 20rpx;
    font-size: 24rpx;
    font-family: OPPOSANS;
    font-weight: 400;
    color: #999999;
    margin-bottom: 40rpx;
  }

  :deep() {
    image {
      display: block;
    }
  }
</style>
