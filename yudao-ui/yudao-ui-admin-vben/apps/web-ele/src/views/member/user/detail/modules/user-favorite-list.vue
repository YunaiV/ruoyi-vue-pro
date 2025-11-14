<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallFavoriteApi } from '#/api/mall/product/favorite';

import { DICT_TYPE } from '@vben/constants';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import * as FavoriteApi from '#/api/mall/product/favorite';

const props = defineProps<{
  userId: number;
}>();

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      {
        field: 'id',
        title: '商品编号',
      },
      {
        field: 'picUrl',
        title: '商品图',
        cellRender: {
          name: 'CellImage',
          props: {
            height: 40,
            width: 40,
          },
        },
      },
      {
        field: 'name',
        title: '商品名称',
      },
      {
        field: 'price',
        title: '商品售价',
      },
      {
        field: 'salesCount',
        title: '销量',
      },
      {
        field: 'createTime',
        title: '收藏时间',
        formatter: 'formatDateTime',
      },
      {
        field: 'status',
        title: '状态',
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.PRODUCT_SPU_STATUS },
        },
      },
    ],
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    expandConfig: {
      trigger: 'row',
      expandAll: true,
      padding: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await FavoriteApi.getFavoritePage({
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
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallFavoriteApi.Favorite>,
  separator: false,
});
</script>

<template>
  <Grid />
</template>
