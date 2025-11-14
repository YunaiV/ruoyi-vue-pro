<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemOAuth2TokenApi } from '#/api/system/oauth2/token';

import { ref } from 'vue';

import { confirm, DocAlert, Page } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteOAuth2Token,
  getOAuth2TokenPage,
} from '#/api/system/oauth2/token';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除 OAuth2 令牌 */
async function handleDelete(row: SystemOAuth2TokenApi.OAuth2Token) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', ['令牌']),
    duration: 0,
  });
  try {
    await deleteOAuth2Token(row.accessToken);
    message.success($t('ui.actionMessage.deleteSuccess', ['令牌']));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 批量删除 OAuth2 令牌 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deletingBatch'),
    duration: 0,
  });
  try {
    await deleteOAuth2Token(checkedIds.value.join(','));
    checkedIds.value = [];
    message.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const checkedIds = ref<string[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: SystemOAuth2TokenApi.OAuth2Token[];
}) {
  checkedIds.value = records.map((item) => item.accessToken);
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
          return await getOAuth2TokenPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'accessToken',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<SystemOAuth2TokenApi.OAuth2Token>,
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
        title="OAuth 2.0（SSO 单点登录）"
        url="https://doc.iocoder.cn/oauth2/"
      />
    </template>

    <Grid table-title="令牌列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'primary',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['system:oauth2-token:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['system:oauth2-token:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', ['令牌']),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
