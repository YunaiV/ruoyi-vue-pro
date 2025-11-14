<script setup lang="ts">
import type { TabBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElOption,
  ElRadioButton,
  ElRadioGroup,
  ElSelect,
  ElText,
} from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import ColorInput from '#/components/color-input/index.vue';
import Draggable from '#/components/draggable/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

import { component, THEME_LIST } from './config';
// 底部导航栏
defineOptions({ name: 'TabBarProperty' });

const props = defineProps<{ modelValue: TabBarProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

// 将数据库的值更新到右侧属性栏
component.property.items = formData.value.items;

// 要的主题
const handleThemeChange = () => {
  const theme = THEME_LIST.find((theme) => theme.id === formData.value.theme);
  if (theme?.color) {
    formData.value.style.activeColor = theme.color;
  }
};
</script>

<template>
  <div class="tab-bar">
    <!-- 表单 -->
    <ElForm :model="formData" label-width="80px">
      <ElFormItem label="主题" prop="theme">
        <ElSelect v-model="formData!.theme" @change="handleThemeChange">
          <ElOption
            v-for="(theme, index) in THEME_LIST"
            :key="index"
            :label="theme.name"
            :value="theme.id"
          >
            <template #default>
              <div class="flex items-center justify-between">
                <IconifyIcon :icon="theme.icon" :color="theme.color" />
                <span>{{ theme.name }}</span>
              </div>
            </template>
          </ElOption>
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="默认颜色">
        <ColorInput v-model="formData!.style.color" />
      </ElFormItem>
      <ElFormItem label="选中颜色">
        <ColorInput v-model="formData!.style.activeColor" />
      </ElFormItem>
      <ElFormItem label="导航背景">
        <ElRadioGroup v-model="formData!.style.bgType">
          <ElRadioButton value="color">纯色</ElRadioButton>
          <ElRadioButton value="img">图片</ElRadioButton>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="选择颜色" v-if="formData!.style.bgType === 'color'">
        <ColorInput v-model="formData!.style.bgColor" />
      </ElFormItem>
      <ElFormItem label="选择图片" v-if="formData!.style.bgType === 'img'">
        <UploadImg
          v-model="formData!.style.bgImg"
          width="100%"
          height="50px"
          class="min-w-[200px]"
          :show-description="false"
        >
          <template #tip> 建议尺寸 375 * 50 </template>
        </UploadImg>
      </ElFormItem>

      <ElText tag="p">图标设置</ElText>
      <ElText type="info" size="small">
        拖动左上角的小圆点可对其排序, 图标建议尺寸 44*44
      </ElText>
      <Draggable v-model="formData.items" :limit="5">
        <template #default="{ element }">
          <div class="mb-2 flex items-center justify-around">
            <div class="flex flex-col items-center justify-between">
              <UploadImg
                v-model="element.iconUrl"
                width="40px"
                height="40px"
                :show-delete="false"
                :show-description="false"
              />
              <ElText size="small">未选中</ElText>
            </div>
            <div>
              <UploadImg
                v-model="element.activeIconUrl"
                width="40px"
                height="40px"
                :show-delete="false"
                :show-description="false"
              />
              <ElText>已选中</ElText>
            </div>
          </div>
          <ElFormItem prop="text" label="文字" label-width="48px" class="mb-2">
            <ElInput v-model="element.text" placeholder="请输入文字" />
          </ElFormItem>
          <ElFormItem prop="url" label="链接" label-width="48px" class="mb-0">
            <AppLinkInput v-model="element.url" />
          </ElFormItem>
        </template>
      </Draggable>
    </ElForm>
  </div>
</template>

<style lang="scss" scoped></style>
