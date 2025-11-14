<script setup lang="ts">
import type { MenuSwiperItemProperty, MenuSwiperProperty } from './config';

import { ref, watch } from 'vue';

import { ElCarousel, ElCarouselItem, ElImage } from 'element-plus';

/** 菜单导航 */
defineOptions({ name: 'MenuSwiper' });
const props = defineProps<{ property: MenuSwiperProperty }>();
// 标题的高度
const TITLE_HEIGHT = 20;
// 图标的高度
const ICON_SIZE = 32;
// 垂直间距：一行上下的间距
const SPACE_Y = 16;

// 分页
const pages = ref<MenuSwiperItemProperty[][]>([]);
// 轮播图高度
const carouselHeight = ref(0);
// 行高
const rowHeight = ref(0);
// 列宽
const columnWidth = ref('');
watch(
  () => props.property,
  () => {
    // 计算列宽：每一列的百分比
    columnWidth.value = `${100 * (1 / props.property.column)}%`;
    // 计算行高：图标 + 文字（仅显示图片时为0） + 垂直间距 * 2
    rowHeight.value =
      (props.property.layout === 'iconText'
        ? ICON_SIZE + TITLE_HEIGHT
        : ICON_SIZE) +
      SPACE_Y * 2;
    // 计算轮播的高度：行数 * 行高
    carouselHeight.value = props.property.row * rowHeight.value;

    // 每页数量：行数 * 列数
    const pageSize = props.property.row * props.property.column;
    // 清空分页
    pages.value = [];
    // 每一页的菜单
    let pageItems: MenuSwiperItemProperty[] = [];
    for (const item of props.property.list) {
      // 本页满员，新建下一页
      if (pageItems.length === pageSize) {
        pageItems = [];
      }
      // 增加一页
      if (pageItems.length === 0) {
        pages.value.push(pageItems);
      }
      // 本页增加一个
      pageItems.push(item);
    }
  },
  { immediate: true, deep: true },
);
</script>

<template>
  <ElCarousel
    :height="`${carouselHeight}px`"
    :autoplay="false"
    arrow="hover"
    indicator-position="outside"
  >
    <ElCarouselItem v-for="(page, pageIndex) in pages" :key="pageIndex">
      <div class="flex flex-row flex-wrap">
        <div
          v-for="(item, index) in page"
          :key="index"
          class="relative flex flex-col items-center justify-center"
          :style="{ width: columnWidth, height: `${rowHeight}px` }"
        >
          <!-- 图标 + 角标 -->
          <div class="relative" :class="`h-${ICON_SIZE}px w-${ICON_SIZE}px`">
            <!-- 右上角角标 -->
            <span
              v-if="item.badge?.show"
              class="absolute -right-2.5 -top-2.5 z-10 h-5 rounded-[10px] px-1.5 text-center text-xs leading-5"
              :style="{
                color: item.badge.textColor,
                backgroundColor: item.badge.bgColor,
              }"
            >
              {{ item.badge.text }}
            </span>
            <ElImage
              v-if="item.iconUrl"
              :src="item.iconUrl"
              class="h-full w-full"
            />
          </div>
          <!-- 标题 -->
          <span
            v-if="property.layout === 'iconText'"
            class="text-xs"
            :style="{
              color: item.titleColor,
              height: `${TITLE_HEIGHT}px`,
              lineHeight: `${TITLE_HEIGHT}px`,
            }"
          >
            {{ item.title }}
          </span>
        </div>
      </div>
    </ElCarouselItem>
  </ElCarousel>
</template>

<style lang="scss">
// 重写指示器样式，与 APP 保持一致
:root {
  .el-carousel__indicator {
    padding-top: 0;
    padding-bottom: 0;

    .el-carousel__button {
      --el-carousel-indicator-height: 6px;
      --el-carousel-indicator-width: 6px;
      --el-carousel-indicator-out-color: #ff6000;

      border-radius: 6px;
    }
  }

  .el-carousel__indicator.is-active {
    .el-carousel__button {
      --el-carousel-indicator-width: 12px;
    }
  }
}
</style>
