<script setup lang="ts">
import type { MenuListProperty } from './config';

import { useVModel } from '@vueuse/core';
import { Form, FormItem } from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import {
  AppLinkInput,
  Draggable,
  InputWithColor,
} from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';
import { EMPTY_MENU_LIST_ITEM_PROPERTY } from './config';

/** 列表导航属性面板 */
defineOptions({ name: 'MenuListProperty' });

const props = defineProps<{ modelValue: MenuListProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <p class="text-base font-bold">菜单设置</p>
    <p class="text-xs text-gray-500">拖动左侧的小圆点可以调整顺序</p>
    <Form
      :label-col="{ style: { width: '60px' } }"
      :model="formData"
      class="mt-2"
    >
      <Draggable
        v-model="formData.list"
        :empty-item="EMPTY_MENU_LIST_ITEM_PROPERTY"
      >
        <template #default="{ element }">
          <FormItem label="图标" name="iconUrl">
            <UploadImg
              v-model="element.iconUrl"
              height="80px"
              width="80px"
              :show-description="false"
            >
              <!-- TODO @芋艿：这里不提示；是不是组件得封装下；-->
              <template #tip> 建议尺寸：44 * 44 </template>
            </UploadImg>
          </FormItem>
          <FormItem label="标题" name="title">
            <InputWithColor
              v-model="element.title"
              v-model:color="element.titleColor"
            />
          </FormItem>
          <FormItem label="副标题" name="subtitle">
            <InputWithColor
              v-model="element.subtitle"
              v-model:color="element.subtitleColor"
            />
          </FormItem>
          <FormItem label="链接" name="url">
            <AppLinkInput v-model="element.url" />
          </FormItem>
        </template>
      </Draggable>
    </Form>
  </ComponentContainerProperty>
</template>
