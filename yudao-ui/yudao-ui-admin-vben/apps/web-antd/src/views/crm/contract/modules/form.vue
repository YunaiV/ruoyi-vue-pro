<script lang="ts" setup>
import type { CrmContractApi } from '#/api/crm/contract';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';
import { erpPriceMultiply } from '@vben/utils';

import { message } from 'ant-design-vue';

import {
  createContract,
  getContract,
  updateContract,
} from '#/api/crm/contract';
import { BizTypeEnum } from '#/api/crm/permission';
import { $t } from '#/locales';
import { ProductEditTable } from '#/views/crm/product/components';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmContractApi.Contract>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['合同'])
    : $t('ui.actionTitle.create', ['合同']);
});

/** 更新产品列表 */
function handleUpdateProducts(products: any) {
  formData.value = modalApi.getData<CrmContractApi.Contract>();
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
    const data = (await formApi.getValues()) as CrmContractApi.Contract;
    data.products = formData.value?.products;
    try {
      await (formData.value?.id ? updateContract(data) : createContract(data));
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
    const data = modalApi.getData<CrmContractApi.Contract>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getContract(data.id);
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
          :biz-type="BizTypeEnum.CRM_CONTRACT"
          @update:products="handleUpdateProducts"
        />
      </template>
    </Form>
  </Modal>
</template>
