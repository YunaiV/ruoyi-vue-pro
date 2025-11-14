<script lang="ts" setup>
import type { MallOrderApi } from '#/api/mall/trade/order';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { fenToYuan } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { updateOrderPrice } from '#/api/mall/trade/order';
import { $t } from '#/locales';

import { usePriceFormSchema } from '../data';

const emit = defineEmits(['success']);

const formData = ref({
  id: 0,
  payPrice: '0',
  adjustPrice: '0',
  newPayPrice: '0',
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
  schema: usePriceFormSchema(),
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
    const data = await formApi.getValues();
    try {
      await updateOrderPrice({
        id: data.id,
        adjustPrice: data.adjustPrice * 100, // 转换为分
      });
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    // 加载数据
    const data = modalApi.getData<MallOrderApi.Order>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value.id = data.id;
      formData.value.payPrice = fenToYuan(data.payPrice || 0);
      formData.value.adjustPrice = fenToYuan(data.adjustPrice || 0);
      formData.value.newPayPrice = formData.value.payPrice;
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-1/3" :title="$t('ui.actionTitle.edit', ['订单价格'])">
    <Form class="mx-4" />
  </Modal>
</template>
