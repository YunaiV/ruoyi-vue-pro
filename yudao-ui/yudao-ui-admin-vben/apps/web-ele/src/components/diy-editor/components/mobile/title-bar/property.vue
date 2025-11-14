<script setup lang="ts">
import type { TitleBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElCheckbox,
  ElForm,
  ElFormItem,
  ElInput,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElTooltip,
} from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import InputWithColor from '#/components/input-with-color/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

// 导航栏属性面板
defineOptions({ name: 'TitleBarProperty' });

const props = defineProps<{ modelValue: TitleBarProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

// 表单校验
const rules = {};
</script>
<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm label-width="85px" :model="formData" :rules="rules">
      <ElCard header="风格" class="property-group" shadow="never">
        <ElFormItem label="背景图片" prop="bgImgUrl">
          <UploadImg
            v-model="formData.bgImgUrl"
            width="100%"
            height="40px"
            :show-description="false"
          >
            <template #tip>建议尺寸 750*80</template>
          </UploadImg>
        </ElFormItem>
        <ElFormItem label="标题位置" prop="textAlign">
          <ElRadioGroup v-model="formData!.textAlign">
            <ElTooltip content="居左" placement="top">
              <ElRadioButton value="left">
                <IconifyIcon icon="ant-design:align-left-outlined" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip content="居中" placement="top">
              <ElRadioButton value="center">
                <IconifyIcon icon="ant-design:align-center-outlined" />
              </ElRadioButton>
            </ElTooltip>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="偏移量" prop="marginLeft" label-width="70px">
          <ElSlider
            v-model="formData.marginLeft"
            :max="100"
            :min="0"
            show-input
            input-size="small"
          />
        </ElFormItem>
        <ElFormItem label="高度" prop="height" label-width="70px">
          <ElSlider
            v-model="formData.height"
            :max="200"
            :min="20"
            show-input
            input-size="small"
          />
        </ElFormItem>
      </ElCard>
      <ElCard header="主标题" class="property-group" shadow="never">
        <ElFormItem label="文字" prop="title" label-width="40px">
          <InputWithColor
            v-model="formData.title"
            v-model:color="formData.titleColor"
            show-word-limit
            maxlength="20"
          />
        </ElFormItem>
        <ElFormItem label="大小" prop="titleSize" label-width="40px">
          <ElSlider
            v-model="formData.titleSize"
            :max="60"
            :min="10"
            show-input
            input-size="small"
          />
        </ElFormItem>
        <ElFormItem label="粗细" prop="titleWeight" label-width="40px">
          <ElSlider
            v-model="formData.titleWeight"
            :min="100"
            :max="900"
            :step="100"
            show-input
            input-size="small"
          />
        </ElFormItem>
      </ElCard>
      <ElCard header="副标题" class="property-group" shadow="never">
        <ElFormItem label="文字" prop="description" label-width="40px">
          <InputWithColor
            v-model="formData.description"
            v-model:color="formData.descriptionColor"
            show-word-limit
            maxlength="50"
          />
        </ElFormItem>
        <ElFormItem label="大小" prop="descriptionSize" label-width="40px">
          <ElSlider
            v-model="formData.descriptionSize"
            :max="60"
            :min="10"
            show-input
            input-size="small"
          />
        </ElFormItem>
        <ElFormItem label="粗细" prop="descriptionWeight" label-width="40px">
          <ElSlider
            v-model="formData.descriptionWeight"
            :min="100"
            :max="900"
            :step="100"
            show-input
            input-size="small"
          />
        </ElFormItem>
      </ElCard>
      <ElCard header="查看更多" class="property-group" shadow="never">
        <ElFormItem label="是否显示" prop="more.show">
          <ElCheckbox v-model="formData.more.show" />
        </ElFormItem>
        <!-- 更多按钮的 样式选择 -->
        <template v-if="formData.more.show">
          <ElFormItem label="样式" prop="more.type">
            <ElRadioGroup v-model="formData.more.type">
              <ElRadio value="text">文字</ElRadio>
              <ElRadio value="icon">图标</ElRadio>
              <ElRadio value="all">文字+图标</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
          <ElFormItem
            label="更多文字"
            prop="more.text"
            v-show="formData.more.type !== 'icon'"
          >
            <ElInput v-model="formData.more.text" />
          </ElFormItem>
          <ElFormItem label="跳转链接" prop="more.url">
            <AppLinkInput v-model="formData.more.url" />
          </ElFormItem>
        </template>
      </ElCard>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
