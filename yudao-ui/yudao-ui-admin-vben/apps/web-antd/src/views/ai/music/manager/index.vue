<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiMusicApi } from '#/api/ai/music';
import type { SystemUserApi } from '#/api/system/user';

import { onMounted, ref } from 'vue';

import { confirm, DocAlert, Page } from '@vben/common-ui';
import { AiMusicStatusEnum } from '@vben/constants';

import { Button, message, Switch } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMusic, getMusicPage, updateMusic } from '#/api/ai/music';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

const userList = ref<SystemUserApi.User[]>([]); // 用户列表
/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除 */
async function handleDelete(row: AiMusicApi.Music) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteMusic(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}
/** 修改是否发布 */
const handleUpdatePublicStatusChange = async (row: AiMusicApi.Music) => {
  try {
    // 修改状态的二次确认
    const text = row.publicStatus ? '公开' : '私有';
    await confirm(`确认要"${text}"该图片吗?`).then(async () => {
      await updateMusic({
        id: row.id,
        publicStatus: row.publicStatus,
      });
      handleRefresh();
    });
  } catch {
    row.publicStatus = !row.publicStatus;
  }
};
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
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<AiMusicApi.Music>,
});
onMounted(async () => {
  // 获得下拉数据
  userList.value = await getSimpleUserList();
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

      <template #userId="{ row }">
        <span>
          {{ userList.find((item) => item.id === row.userId)?.nickname }}
        </span>
      </template>
      <template #content="{ row }">
        <Button
          type="link"
          v-if="row.audioUrl?.length > 0"
          :href="row.audioUrl"
          target="_blank"
          class="p-0"
        >
          音乐
        </Button>
        <Button
          type="link"
          v-if="row.videoUrl?.length > 0"
          :href="row.videoUrl"
          target="_blank"
          class="p-0 !pl-1"
        >
          视频
        </Button>
        <Button
          type="link"
          v-if="row.imageUrl?.length > 0"
          :href="row.imageUrl"
          target="_blank"
          class="p-0 !pl-1"
        >
          封面
        </Button>
      </template>
      <template #publicStatus="{ row }">
        <Switch
          v-model:checked="row.publicStatus"
          @change="handleUpdatePublicStatusChange(row)"
          :disabled="row.status !== AiMusicStatusEnum.SUCCESS"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
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
