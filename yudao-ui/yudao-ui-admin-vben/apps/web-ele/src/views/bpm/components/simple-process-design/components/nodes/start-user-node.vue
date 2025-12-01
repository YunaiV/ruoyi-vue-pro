<script setup lang="ts">
// TODO @芋艿：后续是不是把业务组件，挪到每个模块里；待定
import type { Ref } from 'vue';

import type { SimpleFlowNode } from '../../consts';

import { inject, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { ElInput } from 'element-plus';

import { NODE_DEFAULT_TEXT } from '../../consts';
import { useNodeName2, useTaskStatusClass, useWatchNode } from '../../helpers';
import StartUserNodeConfig from '../nodes-config/start-user-node-config.vue';
import TaskListModal from './modules/task-list-modal.vue';
import NodeHandler from './node-handler.vue';

defineOptions({ name: 'StartUserNode' });

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    default: () => null,
  },
});

// 定义事件，更新父组件
defineEmits<{
  'update:modelValue': [node: SimpleFlowNode | undefined];
}>();

const readonly = inject<Boolean>('readonly'); // 是否只读
const tasks = inject<Ref<any[]>>('tasks', ref([]));
// 监控节点变化
const currentNode = useWatchNode(props);
// 节点名称编辑
const { showInput, changeNodeName, clickTitle, inputRef } = useNodeName2(
  currentNode,
  BpmNodeTypeEnum.START_USER_NODE,
);

const nodeSetting = ref();

const [Modal, modalApi] = useVbenModal({
  connectedComponent: TaskListModal,
  destroyOnClose: true,
});
function nodeClick() {
  if (readonly) {
    // 只读模式，弹窗显示任务信息
    if (tasks && tasks.value) {
      // 过滤出当前节点的任务
      const nodeTasks = tasks.value.filter(
        (task) => task.taskDefinitionKey === currentNode.value.id,
      );
      // 弹窗显示任务信息
      modalApi
        .setData(nodeTasks)
        .setState({ title: currentNode.value.name })
        .open();
    }
  } else {
    nodeSetting.value.showStartUserNodeConfig(currentNode.value);
  }
}
</script>
<template>
  <div class="node-wrapper">
    <div class="node-container">
      <div
        class="node-box"
        :class="[
          { 'node-config-error': !currentNode.showText },
          `${useTaskStatusClass(currentNode?.activityStatus)}`,
        ]"
      >
        <div class="node-title-container">
          <div class="node-title-icon start-user">
            <span class="iconfont icon-start-user"></span>
          </div>
          <ElInput
            ref="inputRef"
            v-if="!readonly && showInput"
            type="text"
            class="editable-title-input"
            @blur="changeNodeName()"
            @keyup.enter="changeNodeName()"
            v-model="currentNode.name"
            :placeholder="currentNode.name"
          />
          <div v-else class="node-title" @click="clickTitle">
            {{ currentNode.name }}
          </div>
        </div>
        <div class="node-content" @click="nodeClick">
          <div
            class="node-text"
            :title="currentNode.showText"
            v-if="currentNode.showText"
          >
            {{ currentNode.showText }}
          </div>
          <div class="node-text" v-else>
            {{ NODE_DEFAULT_TEXT.get(BpmNodeTypeEnum.START_USER_NODE) }}
          </div>
          <IconifyIcon icon="lucide:chevron-right" v-if="!readonly" />
        </div>
      </div>
      <!-- 传递子节点给添加节点组件。会在子节点前面添加节点 -->
      <NodeHandler
        v-if="currentNode"
        v-model:child-node="currentNode.childNode"
        :current-node="currentNode"
      />
    </div>
  </div>

  <StartUserNodeConfig
    v-if="!readonly && currentNode"
    ref="nodeSetting"
    :flow-node="currentNode"
  />
  <!-- 审批记录弹窗 -->
  <Modal />
</template>
