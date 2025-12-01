<script setup lang="ts">
import type { NoticeBarProperty } from './config';

import { useVModel } from '@vueuse/core';
import { Card, Form, FormItem, Input } from 'ant-design-vue';

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
    <Form :model="formData" :rules="rules">
      <FormItem label="公告图标" name="iconUrl">
        <UploadImg
          v-model="formData.iconUrl"
          height="48px"
          :show-description="false"
        >
          <!-- TODO @芋艿：这里不提示；是不是组件得封装下；-->
          <template #tip>建议尺寸：24 * 24</template>
        </UploadImg>
      </FormItem>
      <FormItem label="背景颜色" name="backgroundColor">
        <ColorInput v-model="formData.backgroundColor" />
      </FormItem>
      <FormItem label="文字颜色" name="textColor">
        <ColorInput v-model="formData.textColor" />
      </FormItem>
      <Card title="公告内容" class="property-group">
        <Draggable v-model="formData.contents">
          <template #default="{ element }">
            <FormItem label="公告" name="text">
              <Input v-model:value="element.text" placeholder="请输入公告" />
            </FormItem>
            <FormItem label="链接" name="url">
              <AppLinkInput v-model="element.url" />
            </FormItem>
          </template>
        </Draggable>
      </Card>
    </Form>
  </ComponentContainerProperty>
</template>
