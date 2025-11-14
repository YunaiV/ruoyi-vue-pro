<script setup lang="ts">
import type { MenuListProperty } from './config';

import { useVModel } from '@vueuse/core';
import { ElForm, ElFormItem, ElText } from 'element-plus';

import AppLinkInput from '#/components/app-link-input/index.vue';
import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import Draggable from '#/components/draggable/index.vue';
import InputWithColor from '#/components/input-with-color/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

import { EMPTY_MENU_LIST_ITEM_PROPERTY } from './config';

/** 列表导航属性面板 */
defineOptions({ name: 'MenuListProperty' });

const props = defineProps<{ modelValue: MenuListProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElText tag="p"> 菜单设置 </ElText>
    <ElText type="info" size="small"> 拖动左侧的小圆点可以调整顺序 </ElText>

    <!-- 表单 -->
    <ElForm label-width="60px" :model="formData" class="mt-2">
      <Draggable
        v-model="formData.list"
        :empty-item="EMPTY_MENU_LIST_ITEM_PROPERTY"
      >
        <template #default="{ element }">
          <ElFormItem label="图标" prop="iconUrl">
            <UploadImg
              v-model="element.iconUrl"
              height="80px"
              width="80px"
              :show-description="false"
            >
              <template #tip> 建议尺寸：44 * 44 </template>
            </UploadImg>
          </ElFormItem>
          <ElFormItem label="标题" prop="title">
            <InputWithColor
              v-model="element.title"
              v-model:color="element.titleColor"
            />
          </ElFormItem>
          <ElFormItem label="副标题" prop="subtitle">
            <InputWithColor
              v-model="element.subtitle"
              v-model:color="element.subtitleColor"
            />
          </ElFormItem>
          <ElFormItem label="链接" prop="url">
            <AppLinkInput v-model="element.url" />
          </ElFormItem>
        </template>
      </Draggable>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
