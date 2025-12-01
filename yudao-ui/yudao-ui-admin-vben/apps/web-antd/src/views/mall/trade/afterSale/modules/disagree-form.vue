<script setup lang="ts">
import type { MallAfterSaleApi } from '#/api/mall/trade/afterSale';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { disagreeAfterSale } from '#/api/mall/trade/afterSale';

import { useDisagreeFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MallAfterSaleApi.AfterSale>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useDisagreeFormSchema(),
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
    try {
      const data =
        (await formApi.getValues()) as MallAfterSaleApi.AfterSaleDisagreeReqVO;
      await disagreeAfterSale(data);
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
    const data = modalApi.getData<{ afterSale: MallAfterSaleApi.AfterSale }>();
    if (!data?.afterSale) {
      return;
    }
    formData.value = data.afterSale;
    modalApi.lock();
    try {
      // 设置表单数据
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal title="拒绝售后" class="w-1/3">
    <Form class="mx-4" />
  </Modal>
</template>
