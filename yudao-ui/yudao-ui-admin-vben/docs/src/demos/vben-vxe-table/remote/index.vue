<script lang="ts" setup>
import type { DemoTableApi } from '../mock-api';

import type { VxeGridProps } from '#/adapter/vxe-table';

import { Button } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';

import { MOCK_API_DATA } from '../table-data';

interface RowType {
  category: string;
  color: string;
  id: string;
  price: string;
  productName: string;
  releaseDate: string;
}

// 数据实例
// const MOCK_TREE_TABLE_DATA = [
//   {
//     date: '2020-08-01',
//     id: 10_000,
//     name: 'Test1',
//     parentId: null,
//     size: 1024,
//     type: 'mp3',
//   },
// ]

const sleep = (time = 1000) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(true);
    }, time);
  });
};

/**
 * 获取示例表格数据
 */
async function getExampleTableApi(params: DemoTableApi.PageFetchParams) {
  return new Promise<{ items: any; total: number }>((resolve) => {
    const { page, pageSize } = params;
    const items = MOCK_API_DATA.slice((page - 1) * pageSize, page * pageSize);

    sleep(1000).then(() => {
      resolve({
        total: items.length,
        items,
      });
    });
  });
}

const gridOptions: VxeGridProps<RowType> = {
  checkboxConfig: {
    highlight: true,
    labelField: 'name',
  },
  columns: [
    { title: '序号', type: 'seq', width: 50 },
    { align: 'left', title: 'Name', type: 'checkbox', width: 100 },
    { field: 'category', title: 'Category' },
    { field: 'color', title: 'Color' },
    { field: 'productName', title: 'Product Name' },
    { field: 'price', title: 'Price' },
    { field: 'releaseDate', formatter: 'formatDateTime', title: 'DateTime' },
  ],
  exportConfig: {},
  // height: 'auto', // 如果设置为 auto，则必须确保存在父节点且不允许存在相邻元素，否则会出现高度闪动问题
  keepSource: true,
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
  toolbarConfig: {
    custom: true,
    export: true,
    // import: true,
    refresh: true,
    zoom: true,
  },
};

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions,
});
</script>

<template>
  <div class="vp-raw w-full">
    <Grid>
      <template #toolbar-tools>
        <Button class="mr-2" type="primary" @click="() => gridApi.query()">
          刷新当前页面
        </Button>
        <Button type="primary" @click="() => gridApi.reload()">
          刷新并返回第一页
        </Button>
      </template>
    </Grid>
  </div>
</template>
