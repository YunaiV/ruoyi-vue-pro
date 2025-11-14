<script setup lang="ts">
import type { FloatingActionButtonProperty } from './config';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElImage, ElMessage } from 'element-plus';

/** 悬浮按钮 */
defineOptions({ name: 'FloatingActionButton' });
// 定义属性
defineProps<{ property: FloatingActionButtonProperty }>();

// 是否展开
const expanded = ref(false);
// 处理展开/折叠
const handleToggleFab = () => {
  expanded.value = !expanded.value;
};

const handleActive = (index: number) => {
  ElMessage.success(`点击了${index}`);
};
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
        @click="handleActive(index)"
      >
        <ElImage :src="item.imgUrl" fit="contain" class="h-7 w-7">
          <template #error>
            <div class="flex h-full w-full items-center justify-center">
              <IconifyIcon icon="ep:picture" :color="item.textColor" />
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
    <el-button type="primary" size="large" circle @click="handleToggleFab">
      <IconifyIcon
        icon="ep:plus"
        class="fab-icon"
        :class="[{ active: expanded }]"
      />
    </el-button>
  </div>
  <!-- 模态背景：展开时显示，点击后折叠 -->
  <div v-if="expanded" class="modal-bg" @click="handleToggleFab"></div>
</template>

<style scoped lang="scss">
/* 模态背景 */
.modal-bg {
  position: absolute;
  top: 0;
  left: calc(50% - 375px / 2);
  z-index: 11;
  width: 375px;
  height: 100%;
  background-color: rgb(0 0 0 / 40%);
}

.fab-icon {
  transform: rotate(0deg);
  transition: transform 0.3s;

  &.active {
    transform: rotate(135deg);
  }
}
</style>
