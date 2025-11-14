<script lang="ts" setup>
import type { NavigationBarCellProperty } from '../config';

import type { Rect } from '#/components/magic-cube-editor/util';

import { computed, ref } from 'vue';

import { useVModel } from '@vueuse/core';
import {
  ElFormItem,
  ElInput,
  ElRadio,
  ElRadioGroup,
  ElSlider,
} from 'element-plus';

import appNavBarMp from '#/assets/imgs/diy/app-nav-bar-mp.png';
import AppLinkInput from '#/components/app-link-input/index.vue';
import ColorInput from '#/components/color-input/index.vue';
import MagicCubeEditor from '#/components/magic-cube-editor/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 导航栏属性面板
defineOptions({ name: 'NavigationBarCellProperty' });

const props = defineProps({
  isMp: {
    type: Boolean,
    default: true,
  },
  modelValue: {
    type: Array as () => NavigationBarCellProperty[],
    default: () => [],
  },
});
const emit = defineEmits(['update:modelValue']);
const cellList = useVModel(props, 'modelValue', emit);

// 单元格数量：小程序6个（右侧胶囊按钮占了2个），其它平台8个
const cellCount = computed(() => (props.isMp ? 6 : 8));

// 转换为Rect格式的数据
const rectList = computed<Rect[]>(() => {
  return cellList.value.map((cell) => ({
    left: cell.left,
    top: cell.top,
    width: cell.width,
    height: cell.height,
    right: cell.left + cell.width,
    bottom: cell.top + cell.height,
  }));
});

// 选中的热区
const selectedHotAreaIndex = ref(0);
const handleHotAreaSelected = (
  cellValue: NavigationBarCellProperty,
  index: number,
) => {
  selectedHotAreaIndex.value = index;
  if (!cellValue.type) {
    cellValue.type = 'text';
    cellValue.textColor = '#111111';
  }
};
</script>

<template>
  <div class="h-40px flex items-center justify-center">
    <MagicCubeEditor
      v-model="rectList"
      :cols="cellCount"
      :cube-size="38"
      :rows="1"
      class="m-b-16px"
      @hot-area-selected="handleHotAreaSelected"
    />
    <img
      v-if="isMp"
      alt=""
      style="width: 76px; height: 30px"
      :src="appNavBarMp"
    />
  </div>
  <template v-for="(cell, cellIndex) in cellList" :key="cellIndex">
    <template v-if="selectedHotAreaIndex === Number(cellIndex)">
      <ElFormItem :prop="`cell[${cellIndex}].type`" label="类型">
        <ElRadioGroup v-model="cell.type">
          <ElRadio value="text">文字</ElRadio>
          <ElRadio value="image">图片</ElRadio>
          <ElRadio value="search">搜索框</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <!-- 1. 文字 -->
      <template v-if="cell.type === 'text'">
        <ElFormItem :prop="`cell[${cellIndex}].text`" label="内容">
          <ElInput v-model="cell!.text" maxlength="10" show-word-limit />
        </ElFormItem>
        <ElFormItem :prop="`cell[${cellIndex}].text`" label="颜色">
          <ColorInput v-model="cell!.textColor" />
        </ElFormItem>
        <ElFormItem :prop="`cell[${cellIndex}].url`" label="链接">
          <AppLinkInput v-model="cell.url" />
        </ElFormItem>
      </template>
      <!-- 2. 图片 -->
      <template v-else-if="cell.type === 'image'">
        <ElFormItem :prop="`cell[${cellIndex}].imgUrl`" label="图片">
          <UploadImg
            v-model="cell.imgUrl"
            :limit="1"
            height="56px"
            width="56px"
            :show-description="false"
          >
            <template #tip>建议尺寸 56*56</template>
          </UploadImg>
        </ElFormItem>
        <ElFormItem :prop="`cell[${cellIndex}].url`" label="链接">
          <AppLinkInput v-model="cell.url" />
        </ElFormItem>
      </template>
      <!-- 3. 搜索框 -->
      <template v-else>
        <ElFormItem :prop="`cell[${cellIndex}].placeholder`" label="提示文字">
          <ElInput v-model="cell.placeholder" maxlength="10" show-word-limit />
        </ElFormItem>
        <ElFormItem :prop="`cell[${cellIndex}].borderRadius`" label="圆角">
          <ElSlider
            v-model="cell.borderRadius"
            :max="100"
            :min="0"
            :show-input-controls="false"
            input-size="small"
            show-input
          />
        </ElFormItem>
      </template>
    </template>
  </template>
</template>

<style lang="scss" scoped></style>
