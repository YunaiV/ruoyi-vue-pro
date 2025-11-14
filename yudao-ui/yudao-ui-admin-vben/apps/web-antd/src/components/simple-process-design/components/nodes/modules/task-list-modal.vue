<script lang="ts" setup>
import { useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';

import { useGridColumns } from './task-list-data';

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    border: true,
    height: 'auto',
    rowConfig: {
      keyField: 'id',
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  },
});

const [Modal, modalApi] = useVbenModal({
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }

    modalApi.lock();
    try {
      const data = modalApi.getData<any[]>();
      // 填充列表数据
      await gridApi.setGridOptions({ data });
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-3/4">
    <Grid />
  </Modal>
</template>
