<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InfraFileConfigApi } from '#/api/infra/file-config';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { isEmpty, openWindow } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteFileConfig,
  deleteFileConfigList,
  getFileConfigPage,
  testFileConfig,
  updateFileConfigMaster,
} from '#/api/infra/file-config';
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

/** 创建文件配置 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑文件配置 */
function handleEdit(row: InfraFileConfigApi.FileConfig) {
  formModalApi.setData(row).open();
}

/** 设为主配置 */
async function handleMaster(row: InfraFileConfigApi.FileConfig) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.updating', [row.name]),
    duration: 0,
  });
  try {
    await updateFileConfigMaster(row.id!);
    message.success($t('ui.actionMessage.updateSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 测试文件配置 */
async function handleTest(row: InfraFileConfigApi.FileConfig) {
  const hideLoading = message.loading({
    content: '测试上传中...',
    duration: 0,
  });
  try {
    const response = await testFileConfig(row.id!);
    // 确认是否访问该文件
    confirm({
      title: '测试上传成功',
      content: '是否要访问该文件？',
      confirmText: '访问',
      cancelText: '取消',
    }).then(() => {
      openWindow(response);
    });
  } finally {
    hideLoading();
  }
}

/** 删除文件配置 */
async function handleDelete(row: InfraFileConfigApi.FileConfig) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteFileConfig(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 批量删除文件配置 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deletingBatch'),
    duration: 0,
  });
  try {
    await deleteFileConfigList(checkedIds.value);
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
  records: InfraFileConfigApi.FileConfig[];
}) {
  checkedIds.value = records.map((item) => item.id!);
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
          return await getFileConfigPage({
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
  } as VxeTableGridOptions<InfraFileConfigApi.FileConfig>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="文件配置列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['文件配置']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['infra:file-config:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'primary',
              danger: true,
              icon: ACTION_ICON.DELETE,
              disabled: isEmpty(checkedIds),
              auth: ['infra:file-config:delete'],
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
              auth: ['infra:file-config:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '测试',
              type: 'link',
              icon: 'lucide:test-tube-diagonal',
              auth: ['infra:file-config:update'],
              onClick: handleTest.bind(null, row),
            },
            {
              label: '主配置',
              type: 'link',
              icon: ACTION_ICON.ADD,
              auth: ['infra:file-config:update'],
              disabled: row.master,
              popConfirm: {
                title: `是否要将${row.name}设为主配置？`,
                confirm: handleMaster.bind(null, row),
              },
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['infra:file-config:delete'],
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
