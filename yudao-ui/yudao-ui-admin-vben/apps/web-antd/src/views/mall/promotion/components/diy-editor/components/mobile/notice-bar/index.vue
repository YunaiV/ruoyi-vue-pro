<script setup lang="ts">
import type { NoticeBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { Carousel, Divider, Image } from 'ant-design-vue';

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
    <Image :src="property.iconUrl" class="h-[18px]" :preview="false" />
    <Divider type="vertical" />
    <Carousel
      :autoplay="true"
      :dots="false"
      vertical
      class="flex-1 pr-2"
      style="height: 24px"
    >
      <div v-for="(item, index) in property.contents" :key="index">
        <div class="h-6 truncate leading-6">{{ item.text }}</div>
      </div>
    </Carousel>
    <IconifyIcon icon="lucide:arrow-right" />
  </div>
</template>
