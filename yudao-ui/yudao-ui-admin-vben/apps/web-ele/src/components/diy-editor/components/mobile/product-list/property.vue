<script setup lang="ts">
import type { ProductListProperty } from './config';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElForm,
  ElFormItem,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElSwitch,
  ElTooltip,
} from 'element-plus';

import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import ColorInput from '#/components/input-with-color/index.vue';
import UploadImg from '#/components/upload/image-upload.vue';
import SpuShowcase from '#/views/mall/product/spu/components/spu-showcase.vue';

// 商品栏属性面板
defineOptions({ name: 'ProductListProperty' });

const props = defineProps<{ modelValue: ProductListProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm label-width="80px" :model="formData">
      <ElCard header="商品列表" class="property-group" shadow="never">
        <SpuShowcase v-model="formData.spuIds" />
      </ElCard>
      <ElCard header="商品样式" class="property-group" shadow="never">
        <ElFormItem label="布局" prop="type">
          <ElRadioGroup v-model="formData.layoutType">
            <ElTooltip class="item" content="双列" placement="bottom">
              <ElRadioButton value="twoCol">
                <IconifyIcon icon="fluent:text-column-two-24-filled" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip class="item" content="三列" placement="bottom">
              <ElRadioButton value="threeCol">
                <IconifyIcon icon="fluent:text-column-three-24-filled" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip class="item" content="水平滑动" placement="bottom">
              <ElRadioButton value="horizSwiper">
                <IconifyIcon icon="system-uicons:carousel" />
              </ElRadioButton>
            </ElTooltip>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="商品名称" prop="fields.name.show">
          <div class="flex gap-2">
            <ColorInput v-model="formData.fields.name.color" />
            <ElCheckbox v-model="formData.fields.name.show" />
          </div>
        </ElFormItem>
        <ElFormItem label="商品价格" prop="fields.price.show">
          <div class="flex gap-2">
            <ColorInput v-model="formData.fields.price.color" />
            <ElCheckbox v-model="formData.fields.price.show" />
          </div>
        </ElFormItem>
      </ElCard>
      <ElCard header="角标" class="property-group" shadow="never">
        <ElFormItem label="角标" prop="badge.show">
          <ElSwitch v-model="formData.badge.show" />
        </ElFormItem>
        <ElFormItem label="角标" prop="badge.imgUrl" v-if="formData.badge.show">
          <UploadImg
            v-model="formData.badge.imgUrl"
            height="44px"
            width="72px"
            :show-description="false"
          >
            <template #tip> 建议尺寸：36 * 22 </template>
          </UploadImg>
        </ElFormItem>
      </ElCard>
      <ElCard header="商品样式" class="property-group" shadow="never">
        <ElFormItem label="上圆角" prop="borderRadiusTop">
          <ElSlider
            v-model="formData.borderRadiusTop"
            :max="100"
            :min="0"
            show-input
            input-size="small"
            :show-input-controls="false"
          />
        </ElFormItem>
        <ElFormItem label="下圆角" prop="borderRadiusBottom">
          <ElSlider
            v-model="formData.borderRadiusBottom"
            :max="100"
            :min="0"
            show-input
            input-size="small"
            :show-input-controls="false"
          />
        </ElFormItem>
        <ElFormItem label="间隔" prop="space">
          <ElSlider
            v-model="formData.space"
            :max="100"
            :min="0"
            show-input
            input-size="small"
            :show-input-controls="false"
          />
        </ElFormItem>
      </ElCard>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
