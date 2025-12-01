<script setup lang="ts">
import type { HotZoneProperty } from './config';

import { Image } from 'ant-design-vue';

/** 热区 */
defineOptions({ name: 'HotZone' });

const props = defineProps<{ property: HotZoneProperty }>();
</script>

<template>
  <div class="min-h-30px relative h-full w-full">
    <Image
      :src="props.property.imgUrl"
      class="pointer-events-none h-full w-full select-none"
      :preview="false"
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
        color: 'hsl(var(--primary))',
        background: 'color-mix(in srgb, hsl(var(--primary)) 30%, transparent)',
        borderColor: 'hsl(var(--primary))',
      }"
    >
      {{ item.name }}
    </div>
  </div>
</template>
