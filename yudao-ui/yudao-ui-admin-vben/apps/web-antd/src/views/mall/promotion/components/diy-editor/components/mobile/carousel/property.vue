<script setup lang="ts">
import type { CarouselProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  Form,
  FormItem,
  InputNumber,
  Radio,
  RadioButton,
  RadioGroup,
  Slider,
  Switch,
  Tooltip,
} from 'ant-design-vue';

import UploadFile from '#/components/upload/file-upload.vue';
import UploadImg from '#/components/upload/image-upload.vue';
import { AppLinkInput, Draggable } from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';

/** 轮播图属性面板 */
defineOptions({ name: 'CarouselProperty' });

const props = defineProps<{ modelValue: CarouselProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <Form
      :model="formData"
      :label-col="{ style: { width: '80px' } }"
      label-align="right"
    >
      <p class="text-base font-bold">样式设置：</p>
      <div class="flex flex-col gap-2 rounded-md p-4 shadow-lg">
        <FormItem label="样式">
          <RadioGroup v-model:value="formData.type">
            <Tooltip class="item" title="默认" placement="bottom">
              <RadioButton value="default">
                <IconifyIcon icon="system-uicons:carousel" class="size-6" />
              </RadioButton>
            </Tooltip>
            <Tooltip class="item" title="卡片" placement="bottom">
              <RadioButton value="card">
                <IconifyIcon icon="ic:round-view-carousel" class="size-6" />
              </RadioButton>
            </Tooltip>
          </RadioGroup>
        </FormItem>
        <FormItem label="高度">
          <InputNumber
            v-model:value="formData.height"
            class="mr-[10px] !w-1/2"
          />
          px
        </FormItem>
        <FormItem label="指示器">
          <RadioGroup v-model:value="formData.indicator">
            <Radio value="dot">小圆点</Radio>
            <Radio value="number">数字</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="是否轮播">
          <Switch v-model:checked="formData.autoplay" />
        </FormItem>
        <FormItem label="播放间隔" v-if="formData.autoplay">
          <Slider
            v-model:value="formData.interval"
            :max="10"
            :min="0.5"
            :step="0.5"
          />
          <p class="text-info">单位：秒</p>
        </FormItem>
      </div>
      <p class="text-base font-bold">内容设置：</p>
      <div class="flex flex-col gap-2 rounded-md p-4 shadow-lg">
        <Draggable v-model="formData.items" :empty-item="{ type: 'img' }">
          <template #default="{ element }">
            <FormItem label="类型" name="type">
              <RadioGroup v-model:value="element.type">
                <Radio value="img">图片</Radio>
                <Radio value="video">视频</Radio>
              </RadioGroup>
            </FormItem>
            <FormItem label="图片" v-if="element.type === 'img'">
              <UploadImg
                v-model="element.imgUrl"
                draggable="false"
                height="80px"
                width="100%"
                class="min-w-20"
                :show-description="false"
              />
            </FormItem>
            <template v-else>
              <FormItem label="封面">
                <UploadImg
                  v-model="element.imgUrl"
                  draggable="false"
                  :show-description="false"
                  height="80px"
                  width="100%"
                  class="min-w-20"
                />
              </FormItem>
              <FormItem label="视频">
                <UploadFile
                  v-model="element.videoUrl"
                  :file-type="['mp4']"
                  :limit="1"
                  :file-size="100"
                  class="min-w-20"
                />
              </FormItem>
            </template>
            <FormItem label="链接">
              <AppLinkInput v-model="element.url" />
            </FormItem>
          </template>
        </Draggable>
      </div>
    </Form>
  </ComponentContainerProperty>
</template>
