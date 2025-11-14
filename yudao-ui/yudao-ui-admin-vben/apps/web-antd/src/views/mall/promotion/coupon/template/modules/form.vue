<script lang="ts" setup>
import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { CouponTemplateTakeTypeEnum } from '@vben/constants';
import { convertToInteger, formatToFraction } from '@vben/utils';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createCouponTemplate,
  getCouponTemplate,
  updateCouponTemplate,
} from '#/api/mall/promotion/coupon/couponTemplate';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MallCouponTemplateApi.CouponTemplate>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['优惠券模板'])
    : $t('ui.actionTitle.create', ['优惠券模板']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const formValues = (await formApi.getValues()) as any;
    const data = await processSubmitData(formValues);
    try {
      await (formData.value?.id
        ? updateCouponTemplate(data)
        : createCouponTemplate(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<MallCouponTemplateApi.CouponTemplate>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getCouponTemplate(data.id);
      const processedData = await processLoadData(formData.value);
      // 设置到表单
      await formApi.setValues(processedData);
    } finally {
      modalApi.unlock();
    }
  },
});

/** 处理提交数据 */
async function processSubmitData(
  formValues: any,
): Promise<MallCouponTemplateApi.CouponTemplate> {
  return {
    ...formValues,
    // 金额转换：元转分
    discountPrice: convertToInteger(formValues.discountPrice),
    discountPercent:
      formValues.discountPercent === undefined
        ? undefined
        : formValues.discountPercent * 10,
    discountLimitPrice: convertToInteger(formValues.discountLimitPrice),
    usePrice: convertToInteger(formValues.usePrice),
    // 处理有效期时间
    validStartTime:
      formValues.validTimes && formValues.validTimes.length === 2
        ? formValues.validTimes[0]
        : undefined,
    validEndTime:
      formValues.validTimes && formValues.validTimes.length === 2
        ? formValues.validTimes[1]
        : undefined,
    // 处理发放数量和限领数量
    totalCount:
      formValues.takeType === CouponTemplateTakeTypeEnum.USER.type
        ? formValues.totalCount
        : -1,
    takeLimitCount:
      formValues.takeType === CouponTemplateTakeTypeEnum.USER.type
        ? formValues.takeLimitCount
        : -1,
  };
}

/** 处理加载的数据 */
async function processLoadData(
  data: MallCouponTemplateApi.CouponTemplate,
): Promise<any> {
  return {
    ...data,
    // 金额转换：分转元
    discountPrice: formatToFraction(data.discountPrice),
    discountPercent:
      data.discountPercent === undefined
        ? undefined
        : data.discountPercent / 10,
    discountLimitPrice: formatToFraction(data.discountLimitPrice),
    usePrice: formatToFraction(data.usePrice),
    // 处理有效期时间
    validTimes:
      data.validStartTime && data.validEndTime
        ? [data.validStartTime, data.validEndTime]
        : [],
  };
}
</script>

<template>
  <Modal :title="getTitle" class="w-2/5">
    <Form class="mx-4" />
  </Modal>
</template>
