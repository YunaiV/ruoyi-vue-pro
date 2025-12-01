<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDataRule, getDataRulePage } from '#/api/iot/rule/data/rule';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import DataRuleForm from './data-rule-form.vue';

// TODO @haohao：貌似和 apps/web-antd/src/views/iot/rule/data/index.vue 重复的。可能这个是对的。然后把 apps/web-antd/src/views/iot/rule/data/index.vue 搞成 tabs；

/** IoT 数据流转规则列表 */
defineOptions({ name: 'IotDataRule' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: DataRuleForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建规则 */
function handleCreate() {
  formModalApi.setData({ type: 'create' }).open();
}

/** 编辑规则 */
function handleEdit(row: any) {
  formModalApi.setData({ type: 'update', id: row.id }).open();
}

/** 删除规则 */
async function handleDelete(row: any) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteDataRule(row.id);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
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
          return await getDataRulePage({
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
    <Grid table-title="数据规则列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['规则']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['iot:data-rule:create'],
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
              auth: ['iot:data-rule:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['iot:data-rule:delete'],
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
