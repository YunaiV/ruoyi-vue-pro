<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayNotifyApi } from '#/api/pay/notify';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getNotifyTaskPage } from '#/api/pay/notify';

import { useGridColumns, useGridFormSchema } from './data';
import Detail from './modules/detail.vue';

const [DetailModal, detailModalApi] = useVbenModal({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 查看详情 */
function handleDetail(row: PayNotifyApi.NotifyTask) {
  detailModalApi.setData(row).open();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    cellConfig: {
      height: 80,
    },
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getNotifyTaskPage({
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
  } as VxeTableGridOptions<PayNotifyApi.NotifyTask>,
});
</script>
<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="支付功能开启" url="https://doc.iocoder.cn/pay/build/" />
    </template>

    <DetailModal @success="handleRefresh" />
    <Grid table-title="通知列表">
      <template #merchantInfo="{ row }">
        <div class="flex flex-col gap-1 text-left">
          <p class="text-sm" v-if="row.merchantOrderId">
            <Tag size="small" color="blue">商户订单编号</Tag>
            {{ row.merchantOrderId }}
          </p>
          <p class="text-sm" v-if="row.merchantRefundId">
            <Tag size="small" color="orange">商户退款编号</Tag>
            {{ row.merchantRefundId }}
          </p>
          <p class="text-sm" v-if="row.merchantTransferId">
            <Tag size="small" color="green">商户转账编号</Tag>
            {{ row.merchantTransferId }}
          </p>
        </div>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.detail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['pay:notify:query'],
              onClick: handleDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
