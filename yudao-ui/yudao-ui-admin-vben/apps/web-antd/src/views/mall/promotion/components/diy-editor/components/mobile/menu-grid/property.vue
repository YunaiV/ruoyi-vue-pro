<script setup lang="ts">
import type { MenuGridProperty } from './config';

import { useVModel } from '@vueuse/core';
import { Form, FormItem, Radio, RadioGroup, Switch } from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import {
  AppLinkInput,
  ColorInput,
  Draggable,
  InputWithColor,
} from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';
import { EMPTY_MENU_GRID_ITEM_PROPERTY } from './config';

/** 宫格导航属性面板 */
defineOptions({ name: 'MenuGridProperty' });

const props = defineProps<{ modelValue: MenuGridProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <!-- 表单 -->
    <Form
      :label-col="{ style: { width: '80px' } }"
      :model="formData"
      class="mt-2"
    >
      <FormItem label="每行数量" name="column">
        <RadioGroup v-model:value="formData.column">
          <Radio :value="3">3个</Radio>
          <Radio :value="4">4个</Radio>
        </RadioGroup>
      </FormItem>
      <p class="text-base font-bold">菜单设置</p>
      <div class="flex flex-col gap-2 rounded-md p-4 shadow-lg">
        <Draggable
          v-model="formData.list"
          :empty-item="EMPTY_MENU_GRID_ITEM_PROPERTY"
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
                <template #tip> 建议尺寸：44 * 44</template>
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
            <FormItem label="显示角标" name="badge.show">
              <Switch v-model:checked="element.badge.show" />
            </FormItem>
            <template v-if="element.badge.show">
              <FormItem label="角标内容" name="badge.text">
                <InputWithColor
                  v-model="element.badge.text"
                  v-model:color="element.badge.textColor"
                />
              </FormItem>
              <FormItem label="背景颜色" name="badge.bgColor">
                <ColorInput v-model="element.badge.bgColor" />
              </FormItem>
            </template>
          </template>
        </Draggable>
      </div>
    </Form>
  </ComponentContainerProperty>
</template>
