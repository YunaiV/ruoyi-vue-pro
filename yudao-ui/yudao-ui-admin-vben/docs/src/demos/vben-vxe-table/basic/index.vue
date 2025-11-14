<script lang="ts" setup>
import type { VxeGridListeners, VxeGridProps } from '#/adapter/vxe-table';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';

import { MOCK_TABLE_DATA } from '../table-data';

interface RowType {
  address: string;
  age: number;
  id: number;
  name: string;
  nickname: string;
  role: string;
}

const gridOptions: VxeGridProps<RowType> = {
  columns: [
    { title: '序号', type: 'seq', width: 50 },
    { field: 'name', title: 'Name' },
    { field: 'age', sortable: true, title: 'Age' },
    { field: 'nickname', title: 'Nickname' },
    { field: 'role', title: 'Role' },
    { field: 'address', showOverflow: true, title: 'Address' },
  ],
  data: MOCK_TABLE_DATA,
  pagerConfig: {
    enabled: false,
  },
  sortConfig: {
    multiple: true,
  },
};

const gridEvents: VxeGridListeners<RowType> = {
  cellClick: ({ row }) => {
    message.info(`cell-click: ${row.name}`);
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridEvents, gridOptions });

const showBorder = gridApi.useStore((state) => state.gridOptions?.border);
const showStripe = gridApi.useStore((state) => state.gridOptions?.stripe);

function changeBorder() {
  gridApi.setGridOptions({
    border: !showBorder.value,
  });
}

function changeStripe() {
  gridApi.setGridOptions({
    stripe: !showStripe.value,
  });
}

function changeLoading() {
  gridApi.setLoading(true);
  setTimeout(() => {
    gridApi.setLoading(false);
  }, 2000);
}
</script>

<template>
  <!-- 此处的`vp-raw` 是为了适配文档的展示效果，实际使用时不需要 -->
  <div class="vp-raw w-full">
    <Grid>
      <template #toolbar-tools>
        <Button class="mr-2" type="primary" @click="changeBorder">
          {{ showBorder ? '隐藏' : '显示' }}边框
        </Button>
        <Button class="mr-2" type="primary" @click="changeLoading">
          显示loading
        </Button>
        <Button class="mr-2" type="primary" @click="changeStripe">
          {{ showStripe ? '隐藏' : '显示' }}斑马纹
        </Button>
      </template>
    </Grid>
  </div>
</template>
