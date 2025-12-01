<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiChatConversationApi } from '#/api/ai/chat/conversation';

import { Page } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteChatConversationByAdmin,
  getChatConversationPage,
} from '#/api/ai/chat/conversation';
import { $t } from '#/locales';

import {
  useGridColumnsConversation,
  useGridFormSchemaConversation,
} from '../data';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除对话 */
async function handleDelete(row: AiChatConversationApi.ChatConversation) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteChatConversationByAdmin(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchemaConversation(),
  },
  gridOptions: {
    columns: useGridColumnsConversation(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getChatConversationPage({
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
  } as VxeTableGridOptions<AiChatConversationApi.ChatConversation>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="对话列表">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['ai:chat-conversation:delete'],
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
