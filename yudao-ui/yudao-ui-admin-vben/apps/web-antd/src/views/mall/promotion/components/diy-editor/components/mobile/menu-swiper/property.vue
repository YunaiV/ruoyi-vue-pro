<script setup lang="ts">
import type { MenuSwiperProperty } from './config';

import { cloneDeep } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import {
  Card,
  Form,
  FormItem,
  Radio,
  RadioGroup,
  Switch,
} from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import {
  AppLinkInput,
  ColorInput,
  Draggable,
  InputWithColor,
} from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';
import { EMPTY_MENU_SWIPER_ITEM_PROPERTY } from './config';

/** 菜单导航属性面板 */
defineOptions({ name: 'MenuSwiperProperty' });

const props = defineProps<{ modelValue: MenuSwiperProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <Form :model="formData" class="mt-2">
      <FormItem label="布局" name="layout">
        <RadioGroup v-model:value="formData.layout">
          <Radio value="iconText">图标+文字</Radio>
          <Radio value="icon">仅图标</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem label="行数" name="row">
        <RadioGroup v-model:value="formData.row">
          <Radio :value="1">1行</Radio>
          <Radio :value="2">2行</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem label="列数" name="column">
        <RadioGroup v-model:value="formData.column">
          <Radio :value="3">3列</Radio>
          <Radio :value="4">4列</Radio>
          <Radio :value="5">5列</Radio>
        </RadioGroup>
      </FormItem>
      <Card title="菜单设置" class="property-group">
        <Draggable
          v-model="formData.list"
          :empty-item="cloneDeep(EMPTY_MENU_SWIPER_ITEM_PROPERTY)"
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
                <template #tip> 建议尺寸：98 * 98 </template>
              </UploadImg>
            </FormItem>
            <FormItem label="标题" name="title">
              <InputWithColor
                v-model="element.title"
                v-model:color="element.titleColor"
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
      </Card>
    </Form>
  </ComponentContainerProperty>
</template>
