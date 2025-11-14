<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemRoleApi } from '#/api/system/role';

import { ref } from 'vue';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteRole,
  deleteRoleList,
  exportRole,
  getRolePage,
} from '#/api/system/role';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import AssignDataPermissionForm from './modules/assign-data-permission-form.vue';
import AssignMenuForm from './modules/assign-menu-form.vue';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [AssignDataPermissionFormModel, assignDataPermissionFormApi] =
  useVbenModal({
    connectedComponent: AssignDataPermissionForm,
    destroyOnClose: true,
  });

const [AssignMenuFormModel, assignMenuFormApi] = useVbenModal({
  connectedComponent: AssignMenuForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 导出表格 */
async function handleExport() {
  const data = await exportRole(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '角色.xls', source: data });
}

/** 创建角色 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑角色 */
function handleEdit(row: SystemRoleApi.Role) {
  formModalApi.setData(row).open();
}

/** 删除角色 */
async function handleDelete(row: SystemRoleApi.Role) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteRole(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 批量删除角色 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deletingBatch'),
    duration: 0,
  });
  try {
    await deleteRoleList(checkedIds.value);
    checkedIds.value = [];
    message.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: SystemRoleApi.Role[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 分配角色的数据权限 */
function handleAssignDataPermission(row: SystemRoleApi.Role) {
  assignDataPermissionFormApi.setData(row).open();
}

/** 分配角色的菜单权限 */
function handleAssignMenu(row: SystemRoleApi.Role) {
  assignMenuFormApi.setData(row).open();
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
          return await getRolePage({
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
  } as VxeTableGridOptions<SystemRoleApi.Role>,
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
        title="功能权限"
        url="https://doc.iocoder.cn/resource-permission"
      />
      <DocAlert title="数据权限" url="https://doc.iocoder.cn/data-permission" />
    </template>

    <FormModal @success="handleRefresh" />
    <AssignDataPermissionFormModel @success="handleRefresh" />
    <AssignMenuFormModel @success="handleRefresh" />
    <Grid table-title="角色列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['角色']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['system:role:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['system:role:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'primary',
              danger: true,
              icon: ACTION_ICON.DELETE,
              disabled: isEmpty(checkedIds),
              auth: ['system:role:delete'],
              onClick: handleDeleteBatch,
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
              auth: ['system:role:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['system:role:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
          :drop-down-actions="[
            {
              label: '数据权限',
              type: 'link',
              auth: ['system:permission:assign-role-data-scope'],
              onClick: handleAssignDataPermission.bind(null, row),
            },
            {
              label: '菜单权限',
              type: 'link',
              auth: ['system:permission:assign-role-menu'],
              onClick: handleAssignMenu.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
