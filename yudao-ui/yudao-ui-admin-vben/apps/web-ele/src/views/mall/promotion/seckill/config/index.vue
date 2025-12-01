<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallSeckillConfigApi } from '#/api/mall/promotion/seckill/seckillConfig';

import { confirm, Page, useVbenModal } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteSeckillConfig,
  getSeckillConfigPage,
  updateSeckillConfigStatus,
} from '#/api/mall/promotion/seckill/seckillConfig';
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

/** 创建秒杀时段 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑秒杀时段 */
function handleEdit(row: MallSeckillConfigApi.SeckillConfig) {
  formModalApi.setData(row).open();
}

/** 删除秒杀时段 */
async function handleDelete(row: MallSeckillConfigApi.SeckillConfig) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteSeckillConfig(row.id as number);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 修改状态 */
async function handleStatusChange(
  newStatus: number,
  row: MallSeckillConfigApi.SeckillConfig,
): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    // 二次确认
    const text = row.status === 0 ? '启用' : '停用';
    confirm({
      content: `确认要${text + row.name}吗?`,
    })
      .then(async () => {
        // 更新状态
        await updateSeckillConfigStatus(row.id, newStatus);
        // 提示并返回成功
        ElMessage.success(`${text}成功`);
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
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
          return await getSeckillConfigPage({
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
  } as VxeTableGridOptions<MallSeckillConfigApi.SeckillConfig>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="秒杀时段列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['秒杀时段']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:seckill-config:create'],
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
              auth: ['promotion:seckill-config:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:seckill-config:delete'],
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
