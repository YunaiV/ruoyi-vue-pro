<script setup lang="ts">
import type { CarouselProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElForm,
  ElFormItem,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElSwitch,
  ElText,
  ElTooltip,
} from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import Draggable from '#/components/draggable/index.vue';
import UploadFile from '#/components/upload/file-upload.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 轮播图属性面板
defineOptions({ name: 'CarouselProperty' });

const props = defineProps<{ modelValue: CarouselProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm label-width="80px" :model="formData">
      <ElCard header="样式设置" class="property-group" shadow="never">
        <ElFormItem label="样式" prop="type">
          <ElRadioGroup v-model="formData.type">
            <ElTooltip class="item" content="默认" placement="bottom">
              <ElRadioButton value="default">
                <IconifyIcon icon="system-uicons:carousel" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip class="item" content="卡片" placement="bottom">
              <ElRadioButton value="card">
                <IconifyIcon icon="ic:round-view-carousel" />
              </ElRadioButton>
            </ElTooltip>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="指示器" prop="indicator">
          <ElRadioGroup v-model="formData.indicator">
            <ElRadio value="dot">小圆点</ElRadio>
            <ElRadio value="number">数字</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="是否轮播" prop="autoplay">
          <ElSwitch v-model="formData.autoplay" />
        </ElFormItem>
        <ElFormItem label="播放间隔" prop="interval" v-if="formData.autoplay">
          <ElSlider
            v-model="formData.interval"
            :max="10"
            :min="0.5"
            :step="0.5"
            show-input
            input-size="small"
            :show-input-controls="false"
          />
          <ElText type="info">单位：秒</ElText>
        </ElFormItem>
      </ElCard>
      <ElCard header="内容设置" class="property-group" shadow="never">
        <Draggable v-model="formData.items" :empty-item="{ type: 'img' }">
          <template #default="{ element }">
            <ElFormItem
              label="类型"
              prop="type"
              class="mb-2"
              label-width="40px"
            >
              <ElRadioGroup v-model="element.type">
                <ElRadio value="img">图片</ElRadio>
                <ElRadio value="video">视频</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem
              label="图片"
              class="mb-2"
              label-width="40px"
              v-if="element.type === 'img'"
            >
              <UploadImg
                v-model="element.imgUrl"
                draggable="false"
                height="80px"
                width="100%"
                class="min-w-[80px]"
                :show-description="false"
              />
            </ElFormItem>
            <template v-else>
              <ElFormItem label="封面" class="mb-2" label-width="40px">
                <UploadImg
                  v-model="element.imgUrl"
                  draggable="false"
                  :show-description="false"
                  height="80px"
                  width="100%"
                  class="min-w-[80px]"
                />
              </ElFormItem>
              <ElFormItem label="视频" class="mb-2" label-width="40px">
                <UploadFile
                  v-model="element.videoUrl"
                  :file-type="['mp4']"
                  :limit="1"
                  :file-size="100"
                  class="min-w-[80px]"
                />
              </ElFormItem>
            </template>
            <ElFormItem label="链接" class="mb-2" label-width="40px">
              <AppLinkInput v-model="element.url" />
            </ElFormItem>
          </template>
        </Draggable>
      </ElCard>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
