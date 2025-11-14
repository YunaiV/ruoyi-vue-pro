<script setup lang="ts">
import type { MenuListProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { ElImage } from 'element-plus';

/** 列表导航 */
defineOptions({ name: 'MenuList' });
defineProps<{ property: MenuListProperty }>();
</script>

<template>
  <div class="flex min-h-[42px] flex-col">
    <div
      v-for="(item, index) in property.list"
      :key="index"
      class="item flex h-[42px] flex-row items-center justify-between gap-1 px-3"
    >
      <div class="flex flex-1 flex-row items-center gap-2">
        <ElImage v-if="item.iconUrl" class="h-4 w-4" :src="item.iconUrl" />
        <span class="text-base" :style="{ color: item.titleColor }">{{
          item.title
        }}</span>
      </div>
      <div class="item-center flex flex-row justify-center gap-1">
        <span class="text-xs" :style="{ color: item.subtitleColor }">{{
          item.subtitle
        }}</span>
        <IconifyIcon icon="ep:arrow-right" color="#000" :size="16" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.item + .item {
  border-top: 1px solid #eee;
}
</style>
