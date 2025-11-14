<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmOALeaveApi } from '#/api/bpm/oa/leave';

import { h } from 'vue';

import { DocAlert, Page, prompt } from '@vben/common-ui';

import { message, Textarea } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getLeavePage } from '#/api/bpm/oa/leave';
import { cancelProcessInstanceByStartUser } from '#/api/bpm/processInstance';
import { router } from '#/router';

import { GridFormSchema, useGridColumns } from './data';

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: GridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getLeavePage({
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
  } as VxeTableGridOptions<BpmOALeaveApi.Leave>,
});

/** 创建请假 */
function handleCreate() {
  router.push({
    name: 'OALeaveCreate',
    query: {
      formType: 'create',
    },
  });
}

/** 查看请假详情 */
function handleDetail(row: BpmOALeaveApi.Leave) {
  router.push({
    name: 'OALeaveDetail',
    query: { id: row.id },
  });
}

/** 取消请假 */
function handleCancel(row: BpmOALeaveApi.Leave) {
  prompt({
    async beforeClose(scope) {
      if (scope.isConfirm) {
        if (scope.value) {
          try {
            await cancelProcessInstanceByStartUser(row.id, scope.value);
            message.success('取消成功');
            handleRefresh();
          } catch {
            return false;
          }
        } else {
          message.error('请输入取消原因');
          return false;
        }
      }
    },
    component: () => {
      return h(Textarea, {
        placeholder: '请输入取消原因',
        allowClear: true,
        rows: 2,
        rules: [{ required: true, message: '请输入取消原因' }],
      });
    },
    content: '请输入取消原因',
    title: '取消流程',
    modelPropName: 'value',
  });
}

/** 审批进度 */
function handleProgress(row: BpmOALeaveApi.Leave) {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.processInstanceId },
  });
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="审批接入（业务表单）"
        url="https://doc.iocoder.cn/bpm/use-business-form/"
      />
    </template>

    <Grid table-title="请假列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '发起请假',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['bpm:oa-leave:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.detail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:oa-leave:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '审批进度',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:oa-leave:query'],
              onClick: handleProgress.bind(null, row),
            },
            {
              label: '取消',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['bpm:user-group:query'],
              popConfirm: {
                title: '取消流程',
                confirm: handleCancel.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
