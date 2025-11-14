<script setup lang="ts">
import type { TitleBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { ElImage } from 'element-plus';

/** 标题栏 */
defineOptions({ name: 'TitleBar' });

defineProps<{ property: TitleBarProperty }>();
</script>
<template>
  <div class="title-bar" :style="{ height: `${property.height}px` }">
    <ElImage
      v-if="property.bgImgUrl"
      :src="property.bgImgUrl"
      fit="cover"
      class="w-full"
    />
    <div
      class="absolute left-0 top-0 flex h-full w-full flex-col justify-center"
    >
      <!-- 标题 -->
      <div
        :style="{
          fontSize: `${property.titleSize}px`,
          fontWeight: property.titleWeight,
          color: property.titleColor,
          textAlign: property.textAlign,
          marginLeft: `${property.marginLeft}px`,
          marginBottom: '4px',
        }"
        v-if="property.title"
      >
        {{ property.title }}
      </div>
      <!-- 副标题 -->
      <div
        :style="{
          fontSize: `${property.descriptionSize}px`,
          fontWeight: property.descriptionWeight,
          color: property.descriptionColor,
          textAlign: property.textAlign,
          marginLeft: `${property.marginLeft}px`,
        }"
        v-if="property.description"
      >
        {{ property.description }}
      </div>
    </div>
    <!-- 更多 -->
    <div
      class="more"
      v-show="property.more.show"
      :style="{
        color: property.descriptionColor,
      }"
    >
      <span v-if="property.more.type !== 'icon'">
        {{ property.more.text }}
      </span>
      <IconifyIcon icon="ep:arrow-right" v-if="property.more.type !== 'text'" />
    </div>
  </div>
</template>
<style scoped lang="scss">
.title-bar {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  min-height: 20px;

  /* 更多 */
  .more {
    position: absolute;
    top: 0;
    right: 8px;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: auto;
    font-size: 10px;
    color: #969799;
  }
}
</style>
