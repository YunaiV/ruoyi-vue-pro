<script setup lang="ts">
import type { FloatingActionButtonProperty } from './config';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElButton, ElImage } from 'element-plus';

/** 悬浮按钮 */
defineOptions({ name: 'FloatingActionButton' });

/** 定义属性 */
defineProps<{ property: FloatingActionButtonProperty }>();

const expanded = ref(false); // 是否展开

/** 处理展开/折叠 */
function handleToggleFab() {
  expanded.value = !expanded.value;
}
</script>
<template>
  <div
    class="absolute bottom-8 right-[calc(50%-375px/2+32px)] z-20 flex items-center gap-3"
    :class="[
      {
        'flex-row': property.direction === 'horizontal',
        'flex-col': property.direction === 'vertical',
      },
    ]"
  >
    <template v-if="expanded">
      <div
        v-for="(item, index) in property.list"
        :key="index"
        class="flex flex-col items-center"
      >
        <ElImage :src="item.imgUrl" fit="contain" class="h-7 w-7">
          <template #error>
            <div class="flex h-full w-full items-center justify-center">
              <IconifyIcon icon="lucide:image" :color="item.textColor" />
            </div>
          </template>
        </ElImage>
        <span
          v-if="property.showText"
          class="mt-1 text-xs"
          :style="{ color: item.textColor }"
        >
          {{ item.text }}
        </span>
      </div>
    </template>
    <!-- todo: @owen 使用APP主题色 -->
    <ElButton type="primary" size="large" circle @click="handleToggleFab">
      <IconifyIcon
        icon="lucide:plus"
        class="transition-transform duration-300"
        :class="expanded ? 'rotate-[135deg]' : 'rotate-0'"
      />
    </ElButton>
  </div>
  <!-- 模态背景：展开时显示，点击后折叠 -->
  <div
    v-if="expanded"
    class="absolute left-[calc(50%-375px/2)] top-0 z-[11] h-full w-[375px] bg-black/40"
    @click="handleToggleFab"
  ></div>
</template>
