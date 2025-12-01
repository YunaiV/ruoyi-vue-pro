<script lang="ts" setup>
import type { NavigationBarCellProperty } from '../config';

import { computed, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  ElFormItem,
  ElInput,
  ElRadio,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElSwitch,
  ElTooltip,
} from 'element-plus';

import appNavBarMp from '#/assets/imgs/diy/app-nav-bar-mp.png';
import UploadImg from '#/components/upload/image-upload.vue';
import {
  AppLinkInput,
  ColorInput,
  MagicCubeEditor,
} from '#/views/mall/promotion/components';

/** 导航栏单元格属性面板 */
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

/**
 * 计算单元格数量
 * 1. 小程序：6 个（因为右侧有胶囊按钮占据 2 个格子的空间）
 * 2. 其它平台：8 个（全部空间可用）
 */
const cellCount = computed(() => (props.isMp ? 6 : 8));

const selectedHotAreaIndex = ref(0); // 选中的热区

/** 处理热区被选中事件 */
function handleHotAreaSelected(
  cellValue: NavigationBarCellProperty,
  index: number,
) {
  selectedHotAreaIndex.value = index;
  // 默认设置为选中文字，并设置属性
  if (!cellValue.type) {
    cellValue.type = 'text';
    cellValue.textColor = '#111111';
  }
  // 如果点击的是搜索框，则初始化搜索框的属性
  if (cellValue.type === 'search') {
    cellValue.placeholderPosition = 'left';
    cellValue.backgroundColor = '#EEEEEE';
    cellValue.textColor = '#969799';
  }
}
</script>

<template>
  <div class="h-40px flex items-center justify-center">
    <MagicCubeEditor
      v-model="cellList"
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
        <ElRadioGroup
          v-model="cell.type"
          @change="handleHotAreaSelected(cell, cellIndex)"
        >
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
        <ElFormItem label="框体颜色" prop="backgroundColor">
          <ColorInput v-model="cell.backgroundColor" />
        </ElFormItem>
        <ElFormItem class="lef" label="文本颜色" prop="textColor">
          <ColorInput v-model="cell.textColor" />
        </ElFormItem>
        <ElFormItem :prop="`cell[${cellIndex}].placeholder`" label="提示文字">
          <ElInput v-model="cell.placeholder" maxlength="10" show-word-limit />
        </ElFormItem>
        <ElFormItem label="文本位置" prop="placeholderPosition">
          <ElRadioGroup v-model="cell!.placeholderPosition">
            <ElTooltip content="居左" placement="top">
              <ElRadioButton value="left">
                <IconifyIcon icon="ant-design:align-left-outlined" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip content="居中" placement="top">
              <ElRadioButton value="center">
                <IconifyIcon icon="ant-design:align-center-outlined" />
              </ElRadioButton>
            </ElTooltip>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="扫一扫" prop="showScan">
          <ElSwitch v-model="cell!.showScan" />
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
