<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmTaskApi } from '#/api/bpm/task';

import { DocAlert, Page } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTaskDonePage, withdrawTask } from '#/api/bpm/task';
import { router } from '#/router';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'BpmDoneTask' });

/** 查看历史 */
function handleHistory(row: BpmTaskApi.TaskManager) {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstance.id,
      taskId: row.id,
    },
  });
}

/** 撤回任务 */
async function handleWithdraw(row: BpmTaskApi.TaskManager) {
  const loadingInstance = ElLoading.service({
    text: '正在撤回中...',
  });
  try {
    await withdrawTask(row.id);
    ElMessage.success('撤回成功');
    await gridApi.query();
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
          return await getTaskDonePage({
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
  } as VxeTableGridOptions<BpmTaskApi.TaskManager>,
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

    <Grid table-title="已办任务">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '撤回',
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              popConfirm: {
                title: '确定要撤回该任务吗？',
                confirm: handleWithdraw.bind(null, row),
              },
            },
            {
              label: '历史',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              onClick: handleHistory.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
