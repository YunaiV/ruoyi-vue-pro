<script setup lang="ts">
import type { FloatingActionButtonProperty } from './config';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElForm,
  ElFormItem,
  ElRadio,
  ElRadioGroup,
  ElSwitch,
} from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import Draggable from '#/components/draggable/index.vue';
import InputWithColor from '#/components/input-with-color/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 悬浮按钮属性面板
defineOptions({ name: 'FloatingActionButtonProperty' });

const props = defineProps<{ modelValue: FloatingActionButtonProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ElForm label-width="80px" :model="formData">
    <ElCard header="按钮配置" class="property-group" shadow="never">
      <ElFormItem label="展开方向" prop="direction">
        <ElRadioGroup v-model="formData.direction">
          <ElRadio value="vertical">垂直</ElRadio>
          <ElRadio value="horizontal">水平</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="显示文字" prop="showText">
        <ElSwitch v-model="formData.showText" />
      </ElFormItem>
    </ElCard>
    <ElCard header="按钮列表" class="property-group" shadow="never">
      <Draggable v-model="formData.list" :empty-item="{ textColor: '#fff' }">
        <template #default="{ element, index }">
          <ElFormItem label="图标" :prop="`list[${index}].imgUrl`">
            <UploadImg
              v-model="element.imgUrl"
              height="56px"
              width="56px"
              :show-description="false"
            />
          </ElFormItem>
          <ElFormItem label="文字" :prop="`list[${index}].text`">
            <InputWithColor
              v-model="element.text"
              v-model:color="element.textColor"
            />
          </ElFormItem>
          <ElFormItem label="跳转链接" :prop="`list[${index}].url`">
            <AppLinkInput v-model="element.url" />
          </ElFormItem>
        </template>
      </Draggable>
    </ElCard>
  </ElForm>
</template>

<style scoped lang="scss"></style>
