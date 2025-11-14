<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayOrderApi } from '#/api/pay/order';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElTag } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportOrder, getOrderPage } from '#/api/pay/order';
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

/** 导出支付订单 */
async function handleExport() {
  const data = await exportOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '支付订单.xls', source: data });
}

/** 查看详情 */
function handleDetail(row: PayOrderApi.Order) {
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
          return await getOrderPage({
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
  } as VxeTableGridOptions<PayOrderApi.Order>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="支付宝支付接入"
        url="https://doc.iocoder.cn/pay/alipay-pay-demo/"
      />
      <DocAlert
        title="微信公众号支付接入"
        url="https://doc.iocoder.cn/pay/wx-pub-pay-demo/"
      />
      <DocAlert
        title="微信小程序支付接入"
        url="https://doc.iocoder.cn/pay/wx-lite-pay-demo/"
      />
    </template>
    <DetailModal @success="handleRefresh" />
    <Grid table-title="支付订单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export', ['支付订单']),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['pay:order:export'],
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
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['pay:order:query'],
              onClick: handleDetail.bind(null, row),
            },
          ]"
        />
      </template>
      <template #no="{ row }">
        <div class="flex flex-col gap-1 text-left">
          <p class="text-sm">
            <ElTag size="small" type="primary"> 商户</ElTag>
            {{ row.merchantOrderId }}
          </p>
          <p class="text-sm" v-if="row.no">
            <ElTag size="small" type="warning">支付</ElTag> {{ row.no }}
          </p>
          <p class="text-sm" v-if="row.channelOrderNo">
            <ElTag size="small" type="success">渠道</ElTag>
            {{ row.channelOrderNo }}
          </p>
        </div>
      </template>
    </Grid>
  </Page>
</template>
