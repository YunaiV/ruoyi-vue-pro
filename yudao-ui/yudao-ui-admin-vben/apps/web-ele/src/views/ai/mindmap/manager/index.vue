<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiMindmapApi } from '#/api/ai/mindmap';

import { DocAlert, Page, useVbenDrawer } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMindMap, getMindMapPage } from '#/api/ai/mindmap';
import { $t } from '#/locales';

import Right from '../index/modules/right.vue';
import { useGridColumns, useGridFormSchema } from './data';

const [Drawer, drawerApi] = useVbenDrawer({
  header: false,
  footer: false,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除思维导图记录 */
async function handleDelete(row: AiMindmapApi.MindMap) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteMindMap(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 预览思维导图 */
async function openPreview(row: AiMindmapApi.MindMap) {
  drawerApi.setData(row.generatedContent).open();
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
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<AiMindmapApi.MindMap>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 思维导图" url="https://doc.iocoder.cn/ai/mindmap/" />
    </template>

    <Drawer class="w-3/5">
      <Right
        :generated-content="drawerApi.getData() as any"
        :is-end="true"
        :is-generating="false"
        :is-start="false"
      />
    </Drawer>
    <Grid table-title="思维导图管理列表">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('ui.cropper.preview'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['ai:api-key:update'],
              disabled: !row.generatedContent,
              onClick: openPreview.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
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
