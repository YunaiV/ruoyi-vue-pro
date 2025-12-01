<script setup lang="ts">
import type { SimpleFlowNode } from '../../consts';

import { inject, ref } from 'vue';

import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { ElInput } from 'element-plus';

import { NODE_DEFAULT_TEXT } from '../../consts';
import { useNodeName2, useTaskStatusClass, useWatchNode } from '../../helpers';
import CopyTaskNodeConfig from '../nodes-config/copy-task-node-config.vue';
import NodeHandler from './node-handler.vue';

defineOptions({
  name: 'CopyTaskNode',
});
const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
});
// 定义事件，更新父组件
const emits = defineEmits<{
  'update:flowNode': [node: SimpleFlowNode | undefined];
}>();
// 是否只读
const readonly = inject<Boolean>('readonly');
// 监控节点的变化
const currentNode = useWatchNode(props);
// 节点名称编辑
const { showInput, changeNodeName, clickTitle, inputRef } = useNodeName2(
  currentNode,
  BpmNodeTypeEnum.COPY_TASK_NODE,
);

const nodeSetting = ref();
// 打开节点配置
function openNodeConfig() {
  if (readonly) {
    return;
  }
  nodeSetting.value.showCopyTaskNodeConfig(currentNode.value);
  nodeSetting.value.openDrawer();
}

// 删除节点。更新当前节点为孩子节点
function deleteNode() {
  emits('update:flowNode', currentNode.value.childNode);
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
          <div class="node-title-icon copy-task">
            <span class="iconfont icon-copy"></span>
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
        <div class="node-content" @click="openNodeConfig">
          <div
            class="node-text"
            :title="currentNode.showText"
            v-if="currentNode.showText"
          >
            {{ currentNode.showText }}
          </div>
          <div class="node-text" v-else>
            {{ NODE_DEFAULT_TEXT.get(BpmNodeTypeEnum.COPY_TASK_NODE) }}
          </div>
          <IconifyIcon v-if="!readonly" icon="lucide:chevron-right" />
        </div>
        <div v-if="!readonly" class="node-toolbar">
          <div class="toolbar-icon">
            <IconifyIcon
              color="#0089ff"
              icon="lucide:circle-x"
              :size="18"
              @click="deleteNode"
            />
          </div>
        </div>
      </div>

      <!-- 传递子节点给添加节点组件。会在子节点前面添加节点 -->
      <NodeHandler
        v-if="currentNode"
        v-model:child-node="currentNode.childNode"
        :current-node="currentNode"
      />
    </div>
    <CopyTaskNodeConfig
      v-if="!readonly && currentNode"
      ref="nodeSetting"
      :flow-node="currentNode"
    />
  </div>
</template>
