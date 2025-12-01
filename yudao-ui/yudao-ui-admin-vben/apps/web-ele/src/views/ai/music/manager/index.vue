<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiMusicApi } from '#/api/ai/music';

import { confirm, DocAlert, Page } from '@vben/common-ui';

import { ElButton, ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMusic, getMusicPage, updateMusic } from '#/api/ai/music';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除音乐记录 */
async function handleDelete(row: AiMusicApi.Music) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteMusic(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 修改是否发布 */
async function handleUpdatePublicStatusChange(
  newStatus: boolean,
  row: AiMusicApi.Music,
): Promise<boolean | undefined> {
  const text = newStatus ? '公开' : '私有';
  return new Promise((resolve, reject) => {
    confirm({
      content: `确认要将该音乐切换为【${text}】吗？`,
    })
      .then(async () => {
        // 更新音乐状态
        await updateMusic({
          id: row.id,
          publicStatus: newStatus,
        });
        // 提示并返回成功
        ElMessage.success($t('ui.actionMessage.operationSuccess'));
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
          return await getMusicPage({
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
  } as VxeTableGridOptions<AiMusicApi.Music>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 音乐创作" url="https://doc.iocoder.cn/ai/music/" />
    </template>
    <Grid table-title="音乐管理列表">
      <template #toolbar-tools>
        <TableAction :actions="[]" />
      </template>

      <template #content="{ row }">
        <ElButton
          type="primary"
          link
          v-if="row.audioUrl?.length > 0"
          :href="row.audioUrl"
          target="_blank"
          class="p-0"
        >
          音乐
        </ElButton>
        <ElButton
          type="primary"
          link
          v-if="row.videoUrl?.length > 0"
          :href="row.videoUrl"
          target="_blank"
          class="p-0 !pl-1"
        >
          视频
        </ElButton>
        <ElButton
          type="primary"
          link
          v-if="row.imageUrl?.length > 0"
          :href="row.imageUrl"
          target="_blank"
          class="p-0 !pl-1"
        >
          封面
        </ElButton>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['ai:music:delete'],
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
