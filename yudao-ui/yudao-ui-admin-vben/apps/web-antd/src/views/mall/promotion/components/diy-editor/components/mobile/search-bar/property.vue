<script setup lang="ts">
import type { SearchProperty } from './config';

import { watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { isString } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import {
  Card,
  Form,
  FormItem,
  Input,
  RadioButton,
  RadioGroup,
  Slider,
  Switch,
  Tooltip,
} from 'ant-design-vue';

import { ColorInput, Draggable } from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';

/** 搜索框属性面板 */
defineOptions({ name: 'SearchProperty' });

const props = defineProps<{ modelValue: SearchProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);

/** 监听热词数组变化 */
watch(
  () => formData.value.hotKeywords,
  (newVal) => {
    // 找到非字符串项的索引
    const nonStringIndex = newVal.findIndex((item) => !isString(item));
    if (nonStringIndex !== -1) {
      formData.value.hotKeywords[nonStringIndex] = '';
    }
  },
  { deep: true, flush: 'post' },
);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <Form :model="formData" :label-col="{ style: { width: '80px' } }">
      <Card title="搜索热词" class="property-group">
        <Draggable
          v-model="formData.hotKeywords"
          :empty-item="{
            type: 'input',
            placeholder: '请输入热词',
          }"
        >
          <template #default="{ index }">
            <Input
              v-model:value="formData.hotKeywords[index]"
              placeholder="请输入热词"
            />
          </template>
        </Draggable>
      </Card>
      <Card title="搜索样式" class="property-group">
        <FormItem label="框体样式">
          <RadioGroup v-model:value="formData!.borderRadius">
            <Tooltip title="方形" placement="top">
              <RadioButton :value="0">
                <IconifyIcon icon="tabler:input-search" class="size-6" />
              </RadioButton>
            </Tooltip>
            <Tooltip title="圆形" placement="top">
              <RadioButton :value="10">
                <IconifyIcon icon="iconoir:input-search" class="size-6" />
              </RadioButton>
            </Tooltip>
          </RadioGroup>
        </FormItem>
        <FormItem label="提示文字" name="placeholder">
          <Input v-model:value="formData.placeholder" />
        </FormItem>
        <FormItem label="文本位置" name="placeholderPosition">
          <RadioGroup v-model:value="formData!.placeholderPosition">
            <Tooltip title="居左" placement="top">
              <RadioButton value="left">
                <IconifyIcon
                  icon="ant-design:align-left-outlined"
                  class="size-6"
                />
              </RadioButton>
            </Tooltip>
            <Tooltip title="居中" placement="top">
              <RadioButton value="center">
                <IconifyIcon
                  icon="ant-design:align-center-outlined"
                  class="size-6"
                />
              </RadioButton>
            </Tooltip>
          </RadioGroup>
        </FormItem>
        <FormItem label="扫一扫" name="showScan">
          <Switch v-model:checked="formData!.showScan" />
        </FormItem>
        <FormItem label="框体高度" name="height">
          <Slider v-model:value="formData!.height" :max="50" :min="28" />
        </FormItem>
        <FormItem label="框体颜色" name="backgroundColor">
          <ColorInput v-model="formData.backgroundColor" />
        </FormItem>
        <FormItem label="文本颜色" name="textColor">
          <ColorInput v-model="formData.textColor" />
        </FormItem>
      </Card>
    </Form>
  </ComponentContainerProperty>
</template>
