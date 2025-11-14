<script lang="ts" setup>
import type { CrmBusinessApi } from '#/api/crm/business';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { erpPriceMultiply } from '@vben/utils';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createBusiness,
  getBusiness,
  updateBusiness,
} from '#/api/crm/business';
import { BizTypeEnum } from '#/api/crm/permission';
import { $t } from '#/locales';
import { ProductEditTable } from '#/views/crm/product/components';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmBusinessApi.Business>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['商机'])
    : $t('ui.actionTitle.create', ['商机']);
});

function handleUpdateProducts(products: any) {
  formData.value = modalApi.getData<CrmBusinessApi.Business>();
  formData.value!.products = products;
  if (formData.value) {
    const totalProductPrice =
      formData.value.products?.reduce(
        (prev, curr) => prev + curr.totalPrice,
        0,
      ) ?? 0;
    const discountPercent = formData.value.discountPercent;
    const discountPrice =
      discountPercent === null
        ? 0
        : erpPriceMultiply(totalProductPrice, discountPercent / 100);
    const totalPrice = totalProductPrice - (discountPrice ?? 0);
    formData.value!.totalProductPrice = totalProductPrice;
    formData.value!.totalPrice = totalPrice;
    formApi.setValues(formData.value!);
  }
}

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 120,
  },
  wrapperClass: 'grid-cols-3',
  layout: 'vertical',
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
    const data = (await formApi.getValues()) as CrmBusinessApi.Business;
    data.products = formData.value?.products;
    try {
      await (formData.value?.id ? updateBusiness(data) : createBusiness(data));
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
    const data = modalApi.getData<CrmBusinessApi.Business>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = data.id ? await getBusiness(data.id) : data;
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-1/2">
    <Form class="mx-4">
      <template #product="slotProps">
        <ProductEditTable
          v-bind="slotProps"
          class="w-full"
          :products="formData?.products ?? []"
          :biz-type="BizTypeEnum.CRM_BUSINESS"
          @update:products="handleUpdateProducts"
        />
      </template>
    </Form>
  </Modal>
</template>
