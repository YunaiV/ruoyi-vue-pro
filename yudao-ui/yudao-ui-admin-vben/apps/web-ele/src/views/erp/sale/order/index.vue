<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpSaleOrderApi } from '#/api/erp/sale/order';

import { ref } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteSaleOrder,
  exportSaleOrder,
  getSaleOrderPage,
  updateSaleOrderStatus,
} from '#/api/erp/sale/order';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

/** ERP 销售订单列表 */
defineOptions({ name: 'ErpSaleOrder' });

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
  const data = await exportSaleOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '销售订单.xls', source: data });
}

/** 新增销售订单 */
function handleCreate() {
  formModalApi.setData({ type: 'create' }).open();
}

/** 编辑销售订单 */
function handleEdit(row: ErpSaleOrderApi.SaleOrder) {
  formModalApi.setData({ type: 'edit', id: row.id }).open();
}

/** 删除销售订单 */
async function handleDelete(ids: number[]) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteSaleOrder(ids);
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 审批/反审批操作 */
async function handleUpdateStatus(
  row: ErpSaleOrderApi.SaleOrder,
  status: number,
) {
  const loadingInstance = ElLoading.service({
    text: `确定${status === 20 ? '审批' : '反审批'}该订单吗？`,
  });
  try {
    await updateSaleOrderStatus(row.id!, status);
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
  records: ErpSaleOrderApi.SaleOrder[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 查看详情 */
function handleDetail(row: ErpSaleOrderApi.SaleOrder) {
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
          return await getSaleOrderPage({
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
  } as VxeTableGridOptions<ErpSaleOrderApi.SaleOrder>,
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
        title="【销售】销售订单、出库、退货"
        url="https://doc.iocoder.cn/erp/sale/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="销售订单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['销售订单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['erp:sale-order:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['erp:sale-order:export'],
              onClick: handleExport,
            },
            {
              label: '批量删除',
              type: 'danger',
              disabled: isEmpty(checkedIds),
              icon: ACTION_ICON.DELETE,
              auth: ['erp:sale-order:delete'],
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
              auth: ['erp:sale-order:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['erp:sale-order:update'],
              ifShow: () => row.status !== 20,
              onClick: handleEdit.bind(null, row),
            },
            {
              label: row.status === 10 ? '审批' : '反审批',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.AUDIT,
              auth: ['erp:sale-order:update-status'],
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
              auth: ['erp:sale-order:delete'],
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
