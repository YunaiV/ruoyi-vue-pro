<script setup lang="ts">
import type { DividerProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  Form,
  FormItem,
  RadioButton,
  RadioGroup,
  Slider,
  Tooltip,
} from 'ant-design-vue';

import { ColorInput } from '#/views/mall/promotion/components';

/** 导航栏属性面板 */
defineOptions({ name: 'DividerProperty' });

const props = defineProps<{ modelValue: DividerProperty }>();

const emit = defineEmits(['update:modelValue']);

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
]; // 线类型
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <Form :model="formData">
    <FormItem label="高度" name="height">
      <Slider v-model:value="formData.height" :min="1" :max="100" />
    </FormItem>
    <FormItem label="选择样式" name="borderType">
      <RadioGroup v-model:value="formData!.borderType">
        <Tooltip
          placement="top"
          v-for="(item, index) in BORDER_TYPES"
          :key="index"
          :title="item.text"
        >
          <RadioButton :value="item.type">
            <IconifyIcon
              :icon="item.icon"
              class="inset-0 size-6 items-center"
            />
          </RadioButton>
        </Tooltip>
      </RadioGroup>
    </FormItem>
    <template v-if="formData.borderType !== 'none'">
      <FormItem label="线宽" name="lineWidth">
        <Slider v-model:value="formData.lineWidth" :min="1" :max="30" />
      </FormItem>
      <FormItem label="左右边距" name="paddingType">
        <RadioGroup v-model:value="formData!.paddingType">
          <Tooltip title="无边距" placement="top">
            <RadioButton value="none">
              <IconifyIcon
                icon="tabler:box-padding"
                class="inset-0 size-6 items-center"
              />
            </RadioButton>
          </Tooltip>
          <Tooltip title="左右留边" placement="top">
            <RadioButton value="horizontal">
              <IconifyIcon
                icon="vaadin:padding"
                class="inset-0 size-6 items-center"
              />
            </RadioButton>
          </Tooltip>
        </RadioGroup>
      </FormItem>
      <FormItem label="颜色">
        <ColorInput v-model="formData.lineColor" />
      </FormItem>
    </template>
  </Form>
</template>
