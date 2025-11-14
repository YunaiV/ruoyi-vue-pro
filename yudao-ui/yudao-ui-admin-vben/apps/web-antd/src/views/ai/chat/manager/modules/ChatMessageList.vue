<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiChatConversationApi } from '#/api/ai/chat/conversation';
import type { SystemUserApi } from '#/api/system/user';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteChatMessageByAdmin,
  getChatMessagePage,
} from '#/api/ai/chat/message';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';

import { useGridColumnsMessage, useGridFormSchemaMessage } from '../data';

const userList = ref<SystemUserApi.User[]>([]); // 用户列表
/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除 */
async function handleDelete(row: AiChatConversationApi.ChatConversation) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteChatMessageByAdmin(row.id as number);
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
    schema: useGridFormSchemaMessage(),
  },
  gridOptions: {
    columns: useGridColumnsMessage(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getChatMessagePage({
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
  } as VxeTableGridOptions<AiChatConversationApi.ChatConversation>,
  separator: false,
});
onMounted(async () => {
  // 获得用户列表
  userList.value = await getSimpleUserList();
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="消息列表">
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
              auth: ['ai:chat-message:delete'],
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
