<script setup lang="ts">
import type { ImageBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { ElImage } from 'element-plus';

/** 图片展示 */
defineOptions({ name: 'ImageBar' });

defineProps<{ property: ImageBarProperty }>();
</script>
<template>
  <div
    class="flex h-12 items-center justify-center bg-gray-300"
    v-if="!property.imgUrl"
  >
    <IconifyIcon icon="ep:picture" class="text-3xl text-gray-600" />
  </div>
  <ElImage v-else class="block h-full w-full" :src="property.imgUrl" />
</template>
