<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';

import { Page } from '@vben/common-ui';

import { Button, Image, Switch, Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getExampleTableApi } from '#/api';

interface RowType {
  category: string;
  color: string;
  id: string;
  imageUrl: string;
  open: boolean;
  price: string;
  productName: string;
  releaseDate: string;
  status: 'error' | 'success' | 'warning';
}

const gridOptions: VxeGridProps<RowType> = {
  checkboxConfig: {
    highlight: true,
    labelField: 'name',
  },
  columns: [
    { title: '序号', type: 'seq', width: 50 },
    { field: 'category', title: 'Category', width: 100 },
    {
      field: 'imageUrl',
      slots: { default: 'image-url' },
      title: 'Image',
      width: 100,
    },
    {
      cellRender: { name: 'CellImage' },
      field: 'imageUrl2',
      title: 'Render Image',
      width: 130,
    },
    {
      field: 'open',
      slots: { default: 'open' },
      title: 'Open',
      width: 100,
    },
    {
      field: 'status',
      slots: { default: 'status' },
      title: 'Status',
      width: 100,
    },
    { field: 'color', title: 'Color', width: 100 },
    { field: 'productName', title: 'Product Name', width: 200 },
    { field: 'price', title: 'Price', width: 100 },
    {
      field: 'releaseDate',
      formatter: 'formatDateTime',
      title: 'Date',
      width: 200,
    },
    {
      cellRender: { name: 'CellLink', props: { text: '编辑' } },
      field: 'action',
      fixed: 'right',
      title: '操作',
      width: 120,
    },
  ],
  height: 'auto',
  keepSource: true,
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
  showOverflow: false,
};

const [Grid] = useVbenVxeGrid({ gridOptions });
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #image-url="{ row }">
        <Image :src="row.imageUrl" height="30" width="30" />
      </template>
      <template #open="{ row }">
        <Switch v-model:checked="row.open" />
      </template>
      <template #status="{ row }">
        <Tag :color="row.color">{{ row.status }}</Tag>
      </template>
      <template #action>
        <Button type="link">编辑</Button>
      </template>
    </Grid>
  </Page>
</template>
