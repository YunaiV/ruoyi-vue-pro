<script lang="ts" setup>
import type { PayTransferApi } from '#/api/pay/transfer';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { getTransfer } from '#/api/pay/transfer';
import { useDescription } from '#/components/description';

import { useDetailSchema } from '../data';

const formData = ref<PayTransferApi.Transfer>();

const [Descriptions] = useDescription({
  bordered: true,
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
    const data = modalApi.getData<PayTransferApi.Transfer>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getTransfer(data.id);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    title="转账单详情"
    class="w-1/2"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <Descriptions :data="formData" />
  </Modal>
</template>
