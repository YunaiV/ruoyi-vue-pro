<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayNotifyApi } from '#/api/pay/notify';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Divider } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getNotifyTaskDetail } from '#/api/pay/notify';
import { useDescription } from '#/components/description';

import { useDetailLogColumns, useDetailSchema } from '../data';

const formData = ref<PayNotifyApi.NotifyTask>();

const [Description] = useDescription({
  bordered: true,
  column: 2,
  schema: useDetailSchema(),
});

const [LogGrid, logGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useDetailLogColumns(),
    height: 'auto',
    keepSource: true,
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: true,
      refresh: true,
    },
  } as VxeTableGridOptions,
});

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<PayNotifyApi.NotifyTask>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getNotifyTaskDetail(data.id);
      logGridApi.grid?.reloadData(formData.value?.logs || []);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    title="通知详情"
    class="w-1/2"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <Description :data="formData" />

    <Divider />

    <LogGrid table-title="支付通知列表" />
  </Modal>
</template>
