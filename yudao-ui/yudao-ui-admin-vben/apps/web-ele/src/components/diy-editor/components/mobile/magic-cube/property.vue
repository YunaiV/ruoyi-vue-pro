<script setup lang="ts">
import type { MagicCubeProperty } from './config';

import { ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { ElForm, ElFormItem, ElSlider, ElText } from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import MagicCubeEditor from '#/components/magic-cube-editor/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

/** 广告魔方属性面板 */
defineOptions({ name: 'MagicCubeProperty' });

const props = defineProps<{ modelValue: MagicCubeProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

// 选中的热区
const selectedHotAreaIndex = ref(-1);
const handleHotAreaSelected = (_: any, index: number) => {
  selectedHotAreaIndex.value = index;
};
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <!-- 表单 -->
    <ElForm label-width="80px" :model="formData" class="mt-2">
      <ElText tag="p"> 魔方设置 </ElText>
      <ElText type="info" size="small"> 每格尺寸187 * 187 </ElText>
      <MagicCubeEditor
        class="my-4"
        v-model="formData.list"
        :rows="4"
        :cols="4"
        @hot-area-selected="handleHotAreaSelected"
      />
      <template v-for="(hotArea, index) in formData.list" :key="index">
        <template v-if="selectedHotAreaIndex === index">
          <ElFormItem label="上传图片" :prop="`list[${index}].imgUrl`">
            <UploadImg
              v-model="hotArea.imgUrl"
              height="80px"
              width="80px"
              :show-description="false"
            />
          </ElFormItem>
          <ElFormItem label="链接" :prop="`list[${index}].url`">
            <AppLinkInput v-model="hotArea.url" />
          </ElFormItem>
        </template>
      </template>
      <ElFormItem label="上圆角" prop="borderRadiusTop">
        <ElSlider
          v-model="formData.borderRadiusTop"
          :max="100"
          :min="0"
          show-input
          input-size="small"
          :show-input-controls="false"
        />
      </ElFormItem>
      <ElFormItem label="下圆角" prop="borderRadiusBottom">
        <ElSlider
          v-model="formData.borderRadiusBottom"
          :max="100"
          :min="0"
          show-input
          input-size="small"
          :show-input-controls="false"
        />
      </ElFormItem>
      <ElFormItem label="间隔" prop="space">
        <ElSlider
          v-model="formData.space"
          :max="100"
          :min="0"
          show-input
          input-size="small"
          :show-input-controls="false"
        />
      </ElFormItem>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
