<template>
  <view class="wrap" :style="{height: `${height}px`}">
    <view class="divider" :style="[elStyle]"></view>
  </view>
</template>

<script setup>
  /**
   * 分割线
   */

  import { computed } from 'vue';

  // 接收参数
  const props = defineProps({
    // 线条颜色
    lineColor: {
      type: String,
      default: '#000',
    },
    // 线条样式：'dotted', 'solid', 'double', 'dashed'
    borderType: {
      type: String,
      default: 'dashed',
    },
    // 线条宽度
    lineWidth: {
      type: Number,
      default: 1,
    },
    // 高度
    height: {
      type: [Number, String],
      default: 'auto'
    },
    // 左右边距：none - 无边距，horizontal - 左右留边
    paddingType: {
      type: String,
      default: 'none'
    }
  });

  const elStyle = computed(() => {
    return {
      'border-top-width': `${props.lineWidth}px`,
      'border-top-color': props.lineColor,
      'border-top-style': props.borderType,
      margin: props.paddingType === 'none' ? '0' : '0px 16px'
    };
  });
</script>

<style lang="scss" scoped>
.wrap {
  display: flex;
  align-items: center;

  .divider {
    width: 100%;
  }
}
</style>
