<script setup lang="ts">
import type { TitleBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { ElImage } from 'element-plus';

/** 标题栏 */
defineOptions({ name: 'TitleBar' });

defineProps<{ property: TitleBarProperty }>();
</script>
<template>
  <div
    class="relative box-border min-h-[20px] w-full"
    :style="{ height: `${property.height}px` }"
  >
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
      class="absolute bottom-0 right-2 top-0 m-auto flex items-center justify-center text-[10px] text-[#969799]"
      v-show="property.more.show"
      :style="{
        color: property.descriptionColor,
      }"
    >
      <span v-if="property.more.type !== 'icon'">
        {{ property.more.text }}
      </span>
      <IconifyIcon
        icon="lucide:arrow-right"
        v-if="property.more.type !== 'text'"
      />
    </div>
  </div>
</template>
