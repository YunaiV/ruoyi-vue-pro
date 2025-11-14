<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallDiscountActivityApi } from '#/api/mall/promotion/discount/discountActivity';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  closeDiscountActivity,
  deleteDiscountActivity,
  getDiscountActivityPage,
} from '#/api/mall/promotion/discount/discountActivity';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import DiscountActivityForm from './modules/form.vue';

defineOptions({ name: 'PromotionDiscountActivity' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: DiscountActivityForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建满减活动 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑满减活动 */
function handleEdit(row: MallDiscountActivityApi.DiscountActivity) {
  formModalApi.setData(row).open();
}

/** 关闭满减活动 */
async function handleClose(row: MallDiscountActivityApi.DiscountActivity) {
  try {
    await confirm({
      content: '确认关闭该限时折扣活动吗？',
    });
  } catch {
    return;
  }

  const hideLoading = message.loading({
    content: '正在关闭中',
    duration: 0,
  });
  try {
    await closeDiscountActivity(row.id as number);
    message.success({
      content: '关闭成功',
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 删除满减活动 */
async function handleDelete(row: MallDiscountActivityApi.DiscountActivity) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteDiscountActivity(row.id as number);
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
          return await getDiscountActivityPage({
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
  } as VxeTableGridOptions<MallDiscountActivityApi.DiscountActivity>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【营销】限时折扣"
        url="https://doc.iocoder.cn/mall/promotion-discount/"
      />
    </template>

    <FormModal @success="handleRefresh" />

    <Grid table-title="限时折扣活动列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['限时折扣活动']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:discount-activity:create'],
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
              auth: ['promotion:discount-activity:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '关闭',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:discount-activity:close'],
              ifShow: row.status === 0,
              onClick: handleClose.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:discount-activity:delete'],
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
