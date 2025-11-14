<script setup lang="ts">
import type { MenuSwiperProperty } from './config';

import { cloneDeep } from '@vben/utils';

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
import ColorInput from '#/components/color-input/index.vue';
import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import Draggable from '#/components/draggable/index.vue';
import InputWithColor from '#/components/input-with-color/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

import { EMPTY_MENU_SWIPER_ITEM_PROPERTY } from './config';

/** 菜单导航属性面板 */
defineOptions({ name: 'MenuSwiperProperty' });

const props = defineProps<{ modelValue: MenuSwiperProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <!-- 表单 -->
    <ElForm label-width="80px" :model="formData" class="mt-2">
      <ElFormItem label="布局" prop="layout">
        <ElRadioGroup v-model="formData.layout">
          <ElRadio value="iconText">图标+文字</ElRadio>
          <ElRadio value="icon">仅图标</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="行数" prop="row">
        <ElRadioGroup v-model="formData.row">
          <ElRadio :value="1">1行</ElRadio>
          <ElRadio :value="2">2行</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="列数" prop="column">
        <ElRadioGroup v-model="formData.column">
          <ElRadio :value="3">3列</ElRadio>
          <ElRadio :value="4">4列</ElRadio>
          <ElRadio :value="5">5列</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElCard header="菜单设置" class="property-group" shadow="never">
        <Draggable
          v-model="formData.list"
          :empty-item="cloneDeep(EMPTY_MENU_SWIPER_ITEM_PROPERTY)"
        >
          <template #default="{ element }">
            <ElFormItem label="图标" prop="iconUrl">
              <UploadImg
                v-model="element.iconUrl"
                height="80px"
                width="80px"
                :show-description="false"
              >
                <template #tip> 建议尺寸：98 * 98 </template>
              </UploadImg>
            </ElFormItem>
            <ElFormItem label="标题" prop="title">
              <InputWithColor
                v-model="element.title"
                v-model:color="element.titleColor"
              />
            </ElFormItem>
            <ElFormItem label="链接" prop="url">
              <AppLinkInput v-model="element.url" />
            </ElFormItem>
            <ElFormItem label="显示角标" prop="badge.show">
              <ElSwitch v-model="element.badge.show" />
            </ElFormItem>
            <template v-if="element.badge.show">
              <ElFormItem label="角标内容" prop="badge.text">
                <InputWithColor
                  v-model="element.badge.text"
                  v-model:color="element.badge.textColor"
                />
              </ElFormItem>
              <ElFormItem label="背景颜色" prop="badge.bgColor">
                <ColorInput v-model="element.badge.bgColor" />
              </ElFormItem>
            </template>
          </template>
        </Draggable>
      </ElCard>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
