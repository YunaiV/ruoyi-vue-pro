<script setup lang="ts">
import type { SearchProperty } from './config';

import { watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { isString } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElForm,
  ElFormItem,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElSwitch,
  ElTooltip,
} from 'element-plus';

import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import Draggable from '#/components/draggable/index.vue';

/** 搜索框属性面板 */
defineOptions({ name: 'SearchProperty' });

const props = defineProps<{ modelValue: SearchProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

// 监听热词数组变化
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
    <!-- 表单 -->
    <ElForm label-width="80px" :model="formData" class="mt-2">
      <ElCard header="搜索热词" class="property-group" shadow="never">
        <Draggable
          v-model="formData.hotKeywords"
          :empty-item="{
            type: 'input',
            placeholder: '请输入热词',
          }"
        >
          <template #default="{ index }">
            <el-input
              v-model="formData.hotKeywords[index]"
              placeholder="请输入热词"
            />
          </template>
        </Draggable>
      </ElCard>
      <ElCard header="搜索样式" class="property-group" shadow="never">
        <ElFormItem label="框体样式">
          <ElRadioGroup v-model="formData!.borderRadius">
            <ElTooltip content="方形" placement="top">
              <ElRadioButton :value="0">
                <IconifyIcon icon="tabler:input-search" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip content="圆形" placement="top">
              <ElRadioButton :value="10">
                <IconifyIcon icon="iconoir:input-search" />
              </ElRadioButton>
            </ElTooltip>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="提示文字" prop="placeholder">
          <el-input v-model="formData.placeholder" />
        </ElFormItem>
        <ElFormItem label="文本位置" prop="placeholderPosition">
          <ElRadioGroup v-model="formData!.placeholderPosition">
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
          <ElSwitch v-model="formData!.showScan" />
        </ElFormItem>
        <ElFormItem label="框体高度" prop="height">
          <ElSlider
            v-model="formData!.height"
            :max="50"
            :min="28"
            show-input
            input-size="small"
          />
        </ElFormItem>
        <ElFormItem label="框体颜色" prop="backgroundColor">
          <ColorInput v-model="formData.backgroundColor" />
        </ElFormItem>
        <ElFormItem class="lef" label="文本颜色" prop="textColor">
          <ColorInput v-model="formData.textColor" />
        </ElFormItem>
      </ElCard>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
