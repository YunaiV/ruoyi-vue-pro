<script lang="ts" setup>
import type { SystemSmsLogApi } from '#/api/system/sms/log';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useDescription } from '#/components/description';

import { useDetailSchema } from '../data';

const formData = ref<SystemSmsLogApi.SmsLog>();

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
    const data = modalApi.getData<SystemSmsLogApi.SmsLog>();
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
    title="短信日志详情"
    class="w-[1280px]"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <Descriptions :data="formData" />
  </Modal>
</template>
