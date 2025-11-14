<script setup lang="ts">
import type { DividerProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  ElForm,
  ElFormItem,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElTooltip,
} from 'element-plus';

import ColorInput from '#/components/input-with-color/index.vue';

// 导航栏属性面板
defineOptions({ name: 'DividerProperty' });
const props = defineProps<{ modelValue: DividerProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

// 线类型
const BORDER_TYPES = [
  {
    icon: 'vaadin:line-h',
    text: '实线',
    type: 'solid',
  },
  {
    icon: 'tabler:line-dashed',
    text: '虚线',
    type: 'dashed',
  },
  {
    icon: 'tabler:line-dotted',
    text: '点线',
    type: 'dotted',
  },
  {
    icon: 'entypo:progress-empty',
    text: '无',
    type: 'none',
  },
];
</script>

<template>
  <ElForm label-width="80px" :model="formData">
    <ElFormItem label="高度" prop="height">
      <ElSlider
        v-model="formData.height"
        :min="1"
        :max="100"
        show-input
        input-size="small"
      />
    </ElFormItem>
    <ElFormItem label="选择样式" prop="borderType">
      <ElRadioGroup v-model="formData!.borderType">
        <ElTooltip
          placement="top"
          v-for="(item, index) in BORDER_TYPES"
          :key="index"
          :content="item.text"
        >
          <ElRadioButton :value="item.type">
            <IconifyIcon :icon="item.icon" />
          </ElRadioButton>
        </ElTooltip>
      </ElRadioGroup>
    </ElFormItem>
    <template v-if="formData.borderType !== 'none'">
      <ElFormItem label="线宽" prop="lineWidth">
        <ElSlider
          v-model="formData.lineWidth"
          :min="1"
          :max="30"
          show-input
          input-size="small"
        />
      </ElFormItem>
      <ElFormItem label="左右边距" prop="paddingType">
        <ElRadioGroup v-model="formData!.paddingType">
          <ElTooltip content="无边距" placement="top">
            <ElRadioButton value="none">
              <IconifyIcon icon="tabler:box-padding" />
            </ElRadioButton>
          </ElTooltip>
          <ElTooltip content="左右留边" placement="top">
            <ElRadioButton value="horizontal">
              <IconifyIcon icon="vaadin:padding" />
            </ElRadioButton>
          </ElTooltip>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="颜色">
        <!-- 分割线颜色 -->
        <ColorInput v-model="formData.lineColor" />
      </ElFormItem>
    </template>
  </ElForm>
</template>

<style scoped lang="scss"></style>
