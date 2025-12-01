<script lang="ts" setup>
import { nextTick, onBeforeUnmount, ref, toRaw, watch } from 'vue';

import { FormItem, Input, Select } from 'ant-design-vue';

defineOptions({ name: 'ServiceTask' });
const props = defineProps({
  id: { type: String, default: '' },
  type: { type: String, default: '' },
});

const defaultTaskForm = ref({
  executeType: '',
  class: '',
  expression: '',
  delegateExpression: '',
});

const serviceTaskForm = ref<any>({});
const bpmnElement = ref();

const bpmnInstances = () => (window as any)?.bpmnInstances;

const resetTaskForm = () => {
  for (const key in defaultTaskForm.value) {
    const value =
      // @ts-ignore
      bpmnElement.value?.businessObject[key] || defaultTaskForm.value[key];
    serviceTaskForm.value[key] = value;
    if (value) {
      serviceTaskForm.value.executeType = key;
    }
  }
};

const updateElementTask = () => {
  const taskAttr = Object.create(null);
  const type = serviceTaskForm.value.executeType;
  for (const key in serviceTaskForm.value) {
    if (key !== 'executeType' && key !== type) taskAttr[key] = null;
  }
  taskAttr[type] = serviceTaskForm.value[type] || '';
  bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), taskAttr);
};

onBeforeUnmount(() => {
  bpmnElement.value = null;
});

watch(
  () => props.id,
  () => {
    bpmnElement.value = bpmnInstances().bpmnElement;
    nextTick(() => {
      resetTaskForm();
    });
  },
  { immediate: true },
);
</script>

<template>
  <div>
    <FormItem label="执行类型" key="executeType">
      <Select
        v-model:value="serviceTaskForm.executeType"
        :options="[
          { label: 'Java类', value: 'class' },
          { label: '表达式', value: 'expression' },
          { label: '代理表达式', value: 'delegateExpression' },
        ]"
      />
    </FormItem>
    <FormItem
      v-if="serviceTaskForm.executeType === 'class'"
      label="Java类"
      name="class"
      key="execute-class"
    >
      <Input
        v-model:value="serviceTaskForm.class"
        allow-clear
        @change="updateElementTask"
      />
    </FormItem>
    <FormItem
      v-if="serviceTaskForm.executeType === 'expression'"
      label="表达式"
      name="expression"
      key="execute-expression"
    >
      <Input
        v-model:value="serviceTaskForm.expression"
        allow-clear
        @change="updateElementTask"
      />
    </FormItem>
    <FormItem
      v-if="serviceTaskForm.executeType === 'delegateExpression'"
      label="代理表达式"
      name="delegateExpression"
      key="execute-delegate"
    >
      <Input
        v-model:value="serviceTaskForm.delegateExpression"
        allow-clear
        @change="updateElementTask"
      />
    </FormItem>
  </div>
</template>
