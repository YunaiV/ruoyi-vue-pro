<script setup lang="ts">
import type { PopoverProperty } from './config';

import { useVModel } from '@vueuse/core';
import { Form, FormItem, Radio, RadioGroup, Tooltip } from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import { AppLinkInput, Draggable } from '#/views/mall/promotion/components';

/** 弹窗广告属性面板 */
defineOptions({ name: 'PopoverProperty' });

const props = defineProps<{ modelValue: PopoverProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <Form :label-col="{ style: { width: '80px' } }" :model="formData">
    <Draggable v-model="formData.list" :empty-item="{ showType: 'once' }">
      <template #default="{ element, index }">
        <FormItem label="图片" :name="`list[${index}].imgUrl`">
          <UploadImg
            v-model="element.imgUrl"
            height="56px"
            width="56px"
            :show-description="false"
          />
        </FormItem>
        <FormItem label="跳转链接" :name="`list[${index}].url`">
          <AppLinkInput v-model="element.url" />
        </FormItem>
        <FormItem label="显示次数" :name="`list[${index}].showType`">
          <RadioGroup v-model:value="element.showType">
            <Tooltip title="只显示一次，下次打开时不显示" placement="bottom">
              <Radio value="once">一次</Radio>
            </Tooltip>
            <Tooltip title="每次打开时都会显示" placement="bottom">
              <Radio value="always">不限</Radio>
            </Tooltip>
          </RadioGroup>
        </FormItem>
      </template>
    </Draggable>
  </Form>
</template>
