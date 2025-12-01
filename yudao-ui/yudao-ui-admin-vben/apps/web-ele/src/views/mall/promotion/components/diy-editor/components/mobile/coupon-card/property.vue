<script setup lang="ts">
import type { CouponCardProperty } from './config';

import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { ref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import {
  CouponTemplateTakeTypeEnum,
  PromotionDiscountTypeEnum,
} from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { floatToFixed2 } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import {
  ElCard,
  ElForm,
  ElFormItem,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElTooltip,
} from 'element-plus';

import { getCouponTemplateList } from '#/api/mall/promotion/coupon/couponTemplate';
import UploadImg from '#/components/upload/image-upload.vue';
import { ColorInput } from '#/views/mall/promotion/components';
import CouponSelect from '#/views/mall/promotion/coupon/components/select.vue';

import ComponentContainerProperty from '../../component-container-property.vue';

/** 优惠券卡片属性面板 */
defineOptions({ name: 'CouponCardProperty' });

const props = defineProps<{ modelValue: CouponCardProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);

const couponList = ref<MallCouponTemplateApi.CouponTemplate[]>([]); // 已选择的优惠券列表

const [CouponSelectModal, couponSelectModalApi] = useVbenModal({
  connectedComponent: CouponSelect,
  destroyOnClose: true,
});

/** 添加优惠劵 */
const handleAddCoupon = () => {
  couponSelectModalApi.open();
};

/** 处理优惠劵选择 */
const handleCouponSelect = (
  selectedCoupons: MallCouponTemplateApi.CouponTemplate[],
) => {
  couponList.value = selectedCoupons;
  formData.value.couponIds = selectedCoupons.map((coupon) => coupon.id);
};

/** 监听优惠券 ID 变化，加载优惠券列表 */
watch(
  () => formData.value.couponIds,
  async () => {
    if (formData.value.couponIds?.length > 0) {
      couponList.value = await getCouponTemplateList(formData.value.couponIds);
    }
  },
  {
    immediate: true,
    deep: true,
  },
);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm label-width="80px" :model="formData">
      <ElCard header="优惠券列表" class="property-group" shadow="never">
        <div
          v-for="(coupon, index) in couponList"
          :key="index"
          class="flex items-center justify-between"
        >
          <ElText size="large" truncated>{{ coupon.name }}</ElText>
          <ElText type="info" truncated>
            <span v-if="coupon.usePrice > 0">
              满{{ floatToFixed2(coupon.usePrice) }}元，
            </span>
            <span
              v-if="
                coupon.discountType === PromotionDiscountTypeEnum.PRICE.type
              "
            >
              减{{ floatToFixed2(coupon.discountPrice) }}元
            </span>
            <span v-else> 打{{ coupon.discountPercent }}折 </span>
          </ElText>
        </div>
        <ElFormItem label-width="0">
          <ElButton
            @click="handleAddCoupon"
            type="primary"
            plain
            class="mt-2 w-full"
          >
            <IconifyIcon icon="ep:plus" class="mr-1" /> 添加
          </ElButton>
        </ElFormItem>
      </ElCard>
      <ElCard header="优惠券样式" class="property-group" shadow="never">
        <ElFormItem label="列数" prop="type">
          <ElRadioGroup v-model="formData.columns">
            <ElTooltip class="item" content="一列" placement="bottom">
              <ElRadioButton :value="1">
                <IconifyIcon icon="fluent:text-column-one-24-filled" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip class="item" content="二列" placement="bottom">
              <ElRadioButton :value="2">
                <IconifyIcon icon="fluent:text-column-two-24-filled" />
              </ElRadioButton>
            </ElTooltip>
            <ElTooltip class="item" content="三列" placement="bottom">
              <ElRadioButton :value="3">
                <IconifyIcon icon="fluent:text-column-three-24-filled" />
              </ElRadioButton>
            </ElTooltip>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="背景图片" prop="bgImg">
          <UploadImg
            v-model="formData.bgImg"
            height="80px"
            width="100%"
            class="min-w-[160px]"
            :show-description="false"
          />
        </ElFormItem>
        <ElFormItem label="文字颜色" prop="textColor">
          <ColorInput v-model="formData.textColor" />
        </ElFormItem>
        <ElFormItem label="按钮背景" prop="button.bgColor">
          <ColorInput v-model="formData.button.bgColor" />
        </ElFormItem>
        <ElFormItem label="按钮文字" prop="button.color">
          <ColorInput v-model="formData.button.color" />
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

  <!-- 优惠券选择 -->
  <CouponSelectModal
    :take-type="CouponTemplateTakeTypeEnum.USER.type"
    @success="handleCouponSelect"
  />
</template>
