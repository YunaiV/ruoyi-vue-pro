<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';

import { Button } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';

import { getExampleTableApi } from '../mock-api';

interface RowType {
  category: string;
  color: string;
  id: string;
  price: string;
  productName: string;
  releaseDate: string;
}

const gridOptions: VxeGridProps<RowType> = {
  columns: [
    { fixed: 'left', title: '序号', type: 'seq', width: 50 },
    { field: 'category', title: 'Category', width: 300 },
    { field: 'color', title: 'Color', width: 300 },
    { field: 'productName', title: 'Product Name', width: 300 },
    { field: 'price', title: 'Price', width: 300 },
    {
      field: 'releaseDate',
      formatter: 'formatDateTime',
      title: 'DateTime',
      width: 500,
    },
    {
      field: 'action',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 120,
    },
  ],
  pagerConfig: {},
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getExampleTableApi({
          page: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
  rowConfig: {
    isHover: true,
  },
};

const [Grid] = useVbenVxeGrid({ gridOptions });
</script>

<template>
  <div class="vp-raw w-full">
    <Grid>
      <template #action>
        <Button type="link">编辑</Button>
      </template>
    </Grid>
  </div>
</template>
