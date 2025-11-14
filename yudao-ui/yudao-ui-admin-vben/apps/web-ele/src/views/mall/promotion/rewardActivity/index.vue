<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallRewardActivityApi } from '#/api/mall/promotion/reward/rewardActivity';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  closeRewardActivity,
  deleteRewardActivity,
  getRewardActivityPage,
} from '#/api/mall/promotion/reward/rewardActivity';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import RewardActivityForm from './modules/form.vue';

defineOptions({ name: 'PromotionRewardActivity' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: RewardActivityForm,
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
  formModalApi.setData(row).open();
}

/** 关闭活动 */
async function handleClose(row: MallRewardActivityApi.RewardActivity) {
  await confirm('确认关闭该满减送活动吗？');
  await closeRewardActivity(row.id as number);
  ElMessage.success('关闭成功');
  handleRefresh();
}

/** 删除活动 */
async function handleDelete(row: MallRewardActivityApi.RewardActivity) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteRewardActivity(row.id as number);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
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
    <template #doc>
      <DocAlert
        title="【营销】满减送"
        url="https://doc.iocoder.cn/mall/promotion-record/"
      />
    </template>

    <FormModal @success="handleRefresh" />

    <Grid table-title="满减送活动列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['满减送活动']),
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
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['promotion:reward-activity:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '关闭',
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:reward-activity:close'],
              ifShow: row.status === 0,
              onClick: handleClose.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:reward-activity:delete'],
              ifShow: row.status !== 0,
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
