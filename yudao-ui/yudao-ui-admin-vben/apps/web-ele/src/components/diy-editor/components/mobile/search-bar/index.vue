<script setup lang="ts">
import type { SearchProperty } from './config';

import { IconifyIcon } from '@vben/icons';

/** 搜索框 */
defineOptions({ name: 'SearchBar' });
defineProps<{ property: SearchProperty }>();
</script>

<template>
  <div
    class="search-bar"
    :style="{
      color: property.textColor,
    }"
  >
    <!-- 搜索框 -->
    <div
      class="inner"
      :style="{
        height: `${property.height}px`,
        background: property.backgroundColor,
        borderRadius: `${property.borderRadius}px`,
      }"
    >
      <div
        class="placeholder"
        :style="{
          justifyContent: property.placeholderPosition,
        }"
      >
        <IconifyIcon icon="ep:search" />
        <span>{{ property.placeholder || '搜索商品' }}</span>
      </div>
      <div class="right">
        <!-- 搜索热词 -->
        <span v-for="(keyword, index) in property.hotKeywords" :key="index">{{
          keyword
        }}</span>
        <!-- 扫一扫 -->
        <IconifyIcon
          icon="ant-design:scan-outlined"
          v-show="property.showScan"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.search-bar {
  /* 搜索框 */
  .inner {
    position: relative;
    display: flex;
    align-items: center;
    min-height: 28px;
    font-size: 14px;

    .placeholder {
      display: flex;
      gap: 2px;
      align-items: center;
      width: 100%;
      padding: 0 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      word-break: break-all;
      white-space: nowrap;
    }

    .right {
      position: absolute;
      right: 8px;
      display: flex;
      gap: 8px;
      align-items: center;
      justify-content: center;
    }
  }
}
</style>
