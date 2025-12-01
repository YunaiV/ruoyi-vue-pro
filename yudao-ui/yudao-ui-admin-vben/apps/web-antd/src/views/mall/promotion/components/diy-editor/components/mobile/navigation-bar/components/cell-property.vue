<script lang="ts" setup>
import type { NavigationBarCellProperty } from '../config';

import { computed, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  FormItem,
  Input,
  Radio,
  RadioButton,
  RadioGroup,
  Slider,
  Switch,
  Tooltip,
} from 'ant-design-vue';

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
      <FormItem :name="`cell[${cellIndex}].type`" label="类型">
        <RadioGroup
          v-model:value="cell.type"
          @change="handleHotAreaSelected(cell, cellIndex)"
        >
          <Radio value="text">文字</Radio>
          <Radio value="image">图片</Radio>
          <Radio value="search">搜索框</Radio>
        </RadioGroup>
      </FormItem>
      <!-- 1. 文字 -->
      <template v-if="cell.type === 'text'">
        <FormItem :name="`cell[${cellIndex}].text`" label="内容">
          <Input v-model:value="cell!.text" :maxlength="10" show-count />
        </FormItem>
        <FormItem :name="`cell[${cellIndex}].text`" label="颜色">
          <ColorInput v-model="cell!.textColor" />
        </FormItem>
        <FormItem :name="`cell[${cellIndex}].url`" label="链接">
          <AppLinkInput v-model="cell.url" />
        </FormItem>
      </template>
      <!-- 2. 图片 -->
      <template v-else-if="cell.type === 'image'">
        <FormItem :name="`cell[${cellIndex}].imgUrl`" label="图片">
          <UploadImg
            v-model="cell.imgUrl"
            :limit="1"
            height="56px"
            width="56px"
            :show-description="false"
          />
          <span class="text-xs text-gray-500">建议尺寸 56*56</span>
        </FormItem>
        <FormItem :name="`cell[${cellIndex}].url`" label="链接">
          <AppLinkInput v-model="cell.url" />
        </FormItem>
      </template>
      <!-- 3. 搜索框 -->
      <template v-else>
        <FormItem label="框体颜色" name="backgroundColor">
          <ColorInput v-model="cell.backgroundColor" />
        </FormItem>
        <FormItem class="lef" label="文本颜色" name="textColor">
          <ColorInput v-model="cell.textColor" />
        </FormItem>
        <FormItem :name="`cell[${cellIndex}].placeholder`" label="提示文字">
          <Input v-model:value="cell.placeholder" :maxlength="10" show-count />
        </FormItem>
        <FormItem label="文本位置" name="placeholderPosition">
          <RadioGroup v-model:value="cell!.placeholderPosition">
            <Tooltip title="居左" placement="top">
              <RadioButton value="left">
                <IconifyIcon icon="ant-design:align-left-outlined" />
              </RadioButton>
            </Tooltip>
            <Tooltip title="居中" placement="top">
              <RadioButton value="center">
                <IconifyIcon icon="ant-design:align-center-outlined" />
              </RadioButton>
            </Tooltip>
          </RadioGroup>
        </FormItem>
        <FormItem label="扫一扫" name="showScan">
          <Switch v-model:checked="cell!.showScan" />
        </FormItem>
        <FormItem :name="`cell[${cellIndex}].borderRadius`" label="圆角">
          <Slider v-model:value="cell.borderRadius" :max="100" :min="0" />
        </FormItem>
      </template>
    </template>
  </template>
</template>
