<script lang="ts" setup>
import type { ActionItem, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayAppApi } from '#/api/pay/app';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { CommonStatusEnum, PayChannelEnum } from '@vben/constants';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteApp, getAppPage, updateAppStatus } from '#/api/pay/app';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import AppForm from './modules/app-form.vue';
import ChannelForm from './modules/channel-form.vue';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

const [AppFormModal, appFormModalApi] = useVbenModal({
  connectedComponent: AppForm,
  destroyOnClose: true,
});

const [ChannelFormModal, channelFormModalApi] = useVbenModal({
  connectedComponent: ChannelForm,
  destroyOnClose: true,
});

/** 创建应用 */
function handleCreate() {
  appFormModalApi.setData(null).open();
}

/** 编辑应用 */
function handleEdit(row: PayAppApi.App) {
  appFormModalApi.setData({ id: row.id }).open();
}

/** 创建/编辑渠道 */
async function handleChannelForm(row: PayAppApi.App, payCode: string) {
  channelFormModalApi.setData({ appId: row.id, code: payCode }).open();
}

/** 删除应用 */
async function handleDelete(row: PayAppApi.App) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteApp(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 更新应用状态 */
async function handleStatusChange(
  newStatus: number,
  row: PayAppApi.App,
): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    const text = newStatus === CommonStatusEnum.ENABLE ? '启用' : '停用';
    confirm({
      content: `确认要${text + row.name}应用吗?`,
    })
      .then(async () => {
        // 更新状态
        await updateAppStatus({
          id: row.id!,
          status: newStatus,
        });
        // 提示并返回成功
        ElMessage.success(`${text}成功`);
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

/** 生成渠道配置按钮 */
function createChannelConfigAction(
  row: PayAppApi.App,
  channelCode: string,
): ActionItem[] {
  const exists = row.channelCodes?.includes(channelCode);
  return [
    {
      type: exists ? 'primary' : 'danger',
      size: 'small',
      icon: exists ? 'lucide:check' : 'lucide:x',
      onClick: handleChannelForm.bind(null, row, channelCode),
    },
  ];
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(handleStatusChange),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getAppPage({
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
  } as VxeTableGridOptions<PayAppApi.App>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="支付功能开启" url="https://doc.iocoder.cn/pay/build/" />
    </template>

    <AppFormModal @success="handleRefresh" />
    <ChannelFormModal @success="handleRefresh" />

    <Grid table-title="应用列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['应用']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['pay:app:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['pay:app:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['pay:app:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
      <template #alipayAppConfig="{ row }">
        <TableAction
          :actions="
            createChannelConfigAction(row, PayChannelEnum.ALIPAY_APP.code)
          "
        />
      </template>
      <template #alipayPCConfig="{ row }">
        <TableAction
          :actions="
            createChannelConfigAction(row, PayChannelEnum.ALIPAY_PC.code)
          "
        />
      </template>
      <template #alipayWAPConfig="{ row }">
        <TableAction
          :actions="
            createChannelConfigAction(row, PayChannelEnum.ALIPAY_WAP.code)
          "
        />
      </template>
      <template #alipayQrConfig="{ row }">
        <TableAction
          :actions="
            createChannelConfigAction(row, PayChannelEnum.ALIPAY_QR.code)
          "
        />
      </template>
      <template #alipayBarConfig="{ row }">
        <TableAction
          :actions="
            createChannelConfigAction(row, PayChannelEnum.ALIPAY_BAR.code)
          "
        />
      </template>
      <template #wxLiteConfig="{ row }">
        <TableAction
          :actions="createChannelConfigAction(row, PayChannelEnum.WX_LITE.code)"
        />
      </template>
      <template #wxPubConfig="{ row }">
        <TableAction
          :actions="createChannelConfigAction(row, PayChannelEnum.WX_PUB.code)"
        />
      </template>
      <template #wxAppConfig="{ row }">
        <TableAction
          :actions="createChannelConfigAction(row, PayChannelEnum.WX_APP.code)"
        />
      </template>
      <template #wxNativeConfig="{ row }">
        <TableAction
          :actions="
            createChannelConfigAction(row, PayChannelEnum.WX_NATIVE.code)
          "
        />
      </template>
      <template #wxWapConfig="{ row }">
        <TableAction
          :actions="createChannelConfigAction(row, PayChannelEnum.WX_WAP.code)"
        />
      </template>
      <template #wxBarConfig="{ row }">
        <TableAction
          :actions="createChannelConfigAction(row, PayChannelEnum.WX_BAR.code)"
        />
      </template>
      <template #walletConfig="{ row }">
        <TableAction
          :actions="createChannelConfigAction(row, PayChannelEnum.WALLET.code)"
        />
      </template>
      <template #mockConfig="{ row }">
        <TableAction
          :actions="createChannelConfigAction(row, PayChannelEnum.MOCK.code)"
        />
      </template>
    </Grid>
  </Page>
</template>
