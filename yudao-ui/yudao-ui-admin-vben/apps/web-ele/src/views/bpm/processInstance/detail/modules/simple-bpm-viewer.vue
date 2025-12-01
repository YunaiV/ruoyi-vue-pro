<script lang="ts" setup>
import type { SimpleFlowNode } from '#/views/bpm/components/simple-process-design';

import { ref, watch } from 'vue';

import { BpmNodeTypeEnum, BpmTaskStatusEnum } from '@vben/constants';

import { SimpleProcessViewer } from '#/views/bpm/components/simple-process-design';

defineOptions({ name: 'BpmProcessInstanceSimpleViewer' });

const props = withDefaults(
  defineProps<{
    loading?: boolean; // 是否加载中
    modelView?: any;
    simpleJson?: string; // Simple 模型结构数据 (json 格式)
  }>(),
  {
    loading: false,
    modelView: () => ({}),
    simpleJson: '',
  },
);

const simpleModel = ref<any>({});
const tasks = ref([]); // 用户任务
const processInstance = ref(); // 流程实例

/** 监控模型视图 包括任务列表、进行中的活动节点编号等 */
watch(
  () => props.modelView,
  async (newModelView) => {
    if (newModelView) {
      tasks.value = newModelView.tasks;
      processInstance.value = newModelView.processInstance;
      // 已经拒绝的活动节点编号集合，只包括 UserTask
      const rejectedTaskActivityIds: string[] =
        newModelView.rejectedTaskActivityIds;
      // 进行中的活动节点编号集合， 只包括 UserTask
      const unfinishedTaskActivityIds: string[] =
        newModelView.unfinishedTaskActivityIds;
      // 已经完成的活动节点编号集合， 包括 UserTask、Gateway 等
      const finishedActivityIds: string[] =
        newModelView.finishedTaskActivityIds;
      // 已经完成的连线节点编号集合，只包括 SequenceFlow
      const finishedSequenceFlowActivityIds: string[] =
        newModelView.finishedSequenceFlowActivityIds;
      setSimpleModelNodeTaskStatus(
        newModelView.simpleModel,
        newModelView.processInstance?.status,
        rejectedTaskActivityIds,
        unfinishedTaskActivityIds,
        finishedActivityIds,
        finishedSequenceFlowActivityIds,
      );
      simpleModel.value = newModelView.simpleModel || {};
    }
  },
);

/** 监控模型结构数据 */
watch(
  () => props.simpleJson,
  async (value) => {
    if (value) {
      simpleModel.value = JSON.parse(value);
    }
  },
);

const setSimpleModelNodeTaskStatus = (
  simpleModel: SimpleFlowNode | undefined,
  processStatus: number,
  rejectedTaskActivityIds: string[],
  unfinishedTaskActivityIds: string[],
  finishedActivityIds: string[],
  finishedSequenceFlowActivityIds: string[],
) => {
  if (!simpleModel) {
    return;
  }
  // 结束节点
  if (simpleModel.type === BpmNodeTypeEnum.END_EVENT_NODE) {
    simpleModel.activityStatus = finishedActivityIds.includes(simpleModel.id)
      ? processStatus
      : BpmTaskStatusEnum.NOT_START;
    return;
  }
  // 审批节点
  if (
    simpleModel.type === BpmNodeTypeEnum.START_USER_NODE ||
    simpleModel.type === BpmNodeTypeEnum.USER_TASK_NODE ||
    simpleModel.type === BpmNodeTypeEnum.TRANSACTOR_NODE ||
    simpleModel.type === BpmNodeTypeEnum.CHILD_PROCESS_NODE
  ) {
    simpleModel.activityStatus = BpmTaskStatusEnum.NOT_START;
    if (rejectedTaskActivityIds.includes(simpleModel.id)) {
      simpleModel.activityStatus = BpmTaskStatusEnum.REJECT;
    } else if (unfinishedTaskActivityIds.includes(simpleModel.id)) {
      simpleModel.activityStatus = BpmTaskStatusEnum.RUNNING;
    } else if (finishedActivityIds.includes(simpleModel.id)) {
      simpleModel.activityStatus = BpmTaskStatusEnum.APPROVE;
    }
    // TODO 是不是还缺一个 cancel 的状态 @jason：
  }
  // 抄送节点
  if (simpleModel.type === BpmNodeTypeEnum.COPY_TASK_NODE) {
    // 抄送节点,只有通过和未执行状态
    simpleModel.activityStatus = finishedActivityIds.includes(simpleModel.id)
      ? BpmTaskStatusEnum.APPROVE
      : BpmTaskStatusEnum.NOT_START;
  }
  // 延迟器节点
  if (simpleModel.type === BpmNodeTypeEnum.DELAY_TIMER_NODE) {
    // 延迟器节点,只有通过和未执行状态
    simpleModel.activityStatus = finishedActivityIds.includes(simpleModel.id)
      ? BpmTaskStatusEnum.APPROVE
      : BpmTaskStatusEnum.NOT_START;
  }
  // 触发器节点
  if (simpleModel.type === BpmNodeTypeEnum.TRIGGER_NODE) {
    // 触发器节点,只有通过和未执行状态
    simpleModel.activityStatus = finishedActivityIds.includes(simpleModel.id)
      ? BpmTaskStatusEnum.APPROVE
      : BpmTaskStatusEnum.NOT_START;
  }

  // 条件节点对应 SequenceFlow
  if (simpleModel.type === BpmNodeTypeEnum.CONDITION_NODE) {
    // 条件节点,只有通过和未执行状态
    simpleModel.activityStatus = finishedSequenceFlowActivityIds.includes(
      simpleModel.id,
    )
      ? BpmTaskStatusEnum.APPROVE
      : BpmTaskStatusEnum.NOT_START;
  }
  // 网关节点
  if (
    simpleModel.type === BpmNodeTypeEnum.CONDITION_BRANCH_NODE ||
    simpleModel.type === BpmNodeTypeEnum.PARALLEL_BRANCH_NODE ||
    simpleModel.type === BpmNodeTypeEnum.INCLUSIVE_BRANCH_NODE ||
    simpleModel.type === BpmNodeTypeEnum.ROUTER_BRANCH_NODE
  ) {
    // 网关节点。只有通过和未执行状态
    simpleModel.activityStatus = finishedActivityIds.includes(simpleModel.id)
      ? BpmTaskStatusEnum.APPROVE
      : BpmTaskStatusEnum.NOT_START;
    simpleModel.conditionNodes?.forEach((node) => {
      setSimpleModelNodeTaskStatus(
        node,
        processStatus,
        rejectedTaskActivityIds,
        unfinishedTaskActivityIds,
        finishedActivityIds,
        finishedSequenceFlowActivityIds,
      );
    });
  }

  setSimpleModelNodeTaskStatus(
    simpleModel.childNode,
    processStatus,
    rejectedTaskActivityIds,
    unfinishedTaskActivityIds,
    finishedActivityIds,
    finishedSequenceFlowActivityIds,
  );
};
</script>
<template>
  <div v-loading="loading">
    <SimpleProcessViewer
      :flow-node="simpleModel"
      :tasks="tasks"
      :process-instance="processInstance"
    />
  </div>
</template>
