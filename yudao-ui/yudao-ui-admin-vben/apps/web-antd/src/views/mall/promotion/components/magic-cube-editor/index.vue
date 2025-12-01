<script lang="ts" setup>
import type { Point, Rect } from './util';

import { ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { createRect, isContains, isOverlap } from './util';

/**
 * 魔方编辑器，有两部分组成：
 *  1. 魔方矩阵：位于底层，由方块组件的二维表格，用于创建热区
 *    操作方法：
 *       1.1 点击其中一个方块就会进入热区选择模式
 *       1.2 再次点击另外一个方块时，结束热区选择模式
 *       1.3 在两个方块中间的区域创建热区
 *    如果两次点击的都是同一方块，就只创建一个格子的热区
 *  2. 热区：位于顶层，采用绝对定位，覆盖在魔方矩阵上面。
 */
defineOptions({ name: 'MagicCubeEditor' });

/** 定义属性 */
const props = defineProps({
  modelValue: {
    type: Array as () => Rect[],
    default: () => [],
  }, // 热区列表
  rows: {
    type: Number,
    default: 4,
  }, // 行数，默认 4 行
  cols: {
    type: Number,
    default: 4,
  }, // 列数，默认 4 列
  cubeSize: {
    type: Number,
    default: 75,
  }, // 方块大小，单位px，默认75px
});

const emit = defineEmits(['update:modelValue', 'hotAreaSelected']); // 发送模型更新

/** 方块 */
type Cube = {
  active: boolean; // 是否激活
} & Point;

const cubes = ref<Cube[][]>([]); // 魔方矩阵：所有的方块

/** 监听行数、列数变化 */
watch(
  () => [props.rows, props.cols],
  () => {
    // 清空魔方
    cubes.value = [];
    if (!props.rows || !props.cols) return;

    // 初始化魔方
    for (let row = 0; row < props.rows; row++) {
      cubes.value[row] = [];
      for (let col = 0; col < props.cols; col++) {
        cubes.value[row]!.push({ x: col, y: row, active: false });
      }
    }
  },
  { immediate: true },
);

const hotAreas = ref<Rect[]>([]); // 热区列表

/** 初始化热区 */
watch(
  () => props.modelValue,
  () => (hotAreas.value = props.modelValue || []),
  { immediate: true },
);

const hotAreaBeginCube = ref<Cube>(); // 热区起始方块
const isHotAreaSelectMode = () => !!hotAreaBeginCube.value; // 是否开启了热区选择模式

/**
 * 处理鼠标点击方块
 *
 * @param currentRow 当前行号
 * @param currentCol 当前列号
 */
function handleCubeClick(currentRow: number, currentCol: number) {
  const currentCube = cubes.value[currentRow]?.[currentCol];
  if (!currentCube) {
    return;
  }

  // 情况 1：进入热区选择模式
  if (!isHotAreaSelectMode()) {
    hotAreaBeginCube.value = currentCube;
    hotAreaBeginCube.value!.active = true;
    return;
  }

  // 情况 2：结束热区选择模式
  hotAreas.value.push(createRect(hotAreaBeginCube.value!, currentCube));
  // 结束热区选择模式
  exitHotAreaSelectMode();
  // 创建后就选中热区
  const hotAreaIndex = hotAreas.value.length - 1;
  const hotArea = hotAreas.value[hotAreaIndex];
  if (hotArea) {
    handleHotAreaSelected(hotArea, hotAreaIndex);
  }
  // 发送热区变动通知
  emitUpdateModelValue();
}

/**
 * 处理鼠标经过方块
 *
 * @param currentRow 当前行号
 * @param currentCol 当前列号
 */
function handleCellHover(currentRow: number, currentCol: number) {
  // 当前没有进入热区选择模式
  if (!isHotAreaSelectMode()) {
    return;
  }

  // 当前已选的区域
  const currentCube = cubes.value[currentRow]?.[currentCol];
  if (!currentCube) {
    return;
  }

  const currentSelectedArea = createRect(hotAreaBeginCube.value!, currentCube);
  // 热区不允许重叠
  for (const hotArea of hotAreas.value) {
    // 检查是否重叠
    if (isOverlap(hotArea, currentSelectedArea)) {
      // 结束热区选择模式
      exitHotAreaSelectMode();
      return;
    }
  }

  // 激活选中区域内部的方块
  eachCube((_, __, cube) => {
    cube.active = isContains(currentSelectedArea, cube);
  });
}

/** 处理热区删除 */
function handleDeleteHotArea(index: number) {
  hotAreas.value.splice(index, 1);
  // 结束热区选择模式
  exitHotAreaSelectMode();
  // 发送热区变动通知
  emitUpdateModelValue();
}

const emitUpdateModelValue = () => emit('update:modelValue', hotAreas.value); // 发送热区变动通知

const selectedHotAreaIndex = ref(0); // 热区选中

/** 处理热区选中 */
function handleHotAreaSelected(hotArea: Rect, index: number) {
  selectedHotAreaIndex.value = index;
  emit('hotAreaSelected', hotArea, index);
}

/** 结束热区选择模式 */
function exitHotAreaSelectMode() {
  // 移除方块激活标记
  eachCube((_, __, cube) => {
    if (cube.active) {
      cube.active = false;
    }
  });

  // 清除起点
  hotAreaBeginCube.value = undefined;
}

/**
 * 迭代魔方矩阵
 * @param callback 回调
 */
function eachCube(callback: (x: number, y: number, cube: Cube) => void) {
  for (const [x, row] of cubes.value.entries()) {
    if (!row) continue;
    for (const [y, cube] of row.entries()) {
      if (cube) {
        callback(x, y, cube);
      }
    }
  }
}
</script>
<template>
  <table class="relative border-collapse border-spacing-0">
    <!-- 底层：魔方矩阵 -->
    <tbody>
      <tr v-for="(rowCubes, row) in cubes" :key="row">
        <td
          v-for="(cube, col) in rowCubes"
          :key="col"
          class="box-border cursor-pointer border text-center align-middle hover:bg-primary-100 active:bg-primary-200"
          :class="[{ active: cube.active }]"
          :style="{
            width: `${cubeSize}px`,
            height: `${cubeSize}px`,
          }"
          @click="handleCubeClick(row, col)"
          @mouseenter="handleCellHover(row, col)"
        >
          <IconifyIcon icon="lucide:plus" class="inline-block size-6" />
        </td>
      </tr>
    </tbody>
    <!-- 顶层：热区 -->
    <div
      v-for="(hotArea, index) in hotAreas"
      :key="index"
      class="absolute box-border flex items-center justify-center border border-primary bg-primary-200"
      :style="{
        top: `${cubeSize * hotArea.top}px`,
        left: `${cubeSize * hotArea.left}px`,
        height: `${cubeSize * hotArea.height}px`,
        width: `${cubeSize * hotArea.width}px`,
      }"
      @click="handleHotAreaSelected(hotArea, index)"
      @mouseover="exitHotAreaSelectMode"
    >
      <!-- 右上角热区删除按钮 -->
      <div
        v-if="selectedHotAreaIndex === index && hotArea.width && hotArea.height"
        class="absolute -right-2 -top-2 z-[1] size-6 h-4 w-4 items-center rounded-lg bg-card"
        @click="handleDeleteHotArea(index)"
      >
        <IconifyIcon
          icon="lucide:x"
          class="inset-0 items-center bg-primary text-white"
        />
      </div>
      <span v-if="hotArea.width">
        {{ `${hotArea.width}×${hotArea.height}` }}
      </span>
    </div>
  </table>
</template>
