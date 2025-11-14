<script setup lang="ts">
import type { StyleValue } from 'vue';

import type {
  NavigationBarCellProperty,
  NavigationBarProperty,
} from './config';

import type { SearchProperty } from '#/components/diy-editor/components/mobile/search-bar/config';

import { computed } from 'vue';

import appNavbarMp from '#/assets/imgs/diy/app-nav-bar-mp.png';
import SearchBar from '#/components/diy-editor/components/mobile/search-bar/index.vue';

/** 页面顶部导航栏 */
defineOptions({ name: 'NavigationBar' });

const props = defineProps<{ property: NavigationBarProperty }>();

// 背景
const bgStyle = computed(() => {
  const background =
    props.property.bgType === 'img' && props.property.bgImg
      ? `url(${props.property.bgImg}) no-repeat top center / 100% 100%`
      : props.property.bgColor;
  return { background };
});
// 单元格列表
const cellList = computed(() =>
  props.property._local?.previewMp
    ? props.property.mpCells
    : props.property.otherCells,
);
// 单元格宽度
const cellWidth = computed(() => {
  return props.property._local?.previewMp
    ? (375 - 80 - 86) / 6
    : (375 - 90) / 8;
});
// 获得单元格样式
const getCellStyle = (cell: NavigationBarCellProperty) => {
  return {
    width: `${cell.width * cellWidth.value + (cell.width - 1) * 10}px`,
    left: `${cell.left * cellWidth.value + (cell.left + 1) * 10}px`,
    position: 'absolute',
  } as StyleValue;
};
// 获得搜索框属性
const getSearchProp = computed(() => (cell: NavigationBarCellProperty) => {
  return {
    height: 30,
    showScan: false,
    placeholder: cell.placeholder,
    borderRadius: cell.borderRadius,
  } as SearchProperty;
});
</script>
<template>
  <div class="navigation-bar" :style="bgStyle">
    <div class="flex h-full w-full items-center">
      <div
        v-for="(cell, cellIndex) in cellList"
        :key="cellIndex"
        :style="getCellStyle(cell)"
      >
        <span v-if="cell.type === 'text'">{{ cell.text }}</span>
        <img
          v-else-if="cell.type === 'image'"
          :src="cell.imgUrl"
          alt=""
          class="h-full w-full"
        />
        <SearchBar v-else :property="getSearchProp(cell)" />
      </div>
    </div>
    <img
      v-if="property._local?.previewMp"
      :src="appNavbarMp"
      alt=""
      style="width: 86px; height: 30px"
    />
  </div>
</template>
<style lang="scss" scoped>
.navigation-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 50px;
  padding: 0 6px;
  background: #fff;

  /* 左边 */
  .left {
    margin-left: 8px;
  }

  .center {
    flex: 1;
    font-size: 14px;
    line-height: 35px;
    color: #333;
    text-align: center;
  }

  /* 右边 */
  .right {
    margin-right: 8px;
  }
}
</style>
