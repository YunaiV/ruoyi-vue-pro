<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { RuleSceneApi } from '#/api/iot/rule/scene';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteSceneRule,
  getSceneRulePage,
  updateSceneRuleStatus,
} from '#/api/iot/rule/scene';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

defineOptions({ name: 'IoTRuleScene' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建场景规则 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑场景规则 */
function handleEdit(row: RuleSceneApi.SceneRule) {
  formModalApi.setData(row).open();
}

/** 启用/停用场景规则 */
async function handleToggleStatus(row: RuleSceneApi.SceneRule) {
  const newStatus = row.status === 0 ? 1 : 0;
  const hideLoading = message.loading({
    content: newStatus === 0 ? '正在启用...' : '正在停用...',
    duration: 0,
  });
  try {
    await updateSceneRuleStatus(row.id as number, newStatus);
    message.success({
      content: newStatus === 0 ? '启用成功' : '停用成功',
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 删除场景规则 */
async function handleDelete(row: RuleSceneApi.SceneRule) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteSceneRule(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
    });
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
          return await getSceneRulePage({
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
  } as VxeTableGridOptions<RuleSceneApi.SceneRule>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="场景规则列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['场景规则']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              onClick: handleCreate,
            },
          ]"
        />
      </template>

      <!-- 操作列 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: row.status === 0 ? '停用' : '启用',
              type: 'link',
              icon:
                row.status === 0
                  ? 'ant-design:stop-outlined'
                  : 'ant-design:check-circle-outlined',
              onClick: handleToggleStatus.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
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
