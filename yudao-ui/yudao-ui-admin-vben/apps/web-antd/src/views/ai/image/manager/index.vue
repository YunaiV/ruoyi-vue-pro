<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiImageApi } from '#/api/ai/image';

import { confirm, DocAlert, Page } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteImage, getImagePage, updateImage } from '#/api/ai/image';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除图片 */
async function handleDelete(row: AiImageApi.Image) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteImage(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 修改是否发布 */
async function handleUpdatePublicStatusChange(
  newStatus: boolean,
  row: AiImageApi.Image,
): Promise<boolean | undefined> {
  const text = newStatus ? '公开' : '私有';
  return new Promise((resolve, reject) => {
    confirm({
      content: `确认要将该图片切换为【${text}】吗？`,
    })
      .then(async () => {
        // 更新图片状态
        await updateImage({
          id: row.id,
          publicStatus: newStatus,
        });
        // 提示并返回成功
        message.success($t('ui.actionMessage.operationSuccess'));
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
    columns: useGridColumns(handleUpdatePublicStatusChange),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getImagePage({
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
  } as VxeTableGridOptions<AiImageApi.Image>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 绘图创作" url="https://doc.iocoder.cn/ai/image/" />
    </template>
    <Grid table-title="绘画管理列表">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['ai:image:delete'],
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
