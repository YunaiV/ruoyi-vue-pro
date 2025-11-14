<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBargainActivityApi } from '#/api/mall/promotion/bargain/bargainActivity';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  closeBargainActivity,
  deleteBargainActivity,
  getBargainActivityPage,
} from '#/api/mall/promotion/bargain/bargainActivity';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

defineOptions({ name: 'PromotionBargainActivity' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建砍价活动 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑砍价活动 */
function handleEdit(row: MallBargainActivityApi.BargainActivity) {
  formModalApi.setData(row).open();
}

/** 关闭砍价活动 */
async function handleClose(row: MallBargainActivityApi.BargainActivity) {
  try {
    await confirm({
      content: '确认关闭该砍价活动吗？',
    });
  } catch {
    return;
  }

  const hideLoading = message.loading({
    content: '确认关闭该砍价活动吗？',
    duration: 0,
  });
  try {
    await closeBargainActivity(row.id as number);
    message.success({
      content: '关闭成功',
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 删除砍价活动 */
async function handleDelete(row: MallBargainActivityApi.BargainActivity) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteBargainActivity(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
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
          return await getBargainActivityPage({
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
  } as VxeTableGridOptions<MallBargainActivityApi.BargainActivity>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【营销】砍价活动"
        url="https://doc.iocoder.cn/mall/promotion-bargain/"
      />
    </template>

    <FormModal @success="handleRefresh" />

    <Grid table-title="砍价活动列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['砍价活动']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:bargain-activity:create'],
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
              auth: ['promotion:bargain-activity:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '关闭',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:bargain-activity:close'],
              ifShow: row.status === 0,
              onClick: handleClose.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:bargain-activity:delete'],
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
