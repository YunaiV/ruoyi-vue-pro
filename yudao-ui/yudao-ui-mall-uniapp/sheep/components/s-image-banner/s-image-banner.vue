<!-- 装修图文组件：图片轮播 -->
<template>
  <su-swiper
    :list="imgList"
    :dotStyle="data.indicator === 'dot' ? 'long' : 'tag'"
    imageMode="scaleToFill"
    dotCur="bg-mask-40"
    :seizeHeight="300"
    :autoplay="data.autoplay"
    :interval="data.interval * 1000"
    :mode="data.type"
    :height="px2rpx(data.height)"
  />
</template>

<script setup>
  import { computed } from 'vue';
  import sheep from '@/sheep';

  // 轮播图
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

  function px2rpx(px) {
    //计算比例
    let scale = uni.upx2px(100)/100;
    return px/scale
  }

  const imgList = computed(() =>
      props.data.items.map((item) => {
        const src = item.type === 'img' ? item.imgUrl : item.videoUrl;
        return {
          ...item,
          type: item.type === 'img' ? 'image' : 'video',
          src: sheep.$url.cdn(src),
          poster: sheep.$url.cdn(item.imgUrl),
        };
      }),
  );
</script>

<style></style>
