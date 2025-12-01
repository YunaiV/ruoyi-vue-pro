<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpTagApi } from '#/api/mp/tag';

import { confirm, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteTag, getTagPage, syncTag } from '#/api/mp/tag';
import { $t } from '#/locales';
import { WxAccountSelect } from '#/views/mp/components';

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

/** 公众号变化时查询数据 */
function handleAccountChange(accountId: number) {
  gridApi.formApi.setValues({ accountId });
  gridApi.formApi.submitForm();
}

/** 创建标签 */
async function handleCreate() {
  const formValues = await gridApi.formApi.getValues();
  const accountId = formValues.accountId;
  if (!accountId) {
    message.warning('请先选择公众号');
    return;
  }
  formModalApi.setData({ accountId }).open();
}

/** 编辑标签 */
async function handleEdit(row: MpTagApi.Tag) {
  const formValues = await gridApi.formApi.getValues();
  const accountId = formValues.accountId;
  if (!accountId) {
    message.warning('请先选择公众号');
    return;
  }
  formModalApi.setData({ row, accountId }).open();
}

/** 删除标签 */
async function handleDelete(row: MpTagApi.Tag) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteTag(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 同步标签 */
async function handleSync() {
  const formValues = await gridApi.formApi.getValues();
  const accountId = formValues.accountId;
  if (!accountId) {
    message.warning('请先选择公众号');
    return;
  }

  await confirm('是否确认同步标签？');
  const hideLoading = message.loading({
    content: '正在同步标签...',
    duration: 0,
  });
  try {
    await syncTag(accountId);
    message.success('同步标签成功');
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
          return await getTagPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
      autoLoad: false,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MpTagApi.Tag>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="公众号标签列表">
      <template #form-accountId>
        <WxAccountSelect @change="handleAccountChange" />
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['公众号标签']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mp:tag:create'],
              onClick: handleCreate,
            },
            {
              label: '同步',
              type: 'primary',
              icon: 'lucide:refresh-ccw',
              auth: ['mp:tag:sync'],
              onClick: handleSync,
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
              auth: ['mp:tag:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mp:tag:delete'],
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
