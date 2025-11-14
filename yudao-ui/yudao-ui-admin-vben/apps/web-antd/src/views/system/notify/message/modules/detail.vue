<script lang="ts" setup>
import type { SystemNotifyMessageApi } from '#/api/system/notify/message';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useDescription } from '#/components/description';

import { useDetailSchema } from '../data';

const formData = ref<SystemNotifyMessageApi.NotifyMessage>();

const [Descriptions] = useDescription({
  componentProps: {
    bordered: true,
    column: 1,
    class: 'mx-4',
  },
  schema: useDetailSchema(),
});

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<SystemNotifyMessageApi.NotifyMessage>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = data;
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    title="站内信详情"
    class="w-1/3"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <Descriptions :data="formData" />
  </Modal>
</template>
