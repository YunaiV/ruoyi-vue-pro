<script setup lang="ts">
import type { CarouselProperty } from './config';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElCarousel, ElCarouselItem, ElImage } from 'element-plus';

/** 轮播图 */
defineOptions({ name: 'Carousel' });

defineProps<{ property: CarouselProperty }>();

const currentIndex = ref(0);
const handleIndexChange = (index: number) => {
  currentIndex.value = index + 1;
};
</script>
<template>
  <!-- 无图片 -->
  <div
    class="flex h-[250px] items-center justify-center bg-gray-300"
    v-if="property.items.length === 0"
  >
    <IconifyIcon icon="tdesign:image" class="text-[120px] text-gray-800" />
  </div>
  <div v-else class="relative">
    <ElCarousel
      height="174px"
      :type="property.type === 'card' ? 'card' : ''"
      :autoplay="property.autoplay"
      :interval="property.interval * 1000"
      :indicator-position="property.indicator === 'number' ? 'none' : undefined"
      @change="handleIndexChange"
    >
      <ElCarouselItem v-for="(item, index) in property.items" :key="index">
        <ElImage class="h-full w-full" :src="item.imgUrl" />
      </ElCarouselItem>
    </ElCarousel>
    <div
      v-if="property.indicator === 'number'"
      class="absolute bottom-[10px] right-[10px] rounded-xl bg-black px-[8px] py-[2px] text-[10px] text-white opacity-40"
    >
      {{ currentIndex }} / {{ property.items.length }}
    </div>
  </div>
</template>

<style scoped lang="scss"></style>
