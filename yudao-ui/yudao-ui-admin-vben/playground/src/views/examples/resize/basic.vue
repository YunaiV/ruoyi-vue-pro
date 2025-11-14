<script lang="ts" setup>
import { ref } from 'vue';

import { Page, VResize } from '@vben/common-ui';

const colorMap = ['red', 'green', 'yellow', 'gray'];

type TSize = {
  height: number;
  left: number;
  top: number;
  width: number;
};

const sizeList = ref<TSize[]>([
  { height: 200, left: 200, top: 200, width: 200 },
  { height: 300, left: 300, top: 300, width: 300 },
  { height: 400, left: 400, top: 400, width: 400 },
  { height: 500, left: 500, top: 500, width: 500 },
]);

const resize = (size?: TSize, rect?: TSize) => {
  if (!size || !rect) return;

  size.height = rect.height;
  size.left = rect.left;
  size.top = rect.top;
  size.width = rect.width;
};
</script>

<template>
  <Page description="Resize组件基础示例" title="Resize组件">
    <div class="m-4 bg-blue-500 p-48 text-xl">
      <div v-for="size in sizeList" :key="size.width">
        {{
          `width: ${size.width}px, height: ${size.height}px, top: ${size.top}px, left: ${size.left}px`
        }}
      </div>
    </div>

    <template v-for="(_, idx) of 4" :key="idx">
      <VResize
        :h="100 * (idx + 1)"
        :w="100 * (idx + 1)"
        :x="100 * (idx + 1)"
        :y="100 * (idx + 1)"
        @dragging="(rect) => resize(sizeList[idx], rect)"
        @resizing="(rect) => resize(sizeList[idx], rect)"
      >
        <div
          :style="{ backgroundColor: colorMap[idx] }"
          class="h-full w-full"
        ></div>
      </VResize>
    </template>
  </Page>
</template>
