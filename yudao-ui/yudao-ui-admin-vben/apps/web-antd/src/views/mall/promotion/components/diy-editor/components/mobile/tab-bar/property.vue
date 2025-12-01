<script setup lang="ts">
import type { TabBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  Form,
  FormItem,
  Input,
  RadioButton,
  RadioGroup,
  Select,
  SelectOption,
} from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import {
  AppLinkInput,
  ColorInput,
  Draggable,
} from '#/views/mall/promotion/components';

import { component, THEME_LIST } from './config';

/** 底部导航栏 */
defineOptions({ name: 'TabBarProperty' });

const props = defineProps<{ modelValue: TabBarProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

// 将数据库的值更新到右侧属性栏
component.property.items = formData.value.items;

/** 处理主题变更 */
const handleThemeChange = () => {
  const theme = THEME_LIST.find((theme) => theme.id === formData.value.theme);
  if (theme?.color) {
    formData.value.style.activeColor = theme.color;
  }
};
</script>

<template>
  <div>
    <!-- 表单 -->
    <Form
      :model="formData"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <FormItem label="主题" name="theme">
        <Select v-model:value="formData!.theme" @change="handleThemeChange">
          <SelectOption
            v-for="(theme, index) in THEME_LIST"
            :key="index"
            :label="theme.name"
            :value="theme.id"
          >
            <div class="flex items-center justify-between">
              <IconifyIcon :icon="theme.icon" :color="theme.color" />
              <span>{{ theme.name }}</span>
            </div>
          </SelectOption>
        </Select>
      </FormItem>
      <FormItem label="默认颜色">
        <ColorInput v-model="formData!.style.color" />
      </FormItem>
      <FormItem label="选中颜色">
        <ColorInput v-model="formData!.style.activeColor" />
      </FormItem>
      <FormItem label="导航背景">
        <RadioGroup v-model:value="formData!.style.bgType">
          <RadioButton value="color">纯色</RadioButton>
          <RadioButton value="img">图片</RadioButton>
        </RadioGroup>
      </FormItem>
      <FormItem label="选择颜色" v-if="formData!.style.bgType === 'color'">
        <ColorInput v-model="formData!.style.bgColor" />
      </FormItem>
      <FormItem label="选择图片" v-if="formData!.style.bgType === 'img'">
        <UploadImg
          v-model="formData!.style.bgImg"
          width="100%"
          height="50px"
          class="min-w-[200px]"
          :show-description="false"
        >
          <!-- TODO @芋艿：这里不提示；是不是组件得封装下；-->
          <template #tip> 建议尺寸 375 * 50 </template>
        </UploadImg>
      </FormItem>

      <div class="mb-2 text-base">图标设置</div>
      <div class="mb-2 text-xs text-gray-500">
        拖动左上角的小圆点可对其排序, 图标建议尺寸 44*44
      </div>
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
              <div class="text-xs">未选中</div>
            </div>
            <div>
              <UploadImg
                v-model="element.activeIconUrl"
                width="40px"
                height="40px"
                :show-delete="false"
                :show-description="false"
              />
              <div class="text-xs">已选中</div>
            </div>
          </div>
          <FormItem
            name="text"
            label="文字"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 20 }"
            class="mb-2"
          >
            <Input v-model:value="element.text" placeholder="请输入文字" />
          </FormItem>
          <FormItem
            name="url"
            label="链接"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 20 }"
            class="mb-0"
          >
            <AppLinkInput v-model="element.url" />
          </FormItem>
        </template>
      </Draggable>
    </Form>
  </div>
</template>
