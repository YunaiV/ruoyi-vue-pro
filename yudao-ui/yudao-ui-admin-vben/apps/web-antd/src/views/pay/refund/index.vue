<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayRefundApi } from '#/api/pay/refund';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportRefund, getRefundPage } from '#/api/pay/refund';
import { $t } from '#/locales';

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

/** 导出退款订单 */
async function handleExport() {
  const data = await exportRefund(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '退款订单.xls', source: data });
}

/** 查看详情 */
function handleDetail(row: PayRefundApi.Refund) {
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
          return await getRefundPage({
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
  } as VxeTableGridOptions<PayRefundApi.Refund>,
});
</script>
<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="支付宝、微信退款接入"
        url="https://doc.iocoder.cn/pay/refund-demo/"
      />
    </template>
    <DetailModal @success="handleRefresh" />
    <Grid table-title="支付退款列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export', ['退款订单']),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['pay:refund:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.detail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['pay:refund:query'],
              onClick: handleDetail.bind(null, row),
            },
          ]"
        />
      </template>
      <template #no="{ row }">
        <div class="flex flex-col gap-1 text-left">
          <p class="text-sm">
            <Tag size="small" color="blue"> 商户</Tag> {{ row.merchantOrderId }}
          </p>
          <p class="text-sm" v-if="row.merchantRefundId">
            <Tag size="small" color="orange">退款</Tag>
            {{ row.merchantRefundId }}
          </p>
          <p class="text-sm" v-if="row.channelRefundNo">
            <Tag size="small" color="green">渠道</Tag>
            {{ row.channelRefundNo }}
          </p>
        </div>
      </template>
    </Grid>
  </Page>
</template>
