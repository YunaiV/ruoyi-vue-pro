<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallRewardActivityApi } from '#/api/mall/promotion/reward/rewardActivity';

import { Page, useVbenModal } from '@vben/common-ui';
import { CommonStatusEnum } from '@vben/constants';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  closeRewardActivity,
  deleteRewardActivity,
  getRewardActivityPage,
} from '#/api/mall/promotion/reward/rewardActivity';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

defineOptions({ name: 'PromotionRewardActivity' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建满减送活动 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑满减送活动 */
function handleEdit(row: MallRewardActivityApi.RewardActivity) {
  formModalApi.setData({ id: row.id }).open();
}

/** 关闭满减送活动 */
async function handleClose(row: MallRewardActivityApi.RewardActivity) {
  // TODO @puhui999：这个国际化，需要加下哈；closing、closeSuccess；
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.closing', [row.name]),
    duration: 0,
  });
  try {
    await closeRewardActivity(row.id!);
    message.success($t('ui.actionMessage.closeSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 删除满减送活动 */
async function handleDelete(row: MallRewardActivityApi.RewardActivity) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteRewardActivity(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
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
          return await getRewardActivityPage({
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
  } as VxeTableGridOptions<MallRewardActivityApi.RewardActivity>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="满减送活动">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['活动']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:reward-activity:create'],
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
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['promotion:reward-activity:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '关闭',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.CLOSE,
              auth: ['promotion:reward-activity:close'],
              ifShow: row.status === CommonStatusEnum.ENABLE,
              popConfirm: {
                title: '确认关闭该满减送活动吗？',
                confirm: handleClose.bind(null, row),
              },
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:reward-activity:delete'],
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
