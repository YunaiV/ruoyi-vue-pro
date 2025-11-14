<script setup lang="ts">
import type { VideoPlayerProperty } from './config';

import { useVModel } from '@vueuse/core';
import { ElForm, ElFormItem, ElSlider, ElSwitch } from 'element-plus';

import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import UploadFile from '#/components/upload/file-upload.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 视频播放属性面板
defineOptions({ name: 'VideoPlayerProperty' });

const props = defineProps<{ modelValue: VideoPlayerProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <template #style>
      <ElFormItem label="高度" prop="height">
        <ElSlider
          v-model="formData.style.height"
          :max="500"
          :min="100"
          show-input
          input-size="small"
          :show-input-controls="false"
        />
      </ElFormItem>
    </template>
    <ElForm label-width="80px" :model="formData">
      <ElFormItem label="上传视频" prop="videoUrl">
        <UploadFile
          v-model="formData.videoUrl"
          :file-type="['mp4']"
          :limit="1"
          :file-size="100"
          class="min-w-[80px]"
        />
      </ElFormItem>
      <ElFormItem label="上传封面" prop="posterUrl">
        <UploadImg
          v-model="formData.posterUrl"
          draggable="false"
          height="80px"
          width="100%"
          class="min-w-[80px]"
          :show-description="false"
        >
          <template #tip> 建议宽度750 </template>
        </UploadImg>
      </ElFormItem>
      <ElFormItem label="自动播放" prop="autoplay">
        <ElSwitch v-model="formData.autoplay" />
      </ElFormItem>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
