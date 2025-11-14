<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemSmsChannelApi } from '#/api/system/sms/channel';

import { ref } from 'vue';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteSmsChannel,
  deleteSmsChannelList,
  getSmsChannelPage,
} from '#/api/system/sms/channel';
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

/** 创建短信渠道 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑短信渠道 */
function handleEdit(row: SystemSmsChannelApi.SmsChannel) {
  formModalApi.setData(row).open();
}

/** 删除短信渠道 */
async function handleDelete(row: SystemSmsChannelApi.SmsChannel) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.signature]),
    duration: 0,
  });
  try {
    await deleteSmsChannel(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.signature]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 批量删除短信渠道 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deletingBatch'),
    duration: 0,
  });
  try {
    await deleteSmsChannelList(checkedIds.value);
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
  records: SystemSmsChannelApi.SmsChannel[];
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
          return await getSmsChannelPage({
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
  } as VxeTableGridOptions<SystemSmsChannelApi.SmsChannel>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="短信配置" url="https://doc.iocoder.cn/sms/" />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="短信渠道列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['短信渠道']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['system:sms-channel:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'primary',
              danger: true,
              icon: ACTION_ICON.DELETE,
              disabled: isEmpty(checkedIds),
              auth: ['system:sms-channel:delete'],
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
              auth: ['system:sms-channel:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['system:sms-channel:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.signature]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
