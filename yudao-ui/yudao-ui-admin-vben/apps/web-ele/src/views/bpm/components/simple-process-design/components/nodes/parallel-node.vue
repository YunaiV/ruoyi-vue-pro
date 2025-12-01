<script setup lang="ts">
import type { SimpleFlowNode } from '../../consts';

import { inject, nextTick, ref, watch } from 'vue';

import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { buildShortUUID as generateUUID } from '@vben/utils';

import { ElButton, ElInput } from 'element-plus';

import { NODE_DEFAULT_TEXT } from '../../consts';
import { useTaskStatusClass } from '../../helpers';
import ProcessNodeTree from '../process-node-tree.vue';
import NodeHandler from './node-handler.vue';

defineOptions({ name: 'ParallelNode' });

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
});

// 定义事件，更新父组件
const emits = defineEmits<{
  findParnetNode: [nodeList: SimpleFlowNode[], nodeType: number];
  recursiveFindParentNode: [
    nodeList: SimpleFlowNode[],
    currentNode: SimpleFlowNode,
    nodeType: number,
  ];
  'update:modelValue': [node: SimpleFlowNode | undefined];
}>();

const currentNode = ref<SimpleFlowNode>(props.flowNode);
// 是否只读
const readonly = inject<Boolean>('readonly');

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
    // 当输入框显示时 自动聚焦
    newValues.forEach((value, index) => {
      if (value) {
        // 当显示状态从 false 变为 true 时 自动聚焦
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
  conditionNode.name = conditionNode.name || `并行${index + 1}`;
}

// 点击条件名称
function clickEvent(index: number) {
  showInputs.value[index] = true;
}

// 新增条件
function addCondition() {
  const conditionNodes = currentNode.value.conditionNodes;
  if (conditionNodes) {
    const len = conditionNodes.length;
    const lastIndex = len - 1;
    const conditionData: SimpleFlowNode = {
      id: `Flow_${generateUUID()}`,
      name: `并行${len}`,
      showText: '无需配置条件同时执行',
      type: BpmNodeTypeEnum.CONDITION_NODE,
      childNode: undefined,
      conditionNodes: [],
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
  // 条件节点 (NodeType.CONDITION_NODE) 比较特殊。需要调用其父节点并行节点（NodeType.PARALLEL_NODE) 继续查找
  emits('findParnetNode', nodeList, nodeType);
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
        <span class="iconfont icon-parallel icon-size parallel"></span>
      </div>
      <ElButton
        v-else
        class="branch-node-add"
        color="#626aef"
        @click="addCondition"
        plain
      >
        添加分支
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
              :class="`${useTaskStatusClass(item.activityStatus)}`"
            >
              <div class="branch-node-title-container">
                <div v-if="showInputs[index]">
                  <ElInput
                    :ref="
                      (el) => {
                        inputRefs[index] = el as HTMLInputElement;
                      }
                    "
                    type="text"
                    class="input-max-width editable-title-input"
                    @blur="changeNodeName(index)"
                    @keyup.enter="changeNodeName(index)"
                    v-model="item.name"
                  />
                </div>
                <div v-else class="branch-title" @click="clickEvent(index)">
                  {{ item.name }}
                </div>
                <div class="branch-priority">无优先级</div>
              </div>
              <div class="branch-node-content">
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
              <div v-if="!readonly" class="node-toolbar">
                <div class="toolbar-icon">
                  <IconifyIcon
                    color="#0089ff"
                    icon="lucide:circle-x"
                    @click="deleteCondition(index)"
                  />
                </div>
              </div>
            </div>
            <NodeHandler
              v-model:child-node="item.childNode"
              :current-node="item"
            />
          </div>
        </div>
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
