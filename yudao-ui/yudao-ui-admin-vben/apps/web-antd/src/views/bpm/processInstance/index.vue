<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmTaskApi } from '#/api/bpm/task';

import { h } from 'vue';

import { DocAlert, Page, prompt } from '@vben/common-ui';
import { BpmProcessInstanceStatus, DICT_TYPE } from '@vben/constants';

import { Button, message, Textarea } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelProcessInstanceByStartUser,
  getProcessInstanceMyPage,
} from '#/api/bpm/processInstance';
import { DictTag } from '#/components/dict-tag';
import { router } from '#/router';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'BpmProcessInstanceMy' });

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 查看流程实例 */
function handleDetail(row: BpmTaskApi.Task) {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.id },
  });
}

/** 取消流程实例 */
function handleCancel(row: BpmTaskApi.Task) {
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
          return await getProcessInstanceMyPage({
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
        title="流程发起、取消、重新发起"
        url="https://doc.iocoder.cn/bpm/process-instance"
      />
    </template>

    <Grid table-title="流程状态">
      <!-- 摘要 -->
      <template #slot-summary="{ row }">
        <div
          class="flex flex-col py-2"
          v-if="row.summary && row.summary.length > 0"
        >
          <div v-for="(item, index) in row.summary" :key="index">
            <span class="text-gray-500">
              {{ item.key }} : {{ item.value }}
            </span>
          </div>
        </div>
        <div v-else>-</div>
      </template>

      <template #slot-status="{ row }">
        <!-- 审批中状态 -->
        <template
          v-if="
            row.status === BpmProcessInstanceStatus.RUNNING &&
            row.tasks?.length > 0
          "
        >
          <!-- 单人审批 -->
          <template v-if="row.tasks.length === 1">
            <span>
              <Button type="link" @click="handleDetail(row)">
                {{ row.tasks[0].assigneeUser?.nickname }}
              </Button>
              ({{ row.tasks[0].name }}) 审批中
            </span>
          </template>
          <!-- 多人审批 -->
          <template v-else>
            <span>
              <Button type="link" @click="handleDetail(row)">
                {{ row.tasks[0].assigneeUser?.nickname }}
              </Button>
              等 {{ row.tasks.length }} 人 ({{ row.tasks[0].name }})审批中
            </span>
          </template>
        </template>
        <!-- 非审批中状态 -->
        <template v-else>
          <DictTag
            :type="DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS"
            :value="row.status"
          />
        </template>
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
