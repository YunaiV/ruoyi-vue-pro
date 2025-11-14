<script lang="ts" setup>
import type { PromotionPointProperty } from './config';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElSwitch,
  ElTooltip,
} from 'element-plus';

import ColorInput from '#/components/color-input/index.vue';
import PointShowcase from '#/views/mall/promotion/point/components/point-showcase.vue';

// 秒杀属性面板
defineOptions({ name: 'PromotionPointProperty' });

const props = defineProps<{ modelValue: PromotionPointProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm :model="formData" label-width="80px">
      <ElCard class="property-group" header="积分商城活动" shadow="never">
        <PointShowcase v-model="formData.activityIds" />
      </ElCard>
      <ElCard class="property-group" header="商品样式" shadow="never">
        <ElFormItem label="布局" prop="type">
          <ElRadioGroup v-model="formData.layoutType">
            <ElTooltip class="item" content="单列大图" placement="bottom">
              <ElRadioButton value="oneColBigImg">
                <IconifyIcon icon="fluent:text-column-one-24-filled" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip class="item" content="单列小图" placement="bottom">
              <ElRadioButton value="oneColSmallImg">
                <IconifyIcon icon="fluent:text-column-two-left-24-filled" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip class="item" content="双列" placement="bottom">
              <ElRadioButton value="twoCol">
                <IconifyIcon icon="fluent:text-column-two-24-filled" />
              </ElRadioButton>
            </ElTooltip>
            <!--<el-tooltip class="item" content="三列" placement="bottom">
              <el-radio-button value="threeCol">
                <IconifyIcon icon="fluent:text-column-three-24-filled" />
              </el-radio-button>
            </el-tooltip>-->
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="商品名称" prop="fields.name.show">
          <div class="flex gap-2">
            <ColorInput v-model="formData.fields.name.color" />
            <ElCheckbox v-model="formData.fields.name.show" />
          </div>
        </ElFormItem>
        <ElFormItem label="商品简介" prop="fields.introduction.show">
          <div class="flex gap-2">
            <ColorInput v-model="formData.fields.introduction.color" />
            <ElCheckbox v-model="formData.fields.introduction.show" />
          </div>
        </ElFormItem>
        <ElFormItem label="商品价格" prop="fields.price.show">
          <div class="flex gap-2">
            <ColorInput v-model="formData.fields.price.color" />
            <ElCheckbox v-model="formData.fields.price.show" />
          </div>
        </ElFormItem>
        <ElFormItem label="市场价" prop="fields.marketPrice.show">
          <div class="flex gap-2">
            <ColorInput v-model="formData.fields.marketPrice.color" />
            <ElCheckbox v-model="formData.fields.marketPrice.show" />
          </div>
        </ElFormItem>
        <ElFormItem label="商品销量" prop="fields.salesCount.show">
          <div class="flex gap-2">
            <ColorInput v-model="formData.fields.salesCount.color" />
            <ElCheckbox v-model="formData.fields.salesCount.show" />
          </div>
        </ElFormItem>
        <ElFormItem label="商品库存" prop="fields.stock.show">
          <div class="flex gap-2">
            <ColorInput v-model="formData.fields.stock.color" />
            <ElCheckbox v-model="formData.fields.stock.show" />
          </div>
        </ElFormItem>
      </ElCard>
      <ElCard class="property-group" header="角标" shadow="never">
        <ElFormItem label="角标" prop="badge.show">
          <ElSwitch v-model="formData.badge.show" />
        </ElFormItem>
        <ElFormItem v-if="formData.badge.show" label="角标" prop="badge.imgUrl">
          <UploadImg
            v-model="formData.badge.imgUrl"
            height="44px"
            width="72px"
            :show-description="false"
          >
            <template #tip> 建议尺寸：36 * 22</template>
          </UploadImg>
        </ElFormItem>
      </ElCard>
      <ElCard class="property-group" header="按钮" shadow="never">
        <ElFormItem label="按钮类型" prop="btnBuy.type">
          <ElRadioGroup v-model="formData.btnBuy.type">
            <ElRadioButton value="text">文字</ElRadioButton>
            <ElRadioButton value="img">图片</ElRadioButton>
          </ElRadioGroup>
        </ElFormItem>
        <template v-if="formData.btnBuy.type === 'text'">
          <ElFormItem label="按钮文字" prop="btnBuy.text">
            <ElInput v-model="formData.btnBuy.text" />
          </ElFormItem>
          <ElFormItem label="左侧背景" prop="btnBuy.bgBeginColor">
            <ColorInput v-model="formData.btnBuy.bgBeginColor" />
          </ElFormItem>
          <ElFormItem label="右侧背景" prop="btnBuy.bgEndColor">
            <ColorInput v-model="formData.btnBuy.bgEndColor" />
          </ElFormItem>
        </template>
        <template v-else>
          <ElFormItem label="图片" prop="btnBuy.imgUrl">
            <UploadImg
              v-model="formData.btnBuy.imgUrl"
              height="56px"
              width="56px"
              :show-description="false"
            >
              <template #tip> 建议尺寸：56 * 56</template>
            </UploadImg>
          </ElFormItem>
        </template>
      </ElCard>
      <ElCard class="property-group" header="商品样式" shadow="never">
        <ElFormItem label="上圆角" prop="borderRadiusTop">
          <ElSlider
            v-model="formData.borderRadiusTop"
            :max="100"
            :min="0"
            :show-input-controls="false"
            input-size="small"
            show-input
          />
        </ElFormItem>
        <ElFormItem label="下圆角" prop="borderRadiusBottom">
          <ElSlider
            v-model="formData.borderRadiusBottom"
            :max="100"
            :min="0"
            :show-input-controls="false"
            input-size="small"
            show-input
          />
        </ElFormItem>
        <ElFormItem label="间隔" prop="space">
          <ElSlider
            v-model="formData.space"
            :max="100"
            :min="0"
            :show-input-controls="false"
            input-size="small"
            show-input
          />
        </ElFormItem>
      </ElCard>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style lang="scss" scoped></style>
