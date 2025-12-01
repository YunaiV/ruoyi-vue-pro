<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpAccountApi } from '#/api/mp/account';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  clearAccountQuota,
  deleteAccount,
  generateAccountQrCode,
  getAccountPage,
} from '#/api/mp/account';
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

/** 创建账号 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑账号 */
function handleEdit(row: MpAccountApi.Account) {
  formModalApi.setData(row).open();
}

/** 删除账号 */
async function handleDelete(row: MpAccountApi.Account) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteAccount(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 生成二维码 */
async function handleGenerateQrCode(row: MpAccountApi.Account) {
  const hideLoading = message.loading({
    content: '正在生成二维码中...',
    duration: 0,
  });
  try {
    await generateAccountQrCode(row.id!);
    message.success($t('ui.actionMessage.operationSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 清空 API 配额 */
async function handleCleanQuota(row: MpAccountApi.Account) {
  const hideLoading = message.loading({
    content: '正在清空 API 配额',
    duration: 0,
  });
  try {
    await clearAccountQuota(row.id!);
    message.success($t('ui.actionMessage.operationSuccess'));
  } finally {
    hideLoading();
  }
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
  } as VxeTableGridOptions<MpAccountApi.Account>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="公众号账号列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['公众号账号']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mp:account:create'],
              onClick: handleCreate,
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
              auth: ['mp:account:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mp:account:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
          :drop-down-actions="[
            {
              label: '生成二维码',
              type: 'link',
              icon: 'qrcode',
              auth: ['mp:account:qr-code'],
              onClick: handleGenerateQrCode.bind(null, row),
            },
            {
              label: '清空 API 配额',
              type: 'link',
              danger: true,
              icon: 'clear',
              auth: ['mp:account:clear-quota'],
              popConfirm: {
                title: '你确认要清空 API 配额？',
                confirm: handleCleanQuota.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
