<script lang="ts" setup>
import type { MpMaterialApi } from '#/api/mp/material';

import { nextTick, onMounted, watch } from 'vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';

import { useImageGridColumns } from './data';

const props = defineProps<{
  list: MpMaterialApi.Material[];
  loading: boolean;
}>();

const emit = defineEmits<{
  delete: [v: number];
}>();

const columns = useImageGridColumns();

const [Grid, gridApi] = useVbenVxeGrid<MpMaterialApi.Material>({
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
      height: 220,
    },
    showOverflow: 'tooltip',
  },
});

function updateGridData(data: MpMaterialApi.Material[]) {
  if (gridApi.grid?.loadData) {
    gridApi.grid.loadData(data);
  } else {
    gridApi.setGridOptions({ data });
  }
}

watch(
  () => props.list,
  async (list: MpMaterialApi.Material[]) => {
    const data = Array.isArray(list) ? list : [];
    await nextTick();
    updateGridData(data);
  },
  { flush: 'post' },
);

watch(
  () => props.loading,
  (loading: boolean) => {
    gridApi.setLoading(loading);
  },
);

/** 初始化 */
onMounted(async () => {
  await nextTick();
  updateGridData(Array.isArray(props.list) ? props.list : []);
  gridApi.setLoading(props.loading);
});
</script>

<template>
  <Grid class="image-table-grid mt-4 pb-0">
    <template #image="{ row }">
      <div class="flex items-center justify-center" style="height: 192px">
        <img
          :src="row.url"
          class="object-contain"
          style="display: block; max-width: 100%; max-height: 192px"
        />
      </div>
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: '删除',
            type: 'link',
            danger: true,
            icon: ACTION_ICON.DELETE,
            auth: ['mp:material:delete'],
            popConfirm: {
              title: '确定要删除该图片吗？',
              confirm: () => emit('delete', row.id!),
            },
          },
        ]"
      />
    </template>
  </Grid>
</template>
