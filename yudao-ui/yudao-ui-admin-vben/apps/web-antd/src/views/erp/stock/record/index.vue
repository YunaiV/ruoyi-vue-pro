<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpStockRecordApi } from '#/api/erp/stock/record';

import { DocAlert, Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportStockRecord, getStockRecordPage } from '#/api/erp/stock/record';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 产品库存明细管理 */
defineOptions({ name: 'ErpStockRecord' });

/** 导出库存明细 */
async function handleExport() {
  const data = await exportStockRecord(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '产品库存明细.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
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
          return await getStockRecordPage({
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
  } as VxeTableGridOptions<ErpStockRecordApi.StockRecord>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【库存】产品库存、库存明细"
        url="https://doc.iocoder.cn/erp/stock/"
      />
    </template>

    <Grid table-title="产品库存明细列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['erp:stock-record:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
