<!-- 装修基础组件：列表导航 -->
<template>
  <view class="menu-list-wrap">
    <uni-list :border="true">
      <uni-list-item
        v-for="(item, index) in data.list"
        :key="index"
        showArrow
        clickable
        @tap="sheep.$router.go(item.url)"
      >
        <template v-slot:header>
          <view class="ss-flex ss-col-center">
            <image
              v-if="item.iconUrl"
              class="list-icon"
              :src="sheep.$url.cdn(item.iconUrl)"
              mode="aspectFit"
            ></image>
            <view
              class="title-text ss-flex ss-row-center ss-col-center ss-m-l-20"
              :style="[{ color: item.titleColor }]"
            >
              {{ item.title }}
            </view>
          </view>
        </template>
        <template v-slot:footer>
          <view
            class="notice-text ss-flex ss-row-center ss-col-center"
            :style="[{ color: item.subtitleColor }]"
          >
            {{ item.subtitle }}
          </view>
        </template>
      </uni-list-item>
    </uni-list>
  </view>
</template>

<script setup>
  /**
   * cell
   */
  import sheep from '@/sheep';
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
  });
</script>

<style lang="scss">
  .list-icon {
    width: 20px;
    height: 20px;
  }
  .notice-text {
  }
  .menu-list-wrap {
    ::v-deep .uni-list {
      background-color: transparent;
    }
  }
</style>
