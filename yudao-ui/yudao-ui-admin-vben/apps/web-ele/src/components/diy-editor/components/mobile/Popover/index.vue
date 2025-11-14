<script setup lang="ts">
import type { PopoverProperty } from './config';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElImage } from 'element-plus';

/** 弹窗广告 */
defineOptions({ name: 'Popover' });
// 定义属性
defineProps<{ property: PopoverProperty }>();

// 处理选中
const activeIndex = ref(0);
const handleActive = (index: number) => {
  activeIndex.value = index;
};
</script>
<template>
  <div
    v-for="(item, index) in property.list"
    :key="index"
    class="absolute bottom-1/2 right-1/2 h-[454px] w-[292px] rounded border border-gray-300 bg-white p-0.5"
    :style="{
      zIndex: 100 + index + (activeIndex === index ? 100 : 0),
      marginRight: `${-146 - index * 20}px`,
      marginBottom: `${-227 - index * 20}px`,
    }"
    @click="handleActive(index)"
  >
    <ElImage :src="item.imgUrl" fit="contain" class="h-full w-full">
      <template #error>
        <div class="flex h-full w-full items-center justify-center">
          <IconifyIcon icon="ep:picture" />
        </div>
      </template>
    </ElImage>
    <div class="absolute right-1 top-1 text-xs">{{ index + 1 }}</div>
  </div>
</template>

<style scoped lang="scss"></style>
