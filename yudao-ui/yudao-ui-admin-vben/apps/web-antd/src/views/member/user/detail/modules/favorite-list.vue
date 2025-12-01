<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallFavoriteApi } from '#/api/mall/product/favorite';

import { DICT_TYPE } from '@vben/constants';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getFavoritePage } from '#/api/mall/product/favorite';

const props = defineProps<{
  userId: number;
}>();

const columns = [
  {
    field: 'id',
    title: '商品编号',
    minWidth: 100,
  },
  {
    field: 'picUrl',
    title: '商品图',
    minWidth: 100,
    cellRender: {
      name: 'CellImage',
      props: {
        width: 24,
        height: 24,
      },
    },
  },
  {
    field: 'name',
    title: '商品名称',
    minWidth: 200,
  },
  {
    field: 'price',
    title: '商品售价',
    formatter: 'formatAmount2',
    minWidth: 120,
  },
  {
    field: 'salesCount',
    title: '销量',
    minWidth: 100,
  },
  {
    field: 'createTime',
    title: '收藏时间',
    formatter: 'formatDateTime',
    minWidth: 160,
  },
  {
    field: 'status',
    title: '状态',
    minWidth: 100,
    cellRender: {
      name: 'CellDict',
      props: { type: DICT_TYPE.PRODUCT_SPU_STATUS },
    },
  },
];

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns,
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getFavoritePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: props.userId,
            ...formValues,
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
      search: true,
    },
  } as VxeTableGridOptions<MallFavoriteApi.Favorite>,
});
</script>

<template>
  <Grid />
</template>
