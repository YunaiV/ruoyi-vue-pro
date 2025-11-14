<script setup lang="ts">
import type { ImageBarProperty } from './config';

import { useVModel } from '@vueuse/core';
import { ElForm, ElFormItem } from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 图片展示属性面板
defineOptions({ name: 'ImageBarProperty' });

const props = defineProps<{ modelValue: ImageBarProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm label-width="80px" :model="formData">
      <ElFormItem label="上传图片" prop="imgUrl">
        <UploadImg
          v-model="formData.imgUrl"
          draggable="false"
          height="80px"
          width="100%"
          class="min-w-[80px]"
          :show-description="false"
        >
          <template #tip> 建议宽度750 </template>
        </UploadImg>
      </ElFormItem>
      <ElFormItem label="链接" prop="url">
        <AppLinkInput v-model="formData.url" />
      </ElFormItem>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
