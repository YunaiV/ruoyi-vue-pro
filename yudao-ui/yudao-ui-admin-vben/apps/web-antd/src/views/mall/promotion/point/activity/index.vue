<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  closePointActivity,
  deletePointActivity,
  getPointActivityPage,
} from '#/api/mall/promotion/point';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

defineOptions({ name: 'PromotionPointActivity' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建积分商城活动 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑积分商城活动 */
function handleEdit(row: any) {
  formModalApi.setData(row).open();
}

/** 关闭积分商城活动 */
async function handleClose(row: any) {
  const hideLoading = message.loading({
    content: '正在关闭中...',
    duration: 0,
  });
  try {
    await closePointActivity(row.id);
    message.success('关闭成功');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 删除积分商城活动 */
async function handleDelete(row: any) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.spuName]),
    duration: 0,
  });
  try {
    await deletePointActivity(row.id);
    message.success($t('ui.actionMessage.deleteSuccess', [row.spuName]));
    handleRefresh();
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
          return await getPointActivityPage({
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
  } as VxeTableGridOptions,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="积分商城活动列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['积分活动']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:point-activity:create'],
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
              auth: ['promotion:point-activity:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '关闭',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.CLOSE,
              ifShow: row.status === 0,
              auth: ['promotion:point-activity:close'],
              popConfirm: {
                title: '确认关闭该积分商城活动吗？',
                confirm: handleClose.bind(null, row),
              },
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              ifShow: row.status !== 0,
              auth: ['promotion:point-activity:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.spuName]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
