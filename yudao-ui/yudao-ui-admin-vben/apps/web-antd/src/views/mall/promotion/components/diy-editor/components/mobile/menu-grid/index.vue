<script setup lang="ts">
import type { MenuGridProperty } from './config';

import { Image } from 'ant-design-vue';

/** 宫格导航 */
defineOptions({ name: 'MenuGrid' });

defineProps<{ property: MenuGridProperty }>();
</script>

<template>
  <div class="flex flex-row flex-wrap">
    <div
      v-for="(item, index) in property.list"
      :key="index"
      class="relative flex flex-col items-center pb-4 pt-4"
      :style="{ width: `${100 * (1 / property.column)}%` }"
    >
      <!-- 右上角角标 -->
      <span
        v-if="item.badge?.show"
        class="absolute left-1/2 top-2 z-10 h-4 rounded-full px-2 text-center text-xs leading-5"
        :style="{
          color: item.badge.textColor,
          backgroundColor: item.badge.bgColor,
        }"
      >
        {{ item.badge.text }}
      </span>
      <Image
        v-if="item.iconUrl"
        :width="32"
        :src="item.iconUrl"
        :preview="false"
      />
      <span
        class="mt-2 h-4 text-xs leading-4"
        :style="{ color: item.titleColor }"
      >
        {{ item.title }}
      </span>
      <span
        class="mt-1.5 h-3 text-xs leading-3"
        :style="{ color: item.subtitleColor }"
      >
        {{ item.subtitle }}
      </span>
    </div>
  </div>
</template>
