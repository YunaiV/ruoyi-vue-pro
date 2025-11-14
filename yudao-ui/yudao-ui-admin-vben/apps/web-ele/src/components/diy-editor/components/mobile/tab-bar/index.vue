<script setup lang="ts">
import type { TabBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { ElImage } from 'element-plus';

/** 页面底部导航栏 */
defineOptions({ name: 'TabBar' });

defineProps<{ property: TabBarProperty }>();
</script>
<template>
  <div class="tab-bar">
    <div
      class="tab-bar-bg"
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
        class="tab-bar-item"
      >
        <ElImage :src="index === 0 ? item.activeIconUrl : item.iconUrl">
          <template #error>
            <div class="flex h-full w-full items-center justify-center">
              <IconifyIcon icon="ep:picture" />
            </div>
          </template>
        </ElImage>
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
<style lang="scss" scoped>
.tab-bar {
  z-index: 2;
  width: 100%;

  .tab-bar-bg {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-around;
    padding: 8px 0;

    .tab-bar-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 100%;
      font-size: 12px;

      :deep(img),
      .el-icon {
        width: 26px;
        height: 26px;
        border-radius: 4px;
      }
    }
  }
}
</style>
