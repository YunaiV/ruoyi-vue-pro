<script setup lang="ts">
import type { SimpleFlowNode } from '../../consts';

import { getCurrentInstance, inject, nextTick, ref, watch } from 'vue';

import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { cloneDeep, buildShortUUID as generateUUID } from '@vben/utils';

import { ElButton, ElInput } from 'element-plus';

import {
  ConditionType,
  DEFAULT_CONDITION_GROUP_VALUE,
  NODE_DEFAULT_TEXT,
} from '../../consts';
import {
  getDefaultInclusiveConditionNodeName,
  useTaskStatusClass,
} from '../../helpers';
import ConditionNodeConfig from '../nodes-config/condition-node-config.vue';
import ProcessNodeTree from '../process-node-tree.vue';
import NodeHandler from './node-handler.vue';

defineOptions({
  name: 'InclusiveNode',
});

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
});

// 定义事件，更新父组件
const emits = defineEmits<{
  findParentNode: [nodeList: SimpleFlowNode[], nodeType: number];
  recursiveFindParentNode: [
    nodeList: SimpleFlowNode[],
    currentNode: SimpleFlowNode,
    nodeType: number,
  ];
  'update:modelValue': [node: SimpleFlowNode | undefined];
}>();

const { proxy } = getCurrentInstance() as any;
// 是否只读
const readonly = inject<Boolean>('readonly');

const currentNode = ref<SimpleFlowNode>(props.flowNode);

watch(
  () => props.flowNode,
  (newValue) => {
    currentNode.value = newValue;
  },
);
// 条件节点名称输入框引用
const inputRefs = ref<HTMLInputElement[]>([]);
// 节点名称输入框显示状态
const showInputs = ref<boolean[]>([]);
// 监听显示状态变化
watch(
  showInputs,
  (newValues) => {
    // 当状态为 true 时
    newValues.forEach((value, index) => {
      if (value) {
        // 当显示状态从 false 变为 true 时
        nextTick(() => {
          inputRefs.value[index]?.focus();
        });
      }
    });
  },
  { deep: true },
);
// 修改节点名称
function changeNodeName(index: number) {
  showInputs.value[index] = false;
  const conditionNode = currentNode.value.conditionNodes?.at(
    index,
  ) as SimpleFlowNode;
  conditionNode.name =
    conditionNode.name ||
    getDefaultInclusiveConditionNodeName(
      index,
      conditionNode.conditionSetting?.defaultFlow,
    );
}

// 点击条件名称
function clickEvent(index: number) {
  showInputs.value[index] = true;
}

function conditionNodeConfig(nodeId: string) {
  if (readonly) {
    return;
  }
  const conditionNode = proxy.$refs[nodeId][0];
  conditionNode.open();
}

// 新增条件
function addCondition() {
  const conditionNodes = currentNode.value.conditionNodes;
  if (conditionNodes) {
    const len = conditionNodes.length;
    const lastIndex = len - 1;
    const conditionData: SimpleFlowNode = {
      id: `Flow_${generateUUID()}`,
      name: `包容条件${len}`,
      showText: '',
      type: BpmNodeTypeEnum.CONDITION_NODE,
      childNode: undefined,
      conditionNodes: [],
      conditionSetting: {
        defaultFlow: false,
        conditionType: ConditionType.RULE,
        conditionGroups: cloneDeep(DEFAULT_CONDITION_GROUP_VALUE),
      },
    };
    conditionNodes.splice(lastIndex, 0, conditionData);
  }
}

// 删除条件
function deleteCondition(index: number) {
  const conditionNodes = currentNode.value.conditionNodes;
  if (conditionNodes) {
    conditionNodes.splice(index, 1);
    if (conditionNodes.length === 1) {
      const childNode = currentNode.value.childNode;
      // 更新此节点为后续孩子节点
      emits('update:modelValue', childNode);
    }
  }
}

// 移动节点
function moveNode(index: number, to: number) {
  // -1 ：向左 1：向右
  if (
    currentNode.value.conditionNodes &&
    currentNode.value.conditionNodes[index]
  ) {
    currentNode.value.conditionNodes[index] =
      currentNode.value.conditionNodes.splice(
        index + to,
        1,
        currentNode.value.conditionNodes[index],
      )[0] as SimpleFlowNode;
  }
}

