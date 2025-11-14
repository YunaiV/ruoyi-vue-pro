<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InfraFileApi } from '#/api/infra/file';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { isEmpty, openWindow } from '@vben/utils';

import { useClipboard } from '@vueuse/core';
import { Button, Image, message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteFile, deleteFileList, getFilePage } from '#/api/infra/file';
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

/** 上传文件 */
function handleUpload() {
  formModalApi.setData(null).open();
}

/** 复制链接到剪贴板 */
const { copy } = useClipboard({ legacy: true });
async function handleCopyUrl(row: InfraFileApi.File) {
  if (!row.url) {
    message.error('文件 URL 为空');
    return;
  }

  try {
    await copy(row.url);
    message.success('复制成功');
  } catch {
    message.error('复制失败');
  }
}

/** 删除文件 */
async function handleDelete(row: InfraFileApi.File) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name || row.path]),
    duration: 0,
  });
  try {
    await deleteFile(row.id!);
    message.success(
      $t('ui.actionMessage.deleteSuccess', [row.name || row.path]),
    );
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 批量删除文件 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deletingBatch'),
    duration: 0,
  });
  try {
    await deleteFileList(checkedIds.value);
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
  records: InfraFileApi.File[];
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
          return await getFilePage({
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
  } as VxeTableGridOptions<InfraFileApi.File>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="文件列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '上传文件',
              type: 'primary',
              icon: ACTION_ICON.UPLOAD,
              onClick: handleUpload,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'primary',
              danger: true,
              icon: ACTION_ICON.DELETE,
              disabled: isEmpty(checkedIds),
              auth: ['infra:file:delete'],
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #file-content="{ row }">
        <Image v-if="row.type && row.type.includes('image')" :src="row.url" />
        <Button v-else type="link" @click="() => openWindow(row.url!)">
          {{ row.type && row.type.includes('pdf') ? '预览' : '下载' }}
        </Button>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '复制链接',
              type: 'link',
              icon: ACTION_ICON.COPY,
              onClick: handleCopyUrl.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['infra:file:delete'],
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
