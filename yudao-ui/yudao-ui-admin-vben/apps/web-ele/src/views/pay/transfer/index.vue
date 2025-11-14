<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayTransferApi } from '#/api/pay/transfer';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElTag } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportTransfer, getTransferPage } from '#/api/pay/transfer';
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

/** 导出转账单 */
async function handleExport() {
  const data = await exportTransfer(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '转账单.xls', source: data });
}

/** 查看转账详情 */
function handleDetail(row: PayTransferApi.Transfer) {
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
          return await getTransferPage({
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
  } as VxeTableGridOptions<PayTransferApi.Transfer>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="转账管理" url="https://doc.iocoder.cn/pay/transfer/" />
    </template>

    <DetailModal @success="handleRefresh" />
    <Grid table-title="转账单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export', ['转账单']),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['pay:transfer:export'],
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
              auth: ['pay:transfer:query'],
              onClick: handleDetail.bind(null, row),
            },
          ]"
        />
      </template>
      <template #no="{ row }">
        <div class="flex flex-col gap-1 text-left">
          <p class="text-sm">
            <ElTag size="small" type="primary"> 商户</ElTag>
            {{ row.merchantTransferId }}
          </p>
          <p class="text-sm" v-if="row.no">
            <ElTag size="small" type="warning">转账</ElTag> {{ row.no }}
          </p>
          <p class="text-sm" v-if="row.channelTransferNo">
            <ElTag size="small" type="success">渠道</ElTag>
            {{ row.channelTransferNo }}
          </p>
        </div>
      </template>
    </Grid>
  </Page>
</template>
