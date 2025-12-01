<script setup lang="ts">
import type { MagicCubeProperty } from './config';

import { ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { Form, FormItem, Slider } from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import {
  AppLinkInput,
  MagicCubeEditor,
} from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';

/** 广告魔方属性面板 */
defineOptions({ name: 'MagicCubeProperty' });

const props = defineProps<{ modelValue: MagicCubeProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);

const selectedHotAreaIndex = ref(-1); // 选中的热区

/** 处理热区被选中事件 */
const handleHotAreaSelected = (_: any, index: number) => {
  selectedHotAreaIndex.value = index;
};
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <Form
      :model="formData"
      :label-col="{ style: { width: '80px' } }"
      label-align="right"
    >
      <p class="text-base font-bold">魔方设置：</p>
      <div class="flex flex-col gap-2 rounded-md p-4 shadow-lg">
        <p class="text-xs text-gray-500">每格尺寸 187 * 187</p>
        <MagicCubeEditor
          v-model="formData.list"
          :rows="4"
          :cols="4"
          @hot-area-selected="handleHotAreaSelected"
        />
        <template v-for="(hotArea, index) in formData.list" :key="index">
          <template v-if="selectedHotAreaIndex === index">
            <FormItem label="上传图片" :name="`list[${index}].imgUrl`">
              <UploadImg
                v-model="hotArea.imgUrl"
                height="80px"
                width="80px"
                :show-description="false"
              />
            </FormItem>
            <FormItem label="链接" :name="`list[${index}].url`">
              <AppLinkInput v-model="hotArea.url" />
            </FormItem>
          </template>
        </template>
      </div>
      <FormItem label="上圆角" name="borderRadiusTop">
        <Slider v-model:value="formData.borderRadiusTop" :max="100" :min="0" />
      </FormItem>
      <FormItem label="下圆角" name="borderRadiusBottom">
        <Slider
          v-model:value="formData.borderRadiusBottom"
          :max="100"
          :min="0"
        />
      </FormItem>
      <FormItem label="间隔" name="space">
        <Slider v-model:value="formData.space" :max="100" :min="0" />
      </FormItem>
    </Form>
  </ComponentContainerProperty>
</template>
