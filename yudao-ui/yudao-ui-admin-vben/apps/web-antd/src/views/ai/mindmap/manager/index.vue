<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiMindmapApi } from '#/api/ai/mindmap';
import type { SystemUserApi } from '#/api/system/user';

import { nextTick, onMounted, ref } from 'vue';

import { DocAlert, Page, useVbenDrawer } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMindMap, getMindMapPage } from '#/api/ai/mindmap';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';

import Right from '../index/modules/Right.vue';
import { useGridColumns, useGridFormSchema } from './data';

const userList = ref<SystemUserApi.User[]>([]); // 用户列表
const previewVisible = ref(false); // drawer 的显示隐藏
const previewContent = ref('');
const [Drawer, drawerApi] = useVbenDrawer({
  header: false,
  footer: false,
  destroyOnClose: true,
});
/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除 */
async function handleDelete(row: AiMindmapApi.MindMap) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteMindMap(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
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
          return await getMindMapPage({
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
  } as VxeTableGridOptions<AiMindmapApi.MindMap>,
});
async function openPreview(row: AiMindmapApi.MindMap) {
  previewVisible.value = false;
  drawerApi.open();
  await nextTick();
  previewVisible.value = true;
  previewContent.value = row.generatedContent;
}
onMounted(async () => {
  // 获得下拉数据
  userList.value = await getSimpleUserList();
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 思维导图" url="https://doc.iocoder.cn/ai/mindmap/" />
    </template>
    <Drawer class="w-3/5">
      <Right
        v-if="previewVisible"
        :generated-content="previewContent"
        :is-end="true"
        :is-generating="false"
        :is-start="false"
      />
    </Drawer>
    <Grid table-title="思维导图管理列表">
      <template #toolbar-tools>
        <TableAction :actions="[]" />
      </template>
      <template #userId="{ row }">
        <span>
          {{
            userList.find((item: SystemUserApi.User) => item.id === row.userId)
              ?.nickname
          }}
        </span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('ui.cropper.preview'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['ai:api-key:update'],
              onClick: openPreview.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['ai:mind-map:delete'],
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
