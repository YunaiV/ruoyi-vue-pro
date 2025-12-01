<script lang="ts" setup>
import type { MallOrderApi } from '#/api/mall/trade/order';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { updateOrderRemark } from '#/api/mall/trade/order';
import { $t } from '#/locales';

import { useRemarkFormSchema } from '../data';

const emit = defineEmits(['success']);

const formData = ref<MallOrderApi.OrderUpdateDeliveryReqVO>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useRemarkFormSchema(),
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
    const data =
      (await formApi.getValues()) as MallOrderApi.OrderUpdateRemarkReqVO;
    try {
      await updateOrderRemark(data);
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
    const data = modalApi.getData<MallOrderApi.Order>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      // 设置到 values
      await formApi.setValues(data);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal title="订单备注" class="w-1/3">
    <Form class="mx-4" />
  </Modal>
</template>
