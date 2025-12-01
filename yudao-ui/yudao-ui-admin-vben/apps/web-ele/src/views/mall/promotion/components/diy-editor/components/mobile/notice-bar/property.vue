<script setup lang="ts">
import type { NoticeBarProperty } from './config';

import { useVModel } from '@vueuse/core';
import { ElCard, ElForm, ElFormItem, ElInput } from 'element-plus';

import UploadImg from '#/components/upload/image-upload.vue';
import {
  AppLinkInput,
  ColorInput,
  Draggable,
} from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';

/** 公告栏属性面板 */
defineOptions({ name: 'NoticeBarProperty' });

const props = defineProps<{ modelValue: NoticeBarProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
const rules = {
  content: [{ required: true, message: '请输入公告', trigger: 'blur' }],
}; // 表单校验
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm label-width="80px" :model="formData" :rules="rules">
      <ElFormItem label="公告图标" prop="iconUrl">
        <UploadImg
          v-model="formData.iconUrl"
          height="48px"
          :show-description="false"
        >
          <template #tip>建议尺寸：24 * 24</template>
        </UploadImg>
      </ElFormItem>
      <ElFormItem label="背景颜色" prop="backgroundColor">
        <ColorInput v-model="formData.backgroundColor" />
      </ElFormItem>
      <ElFormItem label="文字颜色" prop="文字颜色">
        <ColorInput v-model="formData.textColor" />
      </ElFormItem>
      <ElCard header="公告内容" class="property-group" shadow="never">
        <Draggable v-model="formData.contents">
          <template #default="{ element }">
            <ElFormItem label="公告" prop="text" label-width="40px">
              <ElInput v-model="element.text" placeholder="请输入公告" />
            </ElFormItem>
            <ElFormItem label="链接" prop="url" label-width="40px">
              <AppLinkInput v-model="element.url" />
            </ElFormItem>
          </template>
        </Draggable>
      </ElCard>
    </ElForm>
  </ComponentContainerProperty>
</template>
