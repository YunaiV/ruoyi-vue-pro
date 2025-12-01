<script lang="ts" setup>
import type { PayRefundApi } from '#/api/pay/refund';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { getRefund } from '#/api/pay/refund';
import { useDescription } from '#/components/description';

import { useDetailSchema } from '../data';

const formData = ref<PayRefundApi.Refund>();

const [Descriptions] = useDescription({
  border: true,
  column: 2,
  schema: useDetailSchema(),
});

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<PayRefundApi.Refund>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getRefund(data.id);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    title="退款详情"
    class="w-1/2"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <Descriptions :data="formData" />
  </Modal>
</template>
