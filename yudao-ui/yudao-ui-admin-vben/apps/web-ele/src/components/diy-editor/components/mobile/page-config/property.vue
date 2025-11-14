<script setup lang="ts">
import type { PageConfigProperty } from './config';

import { useVModel } from '@vueuse/core';
import { ElForm, ElFormItem, ElInput } from 'element-plus';

import ColorInput from '#/components/input-with-color/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 导航栏属性面板
defineOptions({ name: 'PageConfigProperty' });
const props = defineProps<{ modelValue: PageConfigProperty }>();

const emit = defineEmits(['update:modelValue']);

// 表单校验
const rules = {};

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ElForm label-width="80px" :model="formData" :rules="rules">
    <ElFormItem label="页面描述" prop="description">
      <ElInput
        type="textarea"
        v-model="formData!.description"
        placeholder="用户通过微信分享给朋友时，会自动显示页面描述"
      />
    </ElFormItem>
    <ElFormItem label="背景颜色" prop="backgroundColor">
      <ColorInput v-model="formData!.backgroundColor" />
    </ElFormItem>
    <ElFormItem label="背景图片" prop="backgroundImage">
      <UploadImg
        v-model="formData!.backgroundImage"
        :limit="1"
        :show-description="false"
      >
        <template #tip>建议宽度 750px</template>
      </UploadImg>
    </ElFormItem>
  </ElForm>
</template>

<style scoped lang="scss"></style>
