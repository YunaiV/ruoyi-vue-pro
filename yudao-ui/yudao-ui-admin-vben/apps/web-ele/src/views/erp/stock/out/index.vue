<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpStockOutApi } from '#/api/erp/stock/out';

import { ref } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteStockOut,
  exportStockOut,
  getStockOutPage,
  updateStockOutStatus,
} from '#/api/erp/stock/out';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

/** ERP 其它出库单列表 */
defineOptions({ name: 'ErpStockOut' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 导出表格 */
async function handleExport() {
  const data = await exportStockOut(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '其它出库单.xls', source: data });
}

/** 新增其它出库单 */
function handleCreate() {
  formModalApi.setData({ type: 'create' }).open();
}

/** 编辑其它出库单 */
function handleEdit(row: ErpStockOutApi.StockOut) {
  formModalApi.setData({ type: 'edit', id: row.id }).open();
}

/** 删除其它出库单 */
async function handleDelete(ids: number[]) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteStockOut(ids);
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 审批/反审批操作 */
async function handleUpdateStatus(
  row: ErpStockOutApi.StockOut,
  status: number,
) {
  const loadingInstance = ElLoading.service({
    text: `确定${status === 20 ? '审批' : '反审批'}该出库单吗？`,
  });
  try {
    await updateStockOutStatus(row.id!, status);
    ElMessage.success(`${status === 20 ? '审批' : '反审批'}成功`);
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: ErpStockOutApi.StockOut[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 查看详情 */
function handleDetail(row: ErpStockOutApi.StockOut) {
  formModalApi.setData({ type: 'detail', id: row.id }).open();
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
          return await getStockOutPage({
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
  } as VxeTableGridOptions<ErpStockOutApi.StockOut>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【库存】其它入库、其它出库"
        url="https://doc.iocoder.cn/erp/stock-in-out/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="其它出库单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['其它出库单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['erp:stock-out:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['erp:stock-out:export'],
              onClick: handleExport,
            },
            {
              label: '批量删除',
              type: 'danger',
              disabled: isEmpty(checkedIds),
              icon: ACTION_ICON.DELETE,
              auth: ['erp:stock-out:delete'],
              popConfirm: {
                title: `是否删除所选中数据？`,
                confirm: handleDelete.bind(null, checkedIds),
              },
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
              auth: ['erp:stock-out:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['erp:stock-out:update'],
              ifShow: () => row.status !== 20,
              onClick: handleEdit.bind(null, row),
            },
            {
              label: row.status === 10 ? '审批' : '反审批',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.AUDIT,
              auth: ['erp:stock-out:update-status'],
              popConfirm: {
                title: `确认${row.status === 10 ? '审批' : '反审批'}${row.no}吗？`,
                confirm: handleUpdateStatus.bind(
                  null,
                  row,
                  row.status === 10 ? 20 : 10,
                ),
              },
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['erp:stock-out:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.no]),
                confirm: handleDelete.bind(null, [row.id!]),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
