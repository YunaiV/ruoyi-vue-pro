<script setup lang="ts">
import type { PopoverProperty } from './config';

import { useVModel } from '@vueuse/core';
import {
  ElForm,
  ElFormItem,
  ElRadio,
  ElRadioGroup,
  ElTooltip,
} from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import Draggable from '#/components/draggable/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 弹窗广告属性面板
defineOptions({ name: 'PopoverProperty' });

const props = defineProps<{ modelValue: PopoverProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ElForm label-width="80px" :model="formData">
    <Draggable v-model="formData.list" :empty-item="{ showType: 'once' }">
      <template #default="{ element, index }">
        <ElFormItem label="图片" :prop="`list[${index}].imgUrl`">
          <UploadImg
            v-model="element.imgUrl"
            height="56px"
            width="56px"
            :show-description="false"
          />
        </ElFormItem>
        <ElFormItem label="跳转链接" :prop="`list[${index}].url`">
          <AppLinkInput v-model="element.url" />
        </ElFormItem>
        <ElFormItem label="显示次数" :prop="`list[${index}].showType`">
          <ElRadioGroup v-model="element.showType">
            <ElTooltip
              content="只显示一次，下次打开时不显示"
              placement="bottom"
            >
              <ElRadio value="once">一次</ElRadio>
            </ElTooltip>
            <ElTooltip content="每次打开时都会显示" placement="bottom">
              <ElRadio value="always">不限</ElRadio>
            </ElTooltip>
          </ElRadioGroup>
        </ElFormItem>
      </template>
    </Draggable>
  </ElForm>
</template>

<style scoped lang="scss"></style>
