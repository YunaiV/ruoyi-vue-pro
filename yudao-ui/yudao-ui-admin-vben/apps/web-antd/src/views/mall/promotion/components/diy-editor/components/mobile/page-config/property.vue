<script setup lang="ts">
import type { PageConfigProperty } from './config';

import { useVModel } from '@vueuse/core';
import { Form, FormItem, Textarea } from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import { ColorInput } from '#/views/mall/promotion/components';

/** 导航栏属性面板 */
defineOptions({ name: 'PageConfigProperty' });

const props = defineProps<{ modelValue: PageConfigProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <Form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
    <FormItem label="页面描述" name="description">
      <Textarea
        v-model:value="formData!.description"
        placeholder="用户通过微信分享给朋友时，会自动显示页面描述"
        :rows="3"
      />
    </FormItem>
    <FormItem label="背景颜色" name="backgroundColor">
      <ColorInput v-model="formData!.backgroundColor" />
    </FormItem>
    <FormItem label="背景图片" name="backgroundImage">
      <UploadImg
        v-model="formData!.backgroundImage"
        :limit="1"
        :show-description="false"
      >
        <!-- TODO @芋艿：这里不提示；是不是组件得封装下；-->
        <template #tip>建议宽度 750px</template>
      </UploadImg>
    </FormItem>
  </Form>
</template>
