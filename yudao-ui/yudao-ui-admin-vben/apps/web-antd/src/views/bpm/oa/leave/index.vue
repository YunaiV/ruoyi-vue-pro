<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmOALeaveApi } from '#/api/bpm/oa/leave';

import { h } from 'vue';

import { DocAlert, Page, prompt } from '@vben/common-ui';
import { BpmProcessInstanceStatus } from '@vben/constants';

import { message, Textarea } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getLeavePage } from '#/api/bpm/oa/leave';
import { cancelProcessInstanceByStartUser } from '#/api/bpm/processInstance';
import { $t } from '#/locales';
import { router } from '#/router';

import { useGridColumns, useGridFormSchema } from './data';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建请假 */
function handleCreate() {
  router.push({
    name: 'OALeaveCreate',
    query: {
      formType: 'create',
    },
  });
}

/** 取消请假 */
function handleCancel(row: BpmOALeaveApi.Leave) {
  prompt({
    title: '取消流程',
    content: '请输入取消原因',
    modelPropName: 'value',
    component: () => {
      return h(Textarea, {
        placeholder: '请输入取消原因',
        allowClear: true,
        rows: 2,
        rules: [{ required: true, message: '请输入取消原因' }],
      });
    },
    async beforeClose(scope) {
      if (!scope.isConfirm) {
        return;
      }
      if (!scope.value) {
        message.error('请输入取消原因');
        return false;
      }
      const hideLoading = message.loading({
        content: '正在取消中...',
        duration: 0,
      });
      try {
        await cancelProcessInstanceByStartUser(row.id, scope.value);
        message.success('取消成功');
        handleRefresh();
      } catch {
        return false;
      } finally {
        hideLoading();
      }
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

/** 审批进度 */
function handleProgress(row: BpmOALeaveApi.Leave) {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.processInstanceId },
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
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<BpmOALeaveApi.Leave>,
});
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
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '审批进度',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              onClick: handleProgress.bind(null, row),
            },
            {
              label: '取消',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              ifShow: row.result === BpmProcessInstanceStatus.RUNNING,
              onClick: handleCancel.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
