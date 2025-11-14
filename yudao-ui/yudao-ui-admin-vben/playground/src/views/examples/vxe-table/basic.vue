<script lang="ts" setup>
import type { VxeGridListeners, VxeGridProps } from '#/adapter/vxe-table';

import { Page } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';

import DocButton from '../doc-button.vue';
import { MOCK_TABLE_DATA } from './table-data';

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

const [Grid, gridApi] = useVbenVxeGrid<RowType>({
  // 放开注释查看表单组件的类型
  // formOptions: {
  //   schema: [
  //     {
  //       component: 'Switch',
  //       fieldName: 'name',
  //     },
  //   ],
  // },
  gridEvents,
  gridOptions,
});

// 放开注释查看当前表格实例的类型
// gridApi.grid

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
  <Page
    description="表格组件常用于快速开发数据展示与交互界面，示例数据为静态数据。该组件是对vxe-table进行简单的二次封装，大部分属性与方法与vxe-table保持一致。"
    title="表格基础示例"
  >
    <template #extra>
      <DocButton path="/components/common-ui/vben-vxe-table" />
    </template>
    <Grid table-title="基础列表" table-title-help="提示">
      <!-- <template #toolbar-actions>
        <Button class="mr-2" type="primary">左侧插槽</Button>
      </template> -->
      <template #toolbar-tools>
        <Button class="mr-2" type="primary" @click="changeBorder">
          {{ showBorder ? '隐藏' : '显示' }}边框
        </Button>
        <Button class="mr-2" type="primary" @click="changeLoading">
          显示loading
        </Button>
        <Button type="primary" @click="changeStripe">
          {{ showStripe ? '隐藏' : '显示' }}斑马纹
        </Button>
      </template>
    </Grid>
  </Page>
</template>
