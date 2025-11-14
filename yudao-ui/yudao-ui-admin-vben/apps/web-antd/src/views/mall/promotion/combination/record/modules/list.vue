<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getCombinationRecordPage } from '#/api/mall/promotion/combination/combinationRecord';

import { useUserGridColumns } from '../data';

defineOptions({ name: 'CombinationUserList' });

const headId = ref<number>();

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      headId.value = undefined;
      return;
    }
    const data = modalApi.getData<{ headId: number }>();
    if (data?.headId) {
      headId.value = data.headId;
    }
  },
});

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: useUserGridColumns(),
    height: 600,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          // 暂时返回空数据，待API实现后替换
          return await getCombinationRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            headId: headId.value,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
  } as VxeTableGridOptions,
});

const getTitle = computed(() => {
  return `拼团成员列表 (拼团ID: ${headId.value || ''})`;
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Grid class="mx-4" />
  </Modal>
</template>