// 递归从父节点中查询匹配的节点
function recursiveFindParentNode(
  nodeList: SimpleFlowNode[],
  node: SimpleFlowNode,
  nodeType: number,
) {
  if (!node || node.type === BpmNodeTypeEnum.START_USER_NODE) {
    return;
  }
  if (node.type === nodeType) {
    nodeList.push(node);
  }
  // 条件节点 (NodeType.CONDITION_NODE) 比较特殊。需要调用其父节点条件分支节点（NodeType.INCLUSIVE_BRANCH_NODE) 继续查找
  emits('findParentNode', nodeList, nodeType);
}
</script>
<template>
  <div class="branch-node-wrapper">
    <div class="branch-node-container">
      <div
        v-if="readonly"
        class="branch-node-readonly"
        :class="`${useTaskStatusClass(currentNode?.activityStatus)}`"
      >
        <span class="iconfont icon-inclusive icon-size inclusive"></span>
      </div>
      <ElButton v-else class="branch-node-add" @click="addCondition">
        添加条件
      </ElButton>
      <div
        class="branch-node-item"
        v-for="(item, index) in currentNode.conditionNodes"
        :key="index"
      >
        <template v-if="index === 0">
          <div class="branch-line-first-top"></div>
          <div class="branch-line-first-bottom"></div>
        </template>
        <template v-if="index + 1 === currentNode.conditionNodes?.length">
          <div class="branch-line-last-top"></div>
          <div class="branch-line-last-bottom"></div>
        </template>
        <div class="node-wrapper">
          <div class="node-container">
            <div
              class="node-box"
              :class="[
                { 'node-config-error': !item.showText },
                `${useTaskStatusClass(item.activityStatus)}`,
              ]"
            >
              <div class="branch-node-title-container">
                <div v-if="!readonly && showInputs[index]">
                  <ElInput
                    :ref="
                      (el) => {
                        inputRefs[index] = el as HTMLInputElement;
                      }
                    "
                    type="text"
                    class="editable-title-input"
                    @blur="changeNodeName(index)"
                    @keyup.enter="changeNodeName(index)"
                    v-model="item.name"
                  />
                </div>
                <div v-else class="branch-title" @click="clickEvent(index)">
                  {{ item.name }}
                </div>
              </div>
              <div
                class="branch-node-content"
                @click="conditionNodeConfig(item.id)"
              >
                <div
                  class="branch-node-text"
                  :title="item.showText"
                  v-if="item.showText"
                >
                  {{ item.showText }}
                </div>
                <div class="branch-node-text" v-else>
                  {{ NODE_DEFAULT_TEXT.get(BpmNodeTypeEnum.CONDITION_NODE) }}
                </div>
              </div>
              <div
                class="node-toolbar"
                v-if="
                  !readonly && index + 1 !== currentNode.conditionNodes?.length
                "
              >
                <div class="toolbar-icon">
                  <IconifyIcon
                    color="#0089ff"
                    icon="lucide:circle-x"
                    :size="18"
                    @click="deleteCondition(index)"
                  />
                </div>
              </div>
              <div
                class="branch-node-move move-node-left"
                v-if="
                  !readonly &&
                  index !== 0 &&
                  index + 1 !== currentNode.conditionNodes?.length
                "
                @click="moveNode(index, -1)"
              >
                <IconifyIcon icon="lucide:chevron-left" />
              </div>

              <div
                class="branch-node-move move-node-right"
                v-if="
                  !readonly &&
                  currentNode.conditionNodes &&
                  index < currentNode.conditionNodes.length - 2
                "
                @click="moveNode(index, 1)"
              >
                <IconifyIcon icon="lucide:chevron-right" />
              </div>
            </div>
            <NodeHandler
              v-model:child-node="item.childNode"
              :current-node="item"
            />
          </div>
        </div>
        <!-- 条件节点配置 -->
        <ConditionNodeConfig
          :node-index="index"
          :condition-node="item"
          :ref="item.id"
        />
        <!-- 递归显示子节点 -->
        <ProcessNodeTree
          v-if="item && item.childNode"
          :parent-node="item"
          v-model:flow-node="item.childNode"
          @recursive-find-parent-node="recursiveFindParentNode"
        />
      </div>
    </div>
    <NodeHandler
      v-if="currentNode"
      v-model:child-node="currentNode.childNode"
      :current-node="currentNode"
    />
  </div>
</template>
