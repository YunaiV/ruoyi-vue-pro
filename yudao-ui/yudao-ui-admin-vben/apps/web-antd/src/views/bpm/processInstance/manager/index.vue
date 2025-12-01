<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';

import { h } from 'vue';

import { DocAlert, Page, prompt } from '@vben/common-ui';
import { BpmProcessInstanceStatus } from '@vben/constants';

import { Button, message, Textarea } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelProcessInstanceByAdmin,
  getProcessInstanceManagerPage,
} from '#/api/bpm/processInstance';
import { $t } from '#/locales';
import { router } from '#/router';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'BpmProcessInstanceManager' });

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 查看任务详情 */
function handleTaskDetail(
  row: BpmProcessInstanceApi.ProcessInstance,
  task: BpmProcessInstanceApi.Task,
) {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.id, taskId: task.id },
  });
}

/** 查看流程实例 */
function handleDetail(row: BpmProcessInstanceApi.ProcessInstance) {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.id },
  });
}

/** 取消流程实例 */
function handleCancel(row: BpmProcessInstanceApi.ProcessInstance) {
  prompt({
    component: () => {
      return h(Textarea, {
        placeholder: '请输入取消原因',
        allowClear: true,
        rows: 2,
      });
    },
    content: '请输入取消原因',
    title: '取消流程',
    modelPropName: 'value',
  }).then(async (reason) => {
    if (reason) {
      await cancelProcessInstanceByAdmin(row.id, reason);
      message.success('取消成功');
      handleRefresh();
    }
  });
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
          return await getProcessInstanceManagerPage({
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
  } as VxeTableGridOptions<BpmProcessInstanceApi.ProcessInstance>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="工作流手册" url="https://doc.iocoder.cn/bpm" />
    </template>

    <Grid table-title="流程实例">
      <template #tasks="{ row }">
        <template v-if="row.tasks && row.tasks.length > 0">
          <Button
            v-for="task in row.tasks"
            :key="task.id"
            type="link"
            @click="handleTaskDetail(row, task)"
          >
            {{ task.name }}
          </Button>
        </template>
        <span v-else>-</span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.detail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:process-instance:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: $t('ui.actionTitle.cancel'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              ifShow: row.status === BpmProcessInstanceStatus.RUNNING,
              auth: ['bpm:process-instance:cancel'],
              onClick: handleCancel.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
