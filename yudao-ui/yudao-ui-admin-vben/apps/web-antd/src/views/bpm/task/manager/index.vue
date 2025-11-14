<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmTaskApi } from '#/api/bpm/task';

import { DocAlert, Page } from '@vben/common-ui';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTaskManagerPage } from '#/api/bpm/task';
import { router } from '#/router';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'BpmManagerTask' });

/** 查看历史 */
function handleHistory(row: BpmTaskApi.TaskManager) {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstance.id,
    },
  });
}

const [Grid] = useVbenVxeGrid({
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
          return await getTaskManagerPage({
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
    cellConfig: {
      height: 64,
    },
  } as VxeTableGridOptions<BpmTaskApi.TaskManager>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="工作流手册" url="https://doc.iocoder.cn/bpm/" />
    </template>
    <Grid table-title="流程任务">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '历史',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:task:query'],
              onClick: handleHistory.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
