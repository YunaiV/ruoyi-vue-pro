<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiImageApi } from '#/api/ai/image';
import type { SystemUserApi } from '#/api/system/user';

import { onMounted, ref } from 'vue';

import { confirm, DocAlert, Page } from '@vben/common-ui';
import { AiImageStatusEnum } from '@vben/constants';

import { Image, message, Switch } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteImage, getImagePage, updateImage } from '#/api/ai/image';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

const userList = ref<SystemUserApi.User[]>([]); // 用户列表
/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除 */
async function handleDelete(row: AiImageApi.Image) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteImage(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}
/** 修改是否发布 */
const handleUpdatePublicStatusChange = async (row: AiImageApi.Image) => {
  try {
    // 修改状态的二次确认
    const text = row.publicStatus ? '公开' : '私有';
    await confirm(`确认要"${text}"该图片吗?`).then(async () => {
      await updateImage({
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
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<AiImageApi.Image>,
});
onMounted(async () => {
  // 获得下拉数据
  userList.value = await getSimpleUserList();
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 绘图创作" url="https://doc.iocoder.cn/ai/image/" />
    </template>
    <Grid table-title="绘画管理列表">
      <template #toolbar-tools>
        <TableAction :actions="[]" />
      </template>
      <template #picUrl="{ row }">
        <Image :src="row.picUrl" class="h-20 w-20" />
      </template>
      <template #userId="{ row }">
        <span>
          {{
            userList.find((item: SystemUserApi.User) => item.id === row.userId)
              ?.nickname
          }}
        </span>
      </template>
      <template #publicStatus="{ row }">
        <Switch
          v-model:checked="row.publicStatus"
          @change="handleUpdatePublicStatusChange(row)"
          :disabled="row.status !== AiImageStatusEnum.SUCCESS"
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
