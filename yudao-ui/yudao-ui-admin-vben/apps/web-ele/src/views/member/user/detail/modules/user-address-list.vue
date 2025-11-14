<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberAddressApi } from '#/api/member/address';

import { h } from 'vue';

import { ElTag } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getAddressList } from '#/api/member/address';

const props = defineProps<{
  userId: number;
}>();

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      {
        field: 'id',
        title: '地址编号',
        minWidth: 100,
      },
      {
        field: 'name',
        title: '收件人名称',
        minWidth: 120,
      },
      {
        field: 'mobile',
        title: '手机号',
        minWidth: 130,
      },
      {
        field: 'areaId',
        title: '地区编码',
        minWidth: 120,
      },
      {
        field: 'detailAddress',
        title: '收件详细地址',
        minWidth: 200,
      },
      {
        field: 'defaultStatus',
        title: '是否默认',
        minWidth: 100,
        slots: {
          default: ({ row }) => {
            return h(
              ElTag,
              {
                class: 'mr-1',
                type: row.defaultStatus ? 'primary' : 'danger',
              },
              () => (row.defaultStatus ? '是' : '否'),
            );
          },
        },
      },
      {
        field: 'createTime',
        title: '创建时间',
        formatter: 'formatDateTime',
        minWidth: 160,
      },
    ],
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async () => {
          return await getAddressList({
            userId: props.userId,
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
  } as VxeTableGridOptions<MemberAddressApi.Address>,
});
</script>

<template>
  <Grid />
</template>
