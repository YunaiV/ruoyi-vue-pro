<script setup lang="ts">
import type { ImageBarProperty } from './config';

import { useVModel } from '@vueuse/core';
import { ElForm, ElFormItem } from 'element-plus';

import { ImageUpload } from '#/components/upload/';
import { AppLinkInput } from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';

/** 图片展示属性面板 */
defineOptions({ name: 'ImageBarProperty' });

const props = defineProps<{ modelValue: ImageBarProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm label-width="80px" :model="formData">
      <ElFormItem label="上传图片" prop="imgUrl">
        <ImageUpload
          v-model="formData.imgUrl"
          draggable="false"
          height="80px"
          width="100%"
          class="min-w-[80px]"
          :show-description="false"
        >
          <template #tip> 建议宽度750 </template>
        </ImageUpload>
      </ElFormItem>
      <ElFormItem label="链接" prop="url">
        <AppLinkInput v-model="formData.url" />
      </ElFormItem>
    </ElForm>
  </ComponentContainerProperty>
</template>
