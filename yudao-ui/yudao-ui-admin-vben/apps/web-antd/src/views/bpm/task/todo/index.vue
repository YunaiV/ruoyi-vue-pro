<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmTaskApi } from '#/api/bpm/task';

import { DocAlert, Page } from '@vben/common-ui';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTaskTodoPage } from '#/api/bpm/task';
import { router } from '#/router';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'BpmTodoTask' });

/** 办理任务 */
function handleAudit(row: BpmTaskApi.Task) {
  console.warn(row);
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstance.id,
      taskId: row.id,
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
          return await getTaskTodoPage({
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
  } as VxeTableGridOptions<BpmTaskApi.Task>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="审批通过、不通过、驳回"
        url="https://doc.iocoder.cn/bpm/task-todo-done/"
      />
      <DocAlert title="审批加签、减签" url="https://doc.iocoder.cn/bpm/sign/" />
      <DocAlert
        title="审批转办、委派、抄送"
        url="https://doc.iocoder.cn/bpm/task-delegation-and-cc/"
      />
      <DocAlert title="审批加签、减签" url="https://doc.iocoder.cn/bpm/sign/" />
    </template>

    <Grid table-title="待办任务">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '办理',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:task:query'],
              onClick: handleAudit.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
