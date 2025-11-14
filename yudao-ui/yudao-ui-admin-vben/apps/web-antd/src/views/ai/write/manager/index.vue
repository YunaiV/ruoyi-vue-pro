<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiWriteApi } from '#/api/ai/write';
import type { SystemUserApi } from '#/api/system/user';

import { onMounted, ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteWrite, getWritePage } from '#/api/ai/write';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

const userList = ref<SystemUserApi.User[]>([]); // 用户列表
/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除 */
async function handleDelete(row: AiWriteApi.AiWritePageReq) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteWrite(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
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
          return await getWritePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<AiWriteApi.AiWritePageReq>,
});
onMounted(async () => {
  // 获得下拉数据
  userList.value = await getSimpleUserList();
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 写作助手" url="https://doc.iocoder.cn/ai/write/" />
    </template>
    <Grid table-title="写作管理列表">
      <template #toolbar-tools>
        <TableAction :actions="[]" />
      </template>
      <template #userId="{ row }">
        <span>{{
          userList.find((item) => item.id === row.userId)?.nickname
        }}</span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['ai:write:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.id]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
