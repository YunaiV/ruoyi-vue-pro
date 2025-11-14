<!-- 装修图文组件：热区 -->
<template>
  <view class="hotzone-wrap">
    <image :src="sheep.$url.cdn(data.imgUrl)" style="width: 100%" mode="widthFix"></image>
    <view
      class="hotzone-box"
      v-for="(item, index) in data.list"
      :key="index"
      :style="[
        {
          top: `${item.top}px`,
          left: `${item.left}px`,
          width: `${item.width}px`,
          height: `${item.height}px`,
        },
      ]"
      @tap.stop="sheep.$router.go(item.url)"
    >
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';

  // 接收参数
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    styles: {
      type: Object,
      default: () => ({}),
    },
  });
</script>

<style lang="scss" scoped>
  .hotzone-wrap {
    position: relative;
  }
  .hotzone-box {
    position: absolute;
  }
</style>
