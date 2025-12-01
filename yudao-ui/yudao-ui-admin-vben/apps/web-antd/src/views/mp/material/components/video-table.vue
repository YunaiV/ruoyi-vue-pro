<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpMaterialApi } from '#/api/mp/material';

import { watch } from 'vue';

import { openWindow } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { WxVideoPlayer } from '#/views/mp/components';

import { useVideoGridColumns } from './data';

const props = defineProps<{
  list: MpMaterialApi.Material[];
  loading: boolean;
}>();

const emit = defineEmits<{
  delete: [v: number];
}>();

const columns = useVideoGridColumns();

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
    <template #video="{ row }">
      <WxVideoPlayer v-if="row.url" :url="row.url" />
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
              title: '确定要删除该视频吗？',
              confirm: () => emit('delete', row.id!),
            },
          },
        ]"
      />
    </template>
  </Grid>
</template>
