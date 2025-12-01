<script setup lang="ts">
import type { NavigationBarProperty } from './config';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElCheckbox,
  ElForm,
  ElFormItem,
  ElRadio,
  ElRadioGroup,
  ElTooltip,
} from 'element-plus';

import UploadImg from '#/components/upload/image-upload.vue';
import { ColorInput } from '#/views/mall/promotion/components';

import NavigationBarCellProperty from './components/cell-property.vue';

/** 导航栏属性面板 */
defineOptions({ name: 'NavigationBarProperty' });

const props = defineProps<{ modelValue: NavigationBarProperty }>();

const emit = defineEmits(['update:modelValue']);

const rules = {
  name: [{ required: true, message: '请输入页面名称', trigger: 'blur' }],
}; // 表单校验

const formData = useVModel(props, 'modelValue', emit);
if (!formData.value._local) {
  formData.value._local = { previewMp: true, previewOther: false };
}
</script>

<template>
  <ElForm label-width="80px" :model="formData" :rules="rules">
    <ElFormItem label="样式" prop="styleType">
      <ElRadioGroup v-model="formData!.styleType">
        <ElRadio value="normal">标准</ElRadio>
        <ElTooltip
          content="沉侵式头部仅支持微信小程序、APP，建议页面第一个组件为图片展示类组件"
          placement="top"
        >
          <ElRadio value="inner">沉浸式</ElRadio>
        </ElTooltip>
      </ElRadioGroup>
    </ElFormItem>
    <ElFormItem
      label="常驻显示"
      prop="alwaysShow"
      v-if="formData.styleType === 'inner'"
    >
      <ElRadioGroup v-model="formData!.alwaysShow">
        <ElRadio :value="false">关闭</ElRadio>
        <ElTooltip
          content="常驻显示关闭后,头部小组件将在页面滑动时淡入"
          placement="top"
        >
          <ElRadio :value="true">开启</ElRadio>
        </ElTooltip>
      </ElRadioGroup>
    </ElFormItem>
    <ElFormItem label="背景类型" prop="bgType">
      <ElRadioGroup v-model="formData.bgType">
        <ElRadio value="color">纯色</ElRadio>
        <ElRadio value="img">图片</ElRadio>
      </ElRadioGroup>
    </ElFormItem>
    <ElFormItem
      label="背景颜色"
      prop="bgColor"
      v-if="formData.bgType === 'color'"
    >
      <ColorInput v-model="formData.bgColor" />
    </ElFormItem>
    <ElFormItem label="背景图片" prop="bgImg" v-else>
      <div class="flex items-center">
        <UploadImg
          v-model="formData.bgImg"
          :limit="1"
          width="56px"
          height="56px"
          :show-description="false"
        />
        <span class="mb-2 ml-2 text-xs text-gray-400">建议宽度：750</span>
      </div>
    </ElFormItem>
    <ElCard class="property-group" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span>内容（小程序）</span>
          <ElFormItem prop="_local.previewMp" class="mb-0">
            <ElCheckbox
              v-model="formData._local.previewMp"
              @change="
                formData._local.previewOther = !formData._local.previewMp
              "
            >
              预览
            </ElCheckbox>
          </ElFormItem>
        </div>
      </template>
      <NavigationBarCellProperty v-model="formData.mpCells" is-mp />
    </ElCard>
    <ElCard class="property-group" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span>内容（非小程序）</span>
          <ElFormItem prop="_local.previewOther" class="mb-0">
            <ElCheckbox
              v-model="formData._local.previewOther"
              @change="
                formData._local.previewMp = !formData._local.previewOther
              "
            >
              预览
            </ElCheckbox>
          </ElFormItem>
        </div>
      </template>
      <NavigationBarCellProperty v-model="formData.otherCells" :is-mp="false" />
    </ElCard>
  </ElForm>
</template>
