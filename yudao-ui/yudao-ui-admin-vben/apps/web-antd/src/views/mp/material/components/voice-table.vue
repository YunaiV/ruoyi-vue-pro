<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpMaterialApi } from '#/api/mp/material';

import { watch } from 'vue';

import { openWindow } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { WxVoicePlayer } from '#/views/mp/components';

import { useVoiceGridColumns } from './data';
// TODO @dylan：组件内，尽量用 modules 哈。只有对外共享，才用 components

const props = defineProps<{
  list: MpMaterialApi.Material[];
  loading: boolean;
}>();

const emit = defineEmits<{
  delete: [v: number];
}>();

const columns = useVoiceGridColumns();

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns,
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    showOverflow: 'tooltip',
  } as VxeTableGridOptions<MpMaterialApi.Material>,
});

watch(
  () => props.list,
  (list: MpMaterialApi.Material[]) => {
    const data = Array.isArray(list) ? list : [];
    if (gridApi.grid?.loadData) {
      gridApi.grid.loadData(data);
    } else {
      gridApi.setGridOptions({ data });
    }
  },
  { immediate: true },
);

watch(
  () => props.loading,
  (loading: boolean) => {
    gridApi.setLoading(loading);
  },
  { immediate: true },
);
</script>

<template>
  <Grid class="mt-4">
    <template #voice="{ row }">
      <WxVoicePlayer v-if="row.url" :url="row.url" />
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: '下载',
            type: 'link',
            icon: ACTION_ICON.DOWNLOAD,
            onClick: () => openWindow(row.url),
          },
          {
            label: '删除',
            type: 'link',
            danger: true,
            icon: ACTION_ICON.DELETE,
            auth: ['mp:material:delete'],
            popConfirm: {
              title: '确定要删除该语音吗？',
              confirm: () => emit('delete', row.id!),
            },
          },
        ]"
      />
    </template>
  </Grid>
</template>
