<template>
  <u-swiper :list="bannerList" :keyName="keyName" previousMargin="20" nextMargin="20" circular height="150" @change="e => (current = e.current)" :autoplay="true" @click="handleSwiperClick">
    <view slot="indicator" class="indicator">
      <view class="indicator__dot" v-for="(item, index) in bannerList" :key="index" :class="[index === current && 'indicator__dot--active']"></view>
    </view>
  </u-swiper>
</template>

<script>
/**
 * 广告滚动图
 */
export default {
  name: 'yd-banner',
  components: {},
  props: {
    keyName: {
      type: String,
      default: 'url'
    },
    bannerList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      current: 0,
      currentNum: 0
    }
  },
  methods: {
    handleSwiperClick(index) {
      console.log('点击了图片索引值：', index)
    }
  }
}
</script>

<style lang="scss" scoped>
.indicator {
  @include flex(row);
  justify-content: center;

  &__dot {
    height: 15rpx;
    width: 15rpx;
    border-radius: 100rpx;
    background-color: rgba(255, 255, 255, 0.35);
    margin: 0 10rpx;
    transition: background-color 0.3s;

    &--active {
      background-color: $custom-bg-color;
    }
  }
}
</style>
