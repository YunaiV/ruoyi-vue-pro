<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBargainHelpApi } from '#/api/mall/promotion/bargain/bargainHelp';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getBargainHelpPage } from '#/api/mall/promotion/bargain/bargainHelp';

import { useHelpGridColumns } from '../data';

/** 助力列表 */
defineOptions({ name: 'BargainRecordListDialog' });

const recordId = ref<number>();
const getTitle = computed(() => {
  return `助力列表 - 记录${recordId.value || ''}`;
});

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      recordId.value = undefined;
      return;
    }
    // 获取传入的记录ID
    const data = modalApi.getData<{ recordId: number }>();
    if (data?.recordId) {
      recordId.value = data.recordId;
    }
  },
});

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: useHelpGridColumns(),
    height: 600,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getBargainHelpPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            recordId: recordId.value,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
    },
  } as VxeTableGridOptions<MallBargainHelpApi.BargainHelp>,
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Grid class="mx-4" />
  </Modal>
</template>
