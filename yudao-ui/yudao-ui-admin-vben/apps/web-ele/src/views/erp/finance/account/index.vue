<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpAccountApi } from '#/api/erp/finance/account';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteAccount,
  exportAccount,
  getAccountPage,
  updateAccountDefaultStatus,
} from '#/api/erp/finance/account';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

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
  const data = await exportAccount(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '结算账户信息.xls', source: data });
}

/** 创建结算账户 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑结算账户 */
function handleEdit(row: ErpAccountApi.Account) {
  formModalApi.setData(row).open();
}

/** 删除结算账户 */
async function handleDelete(row: ErpAccountApi.Account) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteAccount(row.id as number);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 修改默认状态 */
async function handleDefaultStatusChange(
  newStatus: boolean,
  row: ErpAccountApi.Account,
): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    const text = newStatus ? '设置' : '取消';
    confirm({
      content: `确认要${text}"${row.name}"默认吗?`,
    })
      .then(async () => {
        // 更新默认状态
        await updateAccountDefaultStatus(row.id!, newStatus);
        // 提示并返回成功
        ElMessage.success(`${text}默认成功`);
        handleRefresh();
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

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
          return await getAccountPage({
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
  } as VxeTableGridOptions<ErpAccountApi.Account>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【财务】采购付款、销售收款"
        url="https://doc.iocoder.cn/sale/finance-payment-receipt/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="结算账户列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['结算账户']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['erp:account:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['erp:account:export'],
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
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['erp:account:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['erp:account:delete'],
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
