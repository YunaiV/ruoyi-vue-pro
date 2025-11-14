<script setup lang="ts">
import type { NoticeBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { ElCarousel, ElCarouselItem, ElDivider, ElImage } from 'element-plus';

/** 公告栏 */
defineOptions({ name: 'NoticeBar' });

defineProps<{ property: NoticeBarProperty }>();
</script>

<template>
  <div
    class="flex items-center py-1 text-xs"
    :style="{
      backgroundColor: property.backgroundColor,
      color: property.textColor,
    }"
  >
    <ElImage :src="property.iconUrl" class="h-[18px]" />
    <ElDivider direction="vertical" />
    <ElCarousel
      height="24px"
      direction="vertical"
      :autoplay="true"
      class="flex-1 pr-2"
    >
      <ElCarouselItem v-for="(item, index) in property.contents" :key="index">
        <div class="h-6 truncate leading-6">{{ item.text }}</div>
      </ElCarouselItem>
    </ElCarousel>
    <IconifyIcon icon="ep:arrow-right" />
  </div>
</template>

<style scoped lang="scss"></style>
