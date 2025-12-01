<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpMessageTemplateApi } from '#/api/mp/messageTemplate';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteMessageTemplate,
  getMessageTemplateList,
  syncMessageTemplate,
} from '#/api/mp/messageTemplate';
import { $t } from '#/locales';
import { WxAccountSelect } from '#/views/mp/components';

import { useGridColumns, useGridFormSchema } from './data';
import SendForm from './modules/send-form.vue';

const [SendFormModal, sendFormModalApi] = useVbenModal({
  connectedComponent: SendForm,
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

/** 同步模板 */
async function handleSync() {
  const formValues = await gridApi.formApi.getValues();
  const accountId = formValues.accountId;
  if (!accountId) {
    message.warning('请先选择公众号');
    return;
  }

  await confirm('是否确认同步消息模板？');
  const hideLoading = message.loading({
    content: '正在同步消息模板...',
    duration: 0,
  });
  try {
    await syncMessageTemplate(accountId);
    message.success('同步消息模板成功');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 发送消息 */
function handleSend(row: MpMessageTemplateApi.MessageTemplate) {
  sendFormModalApi.setData(row).open();
}

/** 删除模板 */
async function handleDelete(row: MpMessageTemplateApi.MessageTemplate) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.title]),
    duration: 0,
  });
  try {
    await deleteMessageTemplate(row.id);
    message.success($t('ui.actionMessage.deleteSuccess', [row.title]));
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
        query: async (_params, formValues) => {
          return await getMessageTemplateList({
            accountId: formValues.accountId,
          });
        },
      },
      autoLoad: false,
    },
    pagerConfig: {
      enabled: false,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MpMessageTemplateApi.MessageTemplate>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="模版消息"
        url="https://doc.iocoder.cn/mp/message-template/"
      />
    </template>

    <SendFormModal @success="handleRefresh" />
    <Grid table-title="公众号消息模板列表">
      <template #form-accountId>
        <WxAccountSelect @change="handleAccountChange" />
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '同步',
              type: 'primary',
              icon: 'lucide:refresh-ccw',
              auth: ['mp:message-template:sync'],
              onClick: handleSync,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '发送',
              type: 'link',
              icon: 'lucide:send',
              auth: ['mp:message-template:send'],
              onClick: handleSend.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mp:message-template:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.title]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
