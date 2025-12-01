<script setup lang="ts">
import type { TabBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { Image } from 'ant-design-vue';

/** 页面底部导航栏 */
defineOptions({ name: 'TabBar' });

defineProps<{ property: TabBarProperty }>();
</script>
<template>
  <div class="z-[2] w-full">
    <div
      class="flex flex-row items-center justify-around py-2"
      :style="{
        background:
          property.style.bgType === 'color'
            ? property.style.bgColor
            : `url(${property.style.bgImg})`,
        backgroundSize: '100% 100%',
        backgroundRepeat: 'no-repeat',
      }"
    >
      <div
        v-for="(item, index) in property.items"
        :key="index"
        class="flex w-full flex-col items-center justify-center text-xs"
      >
        <Image
          :src="index === 0 ? item.activeIconUrl : item.iconUrl"
          class="!h-[26px] w-[26px] rounded"
          :preview="false"
        >
          <template #error>
            <div class="flex h-full w-full items-center justify-center">
              <IconifyIcon
                icon="lucide:image"
                class="h-[26px] w-[26px] rounded"
              />
            </div>
          </template>
        </Image>
        <span
          :style="{
            color:
              index === 0 ? property.style.activeColor : property.style.color,
          }"
        >
          {{ item.text }}
        </span>
      </div>
    </div>
  </div>
</template>
