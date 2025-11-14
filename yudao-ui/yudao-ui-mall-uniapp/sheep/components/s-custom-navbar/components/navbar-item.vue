<!-- 顶部导航栏 - 单元格 -->
<template>
  <view class="ss-flex ss-col-center">
    <!-- 类型一： 文字 -->
    <view
      v-if="data.type === 'text'"
      class="nav-title inline"
      :style="[{ color: data.textColor, width: width }]"
      @tap="sheep.$router.go(data.url)"
    >
      {{ data.text }}
    </view>
    <!-- 类型二： 图片 -->
    <view
      v-if="data.type === 'image'"
      :style="[{ width: width }]"
      class="menu-icon-wrap ss-flex ss-row-center ss-col-center"
      @tap="sheep.$router.go(data.url)"
    >
      <image class="nav-image" :src="sheep.$url.cdn(data.imgUrl)" mode="aspectFit"></image>
    </view>
    <!-- 类型三： 搜索框 -->
    <view class="ss-flex-1" v-if="data.type === 'search'" :style="[{ width: width }]">
      <s-search-block
        :placeholder="data.placeholder || '搜索关键字'"
        :placeholder-position="data.placeholderPosition"
        :radius="data.borderRadius"
        :el-background="data.backgroundColor"
        :font-color="data.textColor"
        :height="height"
        :width="width"
        :show-scan="data.showScan"
        @click="sheep.$router.go('/pages/index/search')"
      ></s-search-block>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { computed } from 'vue';

  // 接收参数
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    width: {
      type: String,
      default: '1px',
    },
  });

  const height = computed(() => sheep.$platform.capsule.height);
</script>

<style lang="scss" scoped>
  .nav-title {
    font-size: 36rpx;
    color: #333;
    text-align: center;
  }

  .menu-icon-wrap {
    .nav-image {
      height: 24px;
    }
  }
</style>
