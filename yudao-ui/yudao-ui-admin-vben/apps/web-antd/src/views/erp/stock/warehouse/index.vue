<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpWarehouseApi } from '#/api/erp/stock/warehouse';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteWarehouse,
  exportWarehouse,
  getWarehousePage,
  updateWarehouseDefaultStatus,
} from '#/api/erp/stock/warehouse';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import WarehouseForm from './modules/form.vue';

/** 仓库管理 */
defineOptions({ name: 'ErpWarehouse' });

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 导出仓库 */
async function handleExport() {
  const data = await exportWarehouse(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '仓库.xls', source: data });
}

/** 创建仓库 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑仓库 */
function handleEdit(row: ErpWarehouseApi.Warehouse) {
  formModalApi.setData(row).open();
}

/** 删除仓库 */
async function handleDelete(row: ErpWarehouseApi.Warehouse) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteWarehouse(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 修改默认状态 */
async function handleDefaultStatusChange(
  newStatus: boolean,
  row: ErpWarehouseApi.Warehouse,
): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    const text = newStatus ? '设置' : '取消';
    confirm({
      content: `确认要${text}"${row.name}"默认吗?`,
    })
      .then(async () => {
        // 更新默认状态
        await updateWarehouseDefaultStatus(row.id!, newStatus);
        // 提示并返回成功
        message.success(`${text}默认成功`);
        handleRefresh();
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: WarehouseForm,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(handleDefaultStatusChange),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getWarehousePage({
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
  } as VxeTableGridOptions<ErpWarehouseApi.Warehouse>,
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

    <FormModal @success="handleRefresh" />
    <Grid table-title="仓库列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['仓库']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['erp:warehouse:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['erp:warehouse:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>

      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['erp:warehouse:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['erp:warehouse:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
