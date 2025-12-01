<script setup lang="ts">
import type { StyleValue } from 'vue';

import type { SearchProperty } from '../search-bar/config';
import type {
  NavigationBarCellProperty,
  NavigationBarProperty,
} from './config';

import { computed } from 'vue';

import appNavbarMp from '#/assets/imgs/diy/app-nav-bar-mp.png';

import SearchBar from '../search-bar/index.vue';

/** 页面顶部导航栏 */
defineOptions({ name: 'NavigationBar' });

const props = defineProps<{ property: NavigationBarProperty }>();

/** 计算背景样式 */
const bgStyle = computed(() => {
  const background =
    props.property.bgType === 'img' && props.property.bgImg
      ? `url(${props.property.bgImg}) no-repeat top center / 100% 100%`
      : props.property.bgColor;
  return { background };
});

/** 获取当前预览的单元格列表 */
const cellList = computed(() =>
  props.property._local?.previewMp
    ? props.property.mpCells
    : props.property.otherCells,
);

/** 计算单元格宽度 */
const cellWidth = computed(() => {
  return props.property._local?.previewMp
    ? (375 - 80 - 86) / 6
    : (375 - 90) / 8;
});

/** 获取单元格样式 */
function getCellStyle(cell: NavigationBarCellProperty) {
  return {
    width: `${cell.width * cellWidth.value + (cell.width - 1) * 10}px`,
    left: `${cell.left * cellWidth.value + (cell.left + 1) * 10}px`,
    position: 'absolute',
  } as StyleValue;
}

/** 获取搜索框属性配置 */
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
  <div
    class="flex h-[50px] items-center justify-between bg-white px-[6px]"
    :style="bgStyle"
  >
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
