<template>
  <view
    class="ss-flex-col ss-col-center ss-row-center empty-box"
    :style="[{ paddingTop: paddingTop + 'rpx' }]"
  >
    <view class=""><image class="empty-icon" :src="icon" mode="widthFix"></image></view>
    <view class="empty-text ss-m-t-28 ss-m-b-40">
      <text v-if="text !== ''">{{ text }}</text>
    </view>
    <button class="ss-reset-button empty-btn" v-if="showAction" @tap="clickAction">
      {{ actionText }}
    </button>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  /**
   * 容器组件 - 装修组件的样式容器
   */

  const props = defineProps({
    // 图标
    icon: {
      type: String,
      default: '',
    },
    // 描述
    text: {
      type: String,
      default: '',
    },
    // 是否显示button
    showAction: {
      type: Boolean,
      default: false,
    },
    // button 文字
    actionText: {
      type: String,
      default: '',
    },
    // 链接
    actionUrl: {
      type: String,
      default: '',
    },
    // 间距
    paddingTop: {
      type: String,
      default: '260',
    },
    //主题色
    buttonColor: {
      type: String,
      default: 'var(--ui-BG-Main)',
    },
  });

  const emits = defineEmits(['clickAction']);

  function clickAction() {
    if (props.actionUrl !== '') {
      sheep.$router.go(props.actionUrl);
    }
    emits('clickAction');
  }
</script>

<style lang="scss" scoped>
  .empty-box {
    width: 100%;
  }
  .empty-icon {
    width: 240rpx;
  }

  .empty-text {
    font-size: 26rpx;
    font-weight: 500;
    color: #999999;
  }

  .empty-btn {
    width: 320rpx;
    height: 70rpx;
    border: 2rpx solid v-bind('buttonColor');
    border-radius: 35rpx;
    font-weight: 500;
    color: v-bind('buttonColor');
    font-size: 28rpx;
  }
</style>
