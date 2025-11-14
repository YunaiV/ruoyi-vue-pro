<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WalletTransactionApi } from '#/api/pay/wallet/transaction';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTransactionPage } from '#/api/pay/wallet/transaction';

const props = defineProps<{
  walletId: number | undefined;
}>();

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      {
        field: 'id',
        title: '编号',
        minWidth: 100,
      },
      {
        field: 'title',
        title: '关联业务标题',
        minWidth: 200,
      },
      {
        field: 'price',
        title: '交易金额',
        minWidth: 120,
        formatter: 'formatFenToYuanAmount',
      },
      {
        field: 'balance',
        title: '钱包余额',
        minWidth: 120,
        formatter: 'formatFenToYuanAmount',
      },
      {
        field: 'createTime',
        title: '交易时间',
        minWidth: 180,
        formatter: 'formatDateTime',
      },
    ],
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getTransactionPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            walletId: props.walletId,
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
  } as VxeTableGridOptions<WalletTransactionApi.Transaction>,
});
</script>

<template>
  <Grid />
</template>
