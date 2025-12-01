<script setup lang="ts">
import type { ComponentStyle } from '../util';

import { useVModel } from '@vueuse/core';
import {
  Col,
  Form,
  FormItem,
  InputNumber,
  Radio,
  RadioGroup,
  Row,
  Slider,
  TabPane,
  Tabs,
  Tree,
} from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';
import { ColorInput } from '#/views/mall/promotion/components';

/**
 * 组件容器属性：目前右边部分
 * 用于包裹组件，为组件提供 背景、外边距、内边距、边框等样式
 */
defineOptions({ name: 'ComponentContainer' });

const props = defineProps<{ modelValue: ComponentStyle }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

const treeData: any[] = [
  {
    label: '外部边距',
    prop: 'margin',
    children: [
      {
        label: '上',
        prop: 'marginTop',
      },
      {
        label: '右',
        prop: 'marginRight',
      },
      {
        label: '下',
        prop: 'marginBottom',
      },
      {
        label: '左',
        prop: 'marginLeft',
      },
    ],
  },
  {
    label: '内部边距',
    prop: 'padding',
    children: [
      {
        label: '上',
        prop: 'paddingTop',
      },
      {
        label: '右',
        prop: 'paddingRight',
      },
      {
        label: '下',
        prop: 'paddingBottom',
      },
      {
        label: '左',
        prop: 'paddingLeft',
      },
    ],
  },
  {
    label: '边框圆角',
    prop: 'borderRadius',
    children: [
      {
        label: '上左',
        prop: 'borderTopLeftRadius',
      },
      {
        label: '上右',
        prop: 'borderTopRightRadius',
      },
      {
        label: '下右',
        prop: 'borderBottomRightRadius',
      },
      {
        label: '下左',
        prop: 'borderBottomLeftRadius',
      },
    ],
  },
];

function handleSliderChange(prop: string) {
  switch (prop) {
    case 'borderRadius': {
      formData.value.borderTopLeftRadius = formData.value.borderRadius;
      formData.value.borderTopRightRadius = formData.value.borderRadius;
      formData.value.borderBottomRightRadius = formData.value.borderRadius;
      formData.value.borderBottomLeftRadius = formData.value.borderRadius;
      break;
    }
    case 'margin': {
      formData.value.marginTop = formData.value.margin;
      formData.value.marginRight = formData.value.margin;
      formData.value.marginBottom = formData.value.margin;
      formData.value.marginLeft = formData.value.margin;
      break;
    }
    case 'padding': {
      formData.value.paddingTop = formData.value.padding;
      formData.value.paddingRight = formData.value.padding;
      formData.value.paddingBottom = formData.value.padding;
      formData.value.paddingLeft = formData.value.padding;
      break;
    }
  }
}
</script>

<template>
  <Tabs>
    <!-- 每个组件的自定义内容 -->
    <TabPane tab="内容" key="content" v-if="$slots.default">
      <slot></slot>
    </TabPane>

    <!-- 每个组件的通用内容 -->
    <!-- TODO @xingyu：这里的样式，貌似没 ele 版本的好看。 -->
    <TabPane tab="样式" key="style" force-render>
      <p class="text-lg font-bold">组件样式：</p>
      <div class="flex flex-col gap-2 rounded-md p-4 shadow-lg">
        <Form :model="formData">
          <FormItem
            label="组件背景"
            name="bgType"
            :label-col="{ style: { width: '109px' } }"
          >
            <RadioGroup v-model:value="formData.bgType">
              <Radio value="color">纯色</Radio>
              <Radio value="img">图片</Radio>
            </RadioGroup>
          </FormItem>
          <FormItem
            label="选择颜色"
            name="bgColor"
            :label-col="{ style: { width: '109px' } }"
            v-if="formData.bgType === 'color'"
          >
            <ColorInput v-model="formData.bgColor" />
          </FormItem>
          <FormItem
            label="上传图片"
            name="bgImg"
            :label-col="{ style: { width: '109px' } }"
            v-else
          >
            <UploadImg
              v-model="formData.bgImg"
              :limit="1"
              :show-description="false"
            >
              <template #tip>建议宽度 750px</template>
            </UploadImg>
          </FormItem>
          <Tree :tree-data="treeData" default-expand-all :block-node="true">
            <template #title="{ dataRef }">
              <FormItem
                :label="dataRef.label"
                :name="dataRef.prop"
                :label-col="{
                  style: { width: dataRef.children ? '80px' : '58px' },
                }"
                class="mb-0 w-full"
              >
                <Row>
                  <Col :span="11">
                    <Slider
                      v-model:value="
                        formData[dataRef.prop as keyof ComponentStyle]
                      "
                      :max="100"
                      :min="0"
                      @change="handleSliderChange(dataRef.prop)"
                      class="mr-4"
                    />
                  </Col>
                  <Col :span="2">
                    <InputNumber
                      :max="100"
                      :min="0"
                      v-model:value="
                        formData[dataRef.prop as keyof ComponentStyle]
                      "
                    />
                  </Col>
                </Row>
              </FormItem>
            </template>
          </Tree>
          <slot name="style" :style="formData"></slot>
        </Form>
      </div>
    </TabPane>
  </Tabs>
</template>
