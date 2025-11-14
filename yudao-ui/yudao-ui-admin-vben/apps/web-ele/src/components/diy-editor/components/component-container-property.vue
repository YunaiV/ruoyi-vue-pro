<script setup lang="ts">
import type { ComponentStyle } from '#/components/diy-editor/util';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElForm,
  ElFormItem,
  ElRadio,
  ElRadioGroup,
  ElSlider,
  ElTabPane,
  ElTabs,
  ElTree,
} from 'element-plus';

import ColorInput from '#/components/color-input/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';

/**
 * 组件容器属性：目前右边部分
 * 用于包裹组件，为组件提供 背景、外边距、内边距、边框等样式
 */
defineOptions({ name: 'ComponentContainer' });

const props = defineProps<{ modelValue: ComponentStyle }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

const treeData = [
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

const handleSliderChange = (prop: string) => {
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
};
</script>

<template>
  <ElTabs stretch>
    <!-- 每个组件的自定义内容 -->
    <ElTabPane label="内容" v-if="$slots.default">
      <slot></slot>
    </ElTabPane>

    <!-- 每个组件的通用内容 -->
    <ElTabPane label="样式" lazy>
      <ElCard header="组件样式" class="property-group">
        <ElForm :model="formData" label-width="80px">
          <ElFormItem label="组件背景" prop="bgType">
            <ElRadioGroup v-model="formData.bgType">
              <ElRadio value="color">纯色</ElRadio>
              <ElRadio value="img">图片</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
          <ElFormItem
            label="选择颜色"
            prop="bgColor"
            v-if="formData.bgType === 'color'"
          >
            <ColorInput v-model="formData.bgColor" />
          </ElFormItem>
          <ElFormItem label="上传图片" prop="bgImg" v-else>
            <UploadImg
              v-model="formData.bgImg"
              :limit="1"
              :show-description="false"
            >
              <template #tip>建议宽度 750px</template>
            </UploadImg>
          </ElFormItem>
          <ElTree
            :data="treeData"
            :expand-on-click-node="false"
            default-expand-all
          >
            <template #default="{ node, data }">
              <ElFormItem
                :label="data.label"
                :prop="data.prop"
                :label-width="node.level === 1 ? '80px' : '62px'"
                class="mb-0 w-full"
              >
                <ElSlider
                  v-model="
                    formData[data.prop as keyof ComponentStyle] as number
                  "
                  :max="100"
                  :min="0"
                  show-input
                  input-size="small"
                  :show-input-controls="false"
                  @input="handleSliderChange(data.prop)"
                />
              </ElFormItem>
            </template>
          </ElTree>
          <slot name="style" :style="formData"></slot>
        </ElForm>
      </ElCard>
    </ElTabPane>
  </ElTabs>
</template>

<style scoped lang="scss">
:deep(.el-slider__runway) {
  margin-right: 16px;
}

:deep(.el-input-number) {
  width: 50px;
}
</style>
