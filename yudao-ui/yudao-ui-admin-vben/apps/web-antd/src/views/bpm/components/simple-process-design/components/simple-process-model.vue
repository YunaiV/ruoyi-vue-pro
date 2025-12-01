<script setup lang="ts">
import type { SimpleFlowNode } from '../consts';

import { onMounted, provide, ref } from 'vue';

import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { downloadFileFromBlob, isString } from '@vben/utils';

import { Button, ButtonGroup, Modal, Row } from 'ant-design-vue';

import { NODE_DEFAULT_TEXT } from '../consts';
import { useWatchNode } from '../helpers';
import ProcessNodeTree from './process-node-tree.vue';

defineOptions({
  name: 'SimpleProcessModel',
});

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
  readonly: {
    type: Boolean,
    required: false,
    default: true,
  },
});

const emits = defineEmits<{
  save: [node: SimpleFlowNode | undefined];
}>();

const processNodeTree = useWatchNode(props);

provide('readonly', props.readonly);

// TODO 可优化：拖拽有点卡顿
/** 拖拽、放大缩小等操作 */
const scaleValue = ref(100);
const MAX_SCALE_VALUE = 200;
const MIN_SCALE_VALUE = 50;
const isDragging = ref(false);
const startX = ref(0);
const startY = ref(0);
const currentX = ref(0);
const currentY = ref(0);
const initialX = ref(0);
const initialY = ref(0);

function setGrabCursor() {
  document.body.style.cursor = 'grab';
}

function resetCursor() {
  document.body.style.cursor = 'default';
}

function startDrag(e: MouseEvent) {
  isDragging.value = true;
  startX.value = e.clientX - currentX.value;
  startY.value = e.clientY - currentY.value;
  setGrabCursor(); // 设置小手光标
}

function onDrag(e: MouseEvent) {
  if (!isDragging.value) return;
  e.preventDefault(); // 禁用文本选择

  // 使用 requestAnimationFrame 优化性能
  requestAnimationFrame(() => {
    currentX.value = e.clientX - startX.value;
    currentY.value = e.clientY - startY.value;
  });
}

function stopDrag() {
  isDragging.value = false;
  resetCursor(); // 重置光标
}

function zoomIn() {
  if (scaleValue.value === MAX_SCALE_VALUE) {
    return;
  }
  scaleValue.value += 10;
}

function zoomOut() {
  if (scaleValue.value === MIN_SCALE_VALUE) {
    return;
  }
  scaleValue.value -= 10;
}

function processReZoom() {
  scaleValue.value = 100;
}

function resetPosition() {
  currentX.value = initialX.value;
  currentY.value = initialY.value;
}

/** 校验节点设置 */
const errorDialogVisible = ref(false);
let errorNodes: SimpleFlowNode[] = [];

function validateNode(
  node: SimpleFlowNode | undefined,
  errorNodes: SimpleFlowNode[],
) {
  if (node) {
    const { type, showText, conditionNodes } = node;
    if (type === BpmNodeTypeEnum.END_EVENT_NODE) {
      return;
    }
    if (type === BpmNodeTypeEnum.START_USER_NODE) {
      // 发起人节点暂时不用校验，直接校验孩子节点
      validateNode(node.childNode, errorNodes);
    }

    if (
      type === BpmNodeTypeEnum.USER_TASK_NODE ||
      type === BpmNodeTypeEnum.COPY_TASK_NODE ||
      type === BpmNodeTypeEnum.CONDITION_NODE
    ) {
      if (!showText) {
        errorNodes.push(node);
      }
      validateNode(node.childNode, errorNodes);
    }

    if (
      type === BpmNodeTypeEnum.CONDITION_BRANCH_NODE ||
      type === BpmNodeTypeEnum.PARALLEL_BRANCH_NODE ||
      type === BpmNodeTypeEnum.INCLUSIVE_BRANCH_NODE
    ) {
      // 分支节点
      // 1. 先校验各个分支
      conditionNodes?.forEach((item) => {
        validateNode(item, errorNodes);
      });
      // 2. 校验孩子节点
      validateNode(node.childNode, errorNodes);
    }
  }
}

/** 获取当前流程数据 */
async function getCurrentFlowData() {
  try {
    errorNodes = [];
    validateNode(processNodeTree.value, errorNodes);
    if (errorNodes.length > 0) {
      errorDialogVisible.value = true;
      return undefined;
    }
    return processNodeTree.value;
  } catch (error) {
    console.error('获取流程数据失败:', error);
    return undefined;
  }
}

defineExpose({
  getCurrentFlowData,
});

/** 导出 JSON */
function exportJson() {
  downloadFileFromBlob({
    fileName: 'model.json',
    source: new Blob([JSON.stringify(processNodeTree.value)]),
  });
}

/** 导入 JSON */
const refFile = ref();
function importJson() {
  refFile.value.click();
}
function importLocalFile() {
  const file = refFile.value.files[0];
  file.text().then((result: any) => {
    if (isString(result)) {
      processNodeTree.value = JSON.parse(result);
      emits('save', processNodeTree.value);
    }
  });
}

// 在组件初始化时记录初始位置
onMounted(() => {
  initialX.value = currentX.value;
  initialY.value = currentY.value;
});
</script>
<template>
  <div class="simple-process-model-container">
    <div class="absolute right-0 top-0 bg-card">
      <Row type="flex" justify="end">
        <ButtonGroup key="scale-control">
          <Button v-if="!readonly" @click="exportJson">
            <IconifyIcon icon="lucide:download" /> 导出
          </Button>
          <Button v-if="!readonly" @click="importJson">
            <IconifyIcon icon="lucide:upload" />导入
          </Button>
          <!-- 用于打开本地文件-->
          <input
            v-if="!readonly"
            type="file"
            id="files"
            ref="refFile"
            class="hidden"
            accept=".json"
            @change="importLocalFile"
          />
          <Button @click="processReZoom()">
            <IconifyIcon icon="lucide:table-columns-split" />
          </Button>
          <Button :plain="true" @click="zoomOut()">
            <IconifyIcon icon="lucide:zoom-out" />
          </Button>
          <Button class="w-20"> {{ scaleValue }}% </Button>
          <Button :plain="true" @click="zoomIn()">
            <IconifyIcon icon="lucide:zoom-in" />
          </Button>
          <Button @click="resetPosition">重置</Button>
        </ButtonGroup>
      </Row>
    </div>
    <div
      class="simple-process-model"
      :style="`transform: translate(${currentX}px, ${currentY}px) scale(${scaleValue / 100});`"
      @mousedown="startDrag"
      @mousemove="onDrag"
      @mouseup="stopDrag"
      @mouseleave="stopDrag"
      @mouseenter="setGrabCursor"
    >
      <ProcessNodeTree
        v-if="processNodeTree"
        v-model:flow-node="processNodeTree"
      />
    </div>
  </div>

  <Modal
    v-model:open="errorDialogVisible"
    title="保存失败"
    width="400"
    :fullscreen="false"
  >
    <div class="mb-2">以下节点内容不完善，请修改后保存</div>
    <div
      class="line-height-normal mb-3 rounded p-2"
      v-for="(item, index) in errorNodes"
      :key="index"
    >
      {{ item.name }} : {{ NODE_DEFAULT_TEXT.get(item.type) }}
    </div>
    <template #footer>
      <Button type="primary" @click="errorDialogVisible = false">知道了</Button>
    </template>
  </Modal>
</template>
