<script setup lang="ts">
import type { SimpleFlowNode } from '../consts';

import { provide, ref, watch } from 'vue';

import { useWatchNode } from '../helpers';
import SimpleProcessModel from './simple-process-model.vue';

defineOptions({ name: 'SimpleProcessViewer' });

const props = withDefaults(
  defineProps<{
    flowNode: SimpleFlowNode;
    // 流程实例
    processInstance?: any;
    // 流程任务
    tasks?: any[];
  }>(),
  {
    processInstance: undefined,
    tasks: () => [] as any[],
  },
);
const approveTasks = ref<any[]>(props.tasks);
const currentProcessInstance = ref(props.processInstance);
const simpleModel = useWatchNode(props);
watch(
  () => props.tasks,
  (newValue) => {
    approveTasks.value = newValue;
  },
);
watch(
  () => props.processInstance,
  (newValue) => {
    currentProcessInstance.value = newValue;
  },
);
// 提供给后代组件使用
provide('tasks', approveTasks);
provide('processInstance', currentProcessInstance);
</script>
<template>
  <SimpleProcessModel :flow-node="simpleModel" :readonly="true" />
</template>
