<script setup lang="ts">
import type { formCreate } from '@form-create/antd-designer';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmTaskApi } from '#/api/bpm/task';

import { nextTick, onMounted, ref, shallowRef } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { Button } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTaskListByProcessInstanceId } from '#/api/bpm/task';
import { setConfAndFields2 } from '#/components/form-create';

defineOptions({
  name: 'BpmProcessInstanceTaskList',
});

const props = defineProps<{
  id: string;
  loading: boolean;
}>();

// 使用 shallowRef 减少不必要的深度响应
const columns = shallowRef([
  {
    field: 'name',
    title: '审批节点',
    minWidth: 150,
  },
  {
    field: 'approver',
    title: '审批人',
    slots: {
      default: ({ row }: { row: BpmTaskApi.TaskManager }) => {
        return row.assigneeUser?.nickname || row.ownerUser?.nickname;
      },
    },
    minWidth: 180,
  },
  {
    field: 'createTime',
    title: '开始时间',
    formatter: 'formatDateTime',
    minWidth: 180,
  },
  {
    field: 'endTime',
    title: '结束时间',
    formatter: 'formatDateTime',
    minWidth: 180,
  },
  {
    field: 'status',
    title: '审批状态',
    minWidth: 150,
    cellRender: {
      name: 'CellDict',
      props: { type: DICT_TYPE.BPM_TASK_STATUS },
    },
  },
  {
    field: 'reason',
    title: '审批建议',
    slots: {
      default: 'slot-reason',
    },
    minWidth: 200,
  },
  {
    field: 'durationInMillis',
    title: '耗时',
    minWidth: 180,
    formatter: 'formatPast2',
  },
]);

// Grid配置和API
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: columns.value,
    keepSource: true,
    showFooter: true,
    border: true,
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: async () => {
          return await getTaskListByProcessInstanceId(props.id);
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
    cellConfig: {
      height: 60,
    },
  } as VxeTableGridOptions<BpmTaskApi.Task>,
});

/**
 * 刷新表格数据
 */
function refresh() {
  gridApi.query();
}

// 表单相关
interface TaskForm {
  rule: any[];
  option: Record<string, any>;
  value: Record<string, any>;
}

// 定义表单组件引用类型

// 使用明确的类型定义
const formRef = ref<formCreate>();
const taskForm = ref<TaskForm>({
  rule: [],
  option: {},
  value: {},
});

/**
 * 显示表单详情
 * @param row 任务数据
 */
async function showFormDetail(row: BpmTaskApi.TaskManager): Promise<void> {
  // 设置表单配置和表单字段
  taskForm.value = {
    rule: [],
    option: {},
    value: row,
  };

  setConfAndFields2(
    taskForm,
    row.formConf,
    row.formFields || [],
    row.formVariables || {},
  );

  // 打开弹窗
  modalApi.open();

  // 等待表单渲染
  await nextTick();

  // 获取表单API实例
  const formApi = formRef.value?.fapi;
  if (!formApi) return;

  // 设置表单不可编辑
  formApi.btn.show(false);
  formApi.resetBtn.show(false);
  formApi.disabled(true);
}

// 表单查看模态框
const [Modal, modalApi] = useVbenModal({
  title: '查看表单',
  footer: false,
});

onMounted(() => {
  refresh();
});

// 暴露刷新方法给父组件
defineExpose({
  refresh,
});
</script>

<template>
  <div class="flex h-full flex-col">
    <Grid>
      <template #slot-reason="{ row }">
        <div class="flex flex-wrap items-center justify-center">
          <span v-if="row.reason">{{ row.reason }}</span>
          <span v-else>-</span>

          <Button
            v-if="row.formId > 0"
            type="primary"
            @click="showFormDetail(row)"
            size="small"
            ghost
            class="ml-1"
          >
            <IconifyIcon icon="lucide:file-text" />
            <span class="!ml-0.5 text-xs">查看表单</span>
          </Button>
        </div>
      </template>
    </Grid>
    <Modal class="w-[800px]">
      <form-create
        ref="formRef"
        v-model="taskForm.value"
        :option="taskForm.option"
        :rule="taskForm.rule"
      />
    </Modal>
  </div>
</template>
