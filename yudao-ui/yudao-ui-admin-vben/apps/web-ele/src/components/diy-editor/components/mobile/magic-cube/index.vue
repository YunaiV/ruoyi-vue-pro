<script setup lang="ts">
import type { MagicCubeProperty } from './config';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElImage } from 'element-plus';

/** 广告魔方 */
defineOptions({ name: 'MagicCube' });
const props = defineProps<{ property: MagicCubeProperty }>();
// 一个方块的大小
const CUBE_SIZE = 93.75;
/**
 * 计算方块的行数
 * 行数用于计算魔方的总体高度，存在以下情况：
 * 1. 没有数据时，默认就只显示一行的高度
 * 2. 底部的空白不算高度，例如只有第一行有数据，那么就只显示一行的高度
 * 3. 顶部及中间的空白算高度，例如一共有四行，只有最后一行有数据，那么也显示四行的高度
 */
const rowCount = computed(() => {
  let count = 0;
  if (props.property.list.length > 0) {
    // 最大行号
    count = Math.max(
      ...props.property.list.map((item) => item.top + item.height),
    );
  }
  // 保证至少有一行
  return count === 0 ? 1 : count;
});
</script>

<template>
  <div
    class="relative"
    :style="{
      height: `${rowCount * CUBE_SIZE}px`,
      width: `${4 * CUBE_SIZE}px`,
      padding: `${property.space}px`,
    }"
  >
    <div
      v-for="(item, index) in property.list"
      :key="index"
      class="absolute"
      :style="{
        width: `${item.width * CUBE_SIZE - property.space}px`,
        height: `${item.height * CUBE_SIZE - property.space}px`,
        top: `${item.top * CUBE_SIZE}px`,
        left: `${item.left * CUBE_SIZE}px`,
      }"
    >
      <ElImage
        class="h-full w-full"
        fit="cover"
        :src="item.imgUrl"
        :style="{
          borderTopLeftRadius: `${property.borderRadiusTop}px`,
          borderTopRightRadius: `${property.borderRadiusTop}px`,
          borderBottomLeftRadius: `${property.borderRadiusBottom}px`,
          borderBottomRightRadius: `${property.borderRadiusBottom}px`,
        }"
      >
        <template #error>
          <div class="image-slot">
            <div
              class="flex items-center justify-center"
              :style="{
                width: `${item.width * CUBE_SIZE}px`,
                height: `${item.height * CUBE_SIZE}px`,
              }"
            >
              <IconifyIcon icon="ep-picture" color="gray" :size="CUBE_SIZE" />
            </div>
          </div>
        </template>
      </ElImage>
    </div>
  </div>
</template>

<style scoped lang="scss"></style>
