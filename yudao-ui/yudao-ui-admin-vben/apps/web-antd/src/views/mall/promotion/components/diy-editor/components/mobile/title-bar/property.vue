<script setup lang="ts">
import type { TitleBarProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  Card,
  Checkbox,
  Form,
  FormItem,
  Input,
  Radio,
  RadioButton,
  RadioGroup,
  Slider,
  Tooltip,
} from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import {
  AppLinkInput,
  InputWithColor,
} from '#/views/mall/promotion/components';

import ComponentContainerProperty from '../../component-container-property.vue';

/** 导航栏属性面板 */
defineOptions({ name: 'TitleBarProperty' });

const props = defineProps<{ modelValue: TitleBarProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);

const rules = {}; // 表单校验
</script>
<template>
  <ComponentContainerProperty v-model="formData.style">
    <Form :model="formData" :rules="rules">
      <Card title="风格" class="property-group">
        <FormItem label="背景图片" name="bgImgUrl">
          <UploadImg
            v-model="formData.bgImgUrl"
            width="100%"
            height="40px"
            :show-description="false"
          >
            <!-- TODO @芋艿：这里不提示；是不是组件得封装下；-->
            <template #tip>建议尺寸 750*80</template>
          </UploadImg>
        </FormItem>
        <FormItem label="标题位置" name="textAlign">
          <RadioGroup v-model:value="formData!.textAlign">
            <Tooltip title="居左" placement="top">
              <RadioButton value="left">
                <IconifyIcon
                  icon="ant-design:align-left-outlined"
                  class="size-6"
                />
              </RadioButton>
            </Tooltip>
            <Tooltip title="居中" placement="top">
              <RadioButton value="center">
                <IconifyIcon
                  icon="ant-design:align-center-outlined"
                  class="size-6"
                />
              </RadioButton>
            </Tooltip>
          </RadioGroup>
        </FormItem>
        <FormItem label="偏移量" name="marginLeft">
          <Slider v-model:value="formData.marginLeft" :max="100" :min="0" />
        </FormItem>
        <FormItem label="高度" name="height">
          <Slider v-model:value="formData.height" :max="200" :min="20" />
        </FormItem>
      </Card>
      <Card title="主标题" class="property-group">
        <FormItem label="文字" name="title">
          <InputWithColor
            v-model="formData.title"
            v-model:color="formData.titleColor"
            show-count
            :maxlength="20"
          />
        </FormItem>
        <FormItem label="大小" name="titleSize">
          <Slider v-model:value="formData.titleSize" :max="60" :min="10" />
        </FormItem>
        <FormItem label="粗细" name="titleWeight">
          <Slider
            v-model:value="formData.titleWeight"
            :min="100"
            :max="900"
            :step="100"
          />
        </FormItem>
      </Card>
      <Card title="副标题" class="property-group">
        <FormItem label="文字" name="description">
          <InputWithColor
            v-model="formData.description"
            v-model:color="formData.descriptionColor"
            show-count
            :maxlength="50"
          />
        </FormItem>
        <FormItem label="大小" name="descriptionSize">
          <Slider
            v-model:value="formData.descriptionSize"
            :max="60"
            :min="10"
          />
        </FormItem>
        <FormItem label="粗细" name="descriptionWeight">
          <Slider
            v-model:value="formData.descriptionWeight"
            :min="100"
            :max="900"
            :step="100"
          />
        </FormItem>
      </Card>
      <Card title="查看更多" class="property-group">
        <FormItem label="是否显示" name="more.show">
          <Checkbox v-model:checked="formData.more.show" />
        </FormItem>
        <!-- 更多按钮的 样式选择 -->
        <template v-if="formData.more.show">
          <FormItem label="样式" name="more.type">
            <RadioGroup v-model:value="formData.more.type">
              <Radio value="text">文字</Radio>
              <Radio value="icon">图标</Radio>
              <Radio value="all">文字+图标</Radio>
            </RadioGroup>
          </FormItem>
          <FormItem
            label="更多文字"
            name="more.text"
            v-show="formData.more.type !== 'icon'"
          >
            <Input v-model:value="formData.more.text" />
          </FormItem>
          <FormItem label="跳转链接" name="more.url">
            <AppLinkInput v-model="formData.more.url" />
          </FormItem>
        </template>
      </Card>
    </Form>
  </ComponentContainerProperty>
</template>
