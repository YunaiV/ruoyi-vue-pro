<script setup lang="ts">
import type { SimpleFlowNode } from '../consts';

import { BpmNodeTypeEnum } from '@vben/constants';

import { useWatchNode } from '../helpers';
import ChildProcessNode from './nodes/child-process-node.vue';
import CopyTaskNode from './nodes/copy-task-node.vue';
import DelayTimerNode from './nodes/delay-timer-node.vue';
import EndEventNode from './nodes/end-event-node.vue';
import ExclusiveNode from './nodes/exclusive-node.vue';
import InclusiveNode from './nodes/inclusive-node.vue';
import ParallelNode from './nodes/parallel-node.vue';
import RouterNode from './nodes/router-node.vue';
import StartUserNode from './nodes/start-user-node.vue';
import TriggerNode from './nodes/trigger-node.vue';
import UserTaskNode from './nodes/user-task-node.vue';

defineOptions({ name: 'ProcessNodeTree' });

const props = defineProps({
  parentNode: {
    type: Object as () => SimpleFlowNode,
    default: () => null,
  },
  flowNode: {
    type: Object as () => SimpleFlowNode,
    default: () => null,
  },
});

const emits = defineEmits<{
  recursiveFindParentNode: [
    nodeList: SimpleFlowNode[],
    currentNode: SimpleFlowNode,
    nodeType: number,
  ];
  'update:flowNode': [node: SimpleFlowNode | undefined];
}>();

const currentNode = useWatchNode(props);

// 用于删除节点

const handleModelValueUpdate = (updateValue: any) => {
  emits('update:flowNode', updateValue);
};

const findParentNode = (nodeList: SimpleFlowNode[], nodeType: number) => {
  emits('recursiveFindParentNode', nodeList, props.parentNode, nodeType);
};

// 递归从父节点中查询匹配的节点
function recursiveFindParentNode(
  nodeList: SimpleFlowNode[],
  findNode: SimpleFlowNode,
  nodeType: number,
) {
  if (!findNode) {
    return;
  }
  if (findNode.type === BpmNodeTypeEnum.START_USER_NODE) {
    nodeList.push(findNode);
    return;
  }

  if (findNode.type === nodeType) {
    nodeList.push(findNode);
  }
  emits('recursiveFindParentNode', nodeList, props.parentNode, nodeType);
}
</script>
<template>
  <!-- 发起人节点 -->
  <StartUserNode
    v-if="currentNode && currentNode.type === BpmNodeTypeEnum.START_USER_NODE"
    :flow-node="currentNode"
  />
  <!-- 审批节点 -->
  <UserTaskNode
    v-if="
      currentNode &&
      (currentNode.type === BpmNodeTypeEnum.USER_TASK_NODE ||
        currentNode.type === BpmNodeTypeEnum.TRANSACTOR_NODE)
    "
    :flow-node="currentNode"
    @update:flow-node="handleModelValueUpdate"
    @find-parent-node="findParentNode"
  />
  <!-- 抄送节点 -->
  <CopyTaskNode
    v-if="currentNode && currentNode.type === BpmNodeTypeEnum.COPY_TASK_NODE"
    :flow-node="currentNode"
    @update:flow-node="handleModelValueUpdate"
  />
  <!-- 条件节点 -->
  <ExclusiveNode
    v-if="
      currentNode && currentNode.type === BpmNodeTypeEnum.CONDITION_BRANCH_NODE
    "
    :flow-node="currentNode"
    @update:model-value="handleModelValueUpdate"
    @find-parent-node="findParentNode"
  />
  <!-- 并行节点 -->
  <ParallelNode
    v-if="
      currentNode && currentNode.type === BpmNodeTypeEnum.PARALLEL_BRANCH_NODE
    "
    :flow-node="currentNode"
    @update:model-value="handleModelValueUpdate"
    @find-parent-node="findParentNode"
  />
  <!-- 包容分支节点 -->
  <InclusiveNode
    v-if="
      currentNode && currentNode.type === BpmNodeTypeEnum.INCLUSIVE_BRANCH_NODE
    "
    :flow-node="currentNode"
    @update:model-value="handleModelValueUpdate"
    @find-parent-node="findParentNode"
  />
  <!-- 延迟器节点 -->
  <DelayTimerNode
    v-if="currentNode && currentNode.type === BpmNodeTypeEnum.DELAY_TIMER_NODE"
    :flow-node="currentNode"
    @update:flow-node="handleModelValueUpdate"
  />
  <!-- 路由分支节点 -->
  <RouterNode
    v-if="
      currentNode && currentNode.type === BpmNodeTypeEnum.ROUTER_BRANCH_NODE
    "
    :flow-node="currentNode"
    @update:flow-node="handleModelValueUpdate"
  />
  <!-- 触发器节点 -->
  <TriggerNode
    v-if="currentNode && currentNode.type === BpmNodeTypeEnum.TRIGGER_NODE"
    :flow-node="currentNode"
    @update:flow-node="handleModelValueUpdate"
  />
  <!-- 子流程节点 -->
  <ChildProcessNode
    v-if="
      currentNode && currentNode.type === BpmNodeTypeEnum.CHILD_PROCESS_NODE
    "
    :flow-node="currentNode"
    @update:flow-node="handleModelValueUpdate"
  />
  <!-- 递归显示孩子节点  -->
  <ProcessNodeTree
    v-if="currentNode && currentNode.childNode"
    v-model:flow-node="currentNode.childNode"
    :parent-node="currentNode"
    @recursive-find-parent-node="recursiveFindParentNode"
  />

  <!-- 结束节点 -->
  <EndEventNode
    v-if="currentNode && currentNode.type === BpmNodeTypeEnum.END_EVENT_NODE"
    :flow-node="currentNode"
  />
</template>
