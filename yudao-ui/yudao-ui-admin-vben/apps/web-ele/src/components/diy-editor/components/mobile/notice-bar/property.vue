<script setup lang="ts">
import type { NoticeBarProperty } from './config';

import { useVModel } from '@vueuse/core';
import { ElCard, ElForm, ElFormItem, ElInput } from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import ColorInput from '#/components/color-input/index.vue';
import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import Draggable from '#/components/draggable/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 通知栏属性面板
defineOptions({ name: 'NoticeBarProperty' });
const props = defineProps<{ modelValue: NoticeBarProperty }>();

const emit = defineEmits(['update:modelValue']);

// 表单校验
const rules = {
  content: [{ required: true, message: '请输入公告', trigger: 'blur' }],
};

const formData = useVModel(props, 'modelValue', emit);
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

<style scoped lang="scss"></style>
