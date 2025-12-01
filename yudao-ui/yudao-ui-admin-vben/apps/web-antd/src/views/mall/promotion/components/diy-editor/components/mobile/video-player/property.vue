<script setup lang="ts">
import type { VideoPlayerProperty } from './config';

import { useVModel } from '@vueuse/core';
import { Form, FormItem, Slider, Switch } from 'ant-design-vue';

import UploadFile from '#/components/upload/file-upload.vue';
import UploadImg from '#/components/upload/image-upload.vue';

import ComponentContainerProperty from '../../component-container-property.vue';

/** 视频播放属性面板 */
defineOptions({ name: 'VideoPlayerProperty' });

const props = defineProps<{ modelValue: VideoPlayerProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <template #style>
      <FormItem label="高度" name="height">
        <Slider v-model:value="formData.style.height" :max="500" :min="100" />
      </FormItem>
    </template>
    <Form
      :model="formData"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <FormItem label="上传视频" name="videoUrl">
        <UploadFile
          v-model="formData.videoUrl"
          :file-type="['mp4']"
          :limit="1"
          :file-size="100"
          class="min-w-[80px]"
        />
      </FormItem>
      <FormItem label="上传封面" name="posterUrl">
        <UploadImg
          v-model="formData.posterUrl"
          draggable="false"
          height="80px"
          width="100%"
          class="min-w-[80px]"
          :show-description="false"
        >
          <!-- TODO @芋艿：这里不提示；是不是组件得封装下；-->
          <template #tip> 建议宽度750 </template>
        </UploadImg>
      </FormItem>
      <FormItem label="自动播放" name="autoplay">
        <Switch v-model:checked="formData.autoplay" />
      </FormItem>
    </Form>
  </ComponentContainerProperty>
</template>
