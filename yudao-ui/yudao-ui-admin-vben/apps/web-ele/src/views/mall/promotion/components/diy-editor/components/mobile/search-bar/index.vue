<script setup lang="ts">
import type { SearchProperty } from './config';

import { IconifyIcon } from '@vben/icons';

/** 搜索框 */
defineOptions({ name: 'SearchBar' });

defineProps<{ property: SearchProperty }>();
</script>

<template>
  <div
    :style="{
      color: property.textColor,
    }"
  >
    <!-- 搜索框 -->
    <div
      class="relative flex min-h-7 items-center text-sm"
      :style="{
        height: `${property.height}px`,
        background: property.backgroundColor,
        borderRadius: `${property.borderRadius}px`,
      }"
    >
      <div
        class="flex w-full items-center gap-0.5 overflow-hidden text-ellipsis whitespace-nowrap break-all px-2"
        :style="{
          justifyContent: property.placeholderPosition,
        }"
      >
        <IconifyIcon icon="lucide:search" />
        <span>{{ property.placeholder || '搜索商品' }}</span>
      </div>
      <div class="absolute right-2 flex items-center justify-center gap-2">
        <!-- 搜索热词 -->
        <span v-for="(keyword, index) in property.hotKeywords" :key="index">
          {{ keyword }}
        </span>
        <!-- 扫一扫 -->
        <IconifyIcon icon="lucide:scan-barcode" v-show="property.showScan" />
      </div>
    </div>
  </div>
</template>
