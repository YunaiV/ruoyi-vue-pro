<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallPointActivityApi } from '#/api/mall/promotion/point';

import { computed } from 'vue';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  closePointActivity,
  deletePointActivity,
  getPointActivityPage,
} from '#/api/mall/promotion/point';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import PointActivityForm from './modules/form.vue';

defineOptions({ name: 'PromotionPointActivity' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: PointActivityForm,
  destroyOnClose: true,
});

/** 获得商品已兑换数量 */
const getRedeemedQuantity = computed(
  () => (row: MallPointActivityApi.PointActivity) =>
    (row.totalStock || 0) - (row.stock || 0),
);

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建积分活动 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑积分活动 */
function handleEdit(row: MallPointActivityApi.PointActivity) {
  formModalApi.setData(row).open();
}

/** 关闭积分活动 */
async function handleClose(row: MallPointActivityApi.PointActivity) {
  await confirm('确认关闭该积分商城活动吗？');
  await closePointActivity(row.id);
  ElMessage.success('关闭成功');
  handleRefresh();
}

/** 删除积分活动 */
async function handleDelete(row: MallPointActivityApi.PointActivity) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.spuName]),
  });
  try {
    await deletePointActivity(row.id);
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
          return await getPointActivityPage({
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
  } as VxeTableGridOptions<MallPointActivityApi.PointActivity>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【营销】积分商城活动"
        url="https://doc.iocoder.cn/mall/promotion-point/"
      />
    </template>

    <FormModal @success="handleRefresh" />

    <Grid table-title="积分商城活动列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['积分活动']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:point-activity:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #redeemedQuantity="{ row }">
        {{ getRedeemedQuantity(row) }}
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['promotion:point-activity:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '关闭',
              type: 'danger',
              link: true,
              auth: ['promotion:point-activity:close'],
              ifShow: row.status === 0,
              onClick: handleClose.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:point-activity:delete'],
              ifShow: row.status !== 0,
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.spuName]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
