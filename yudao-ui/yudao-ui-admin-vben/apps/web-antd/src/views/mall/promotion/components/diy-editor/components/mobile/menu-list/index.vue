<script setup lang="ts">
import type { MenuListProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { Image } from 'ant-design-vue';

/** 列表导航 */
defineOptions({ name: 'MenuList' });

defineProps<{ property: MenuListProperty }>();
</script>

<template>
  <div class="flex min-h-10 flex-col">
    <div
      v-for="(item, index) in property.list"
      :key="index"
      class="flex h-10 flex-row items-center justify-between gap-1 border-t border-gray-200 px-3 first:border-t-0"
    >
      <div class="flex flex-1 flex-row items-center gap-2">
        <Image
          v-if="item.iconUrl"
          class="h-4 w-4"
          :src="item.iconUrl"
          :preview="false"
        />
        <span class="text-base" :style="{ color: item.titleColor }">
          {{ item.title }}
        </span>
      </div>
      <div class="item-center flex flex-row justify-center gap-1">
        <span class="text-xs" :style="{ color: item.subtitleColor }">
          {{ item.subtitle }}
        </span>
        <IconifyIcon icon="ep:arrow-right" color="#000" :size="16" />
      </div>
    </div>
  </div>
</template>
