<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBrokerageRecordApi } from '#/api/mall/trade/brokerage/record';

import { DocAlert, Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getBrokerageRecordPage } from '#/api/mall/trade/brokerage/record';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'TradeBrokerageRecord' });

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getBrokerageRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
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
  } as VxeTableGridOptions<MallBrokerageRecordApi.BrokerageRecord>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【交易】分销返佣"
        url="https://doc.iocoder.cn/mall/trade-brokerage/"
      />
    </template>

    <Grid table-title="分销返佣记录" />
  </Page>
</template>
