<script setup lang="ts">
import type { HotZoneProperty } from './config';

import { ElImage } from 'element-plus';

/** 热区 */
defineOptions({ name: 'HotZone' });

const props = defineProps<{ property: HotZoneProperty }>();
</script>

<template>
  <div class="min-h-30px relative h-full w-full">
    <ElImage
      :src="props.property.imgUrl"
      class="pointer-events-none h-full w-full select-none"
    />
    <div
      v-for="(item, index) in props.property.list"
      :key="index"
      class="absolute z-10 flex cursor-move items-center justify-center border text-sm opacity-80"
      :style="{
        width: `${item.width}px`,
        height: `${item.height}px`,
        top: `${item.top}px`,
        left: `${item.left}px`,
        color: 'var(--el-color-primary)',
        background: 'var(--el-color-primary-light-7)',
        borderColor: 'var(--el-color-primary)',
      }"
    >
      {{ item.name }}
    </div>
  </div>
</template>
