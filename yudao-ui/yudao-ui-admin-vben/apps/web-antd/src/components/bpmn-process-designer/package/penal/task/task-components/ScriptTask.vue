<script lang="ts" setup>
import { nextTick, onBeforeUnmount, ref, toRaw, watch } from 'vue';

import {
  FormItem,
  Input,
  Select,
  SelectOption,
  Textarea,
} from 'ant-design-vue';

defineOptions({ name: 'ScriptTask' });
const props = defineProps({
  id: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: '',
  },
});
const defaultTaskForm = ref({
  scriptFormat: '',
  script: '',
  resource: '',
  resultVariable: '',
});
const scriptTaskForm = ref<any>({});
const bpmnElement = ref();

const bpmnInstances = () => (window as any)?.bpmnInstances;

const resetTaskForm = () => {
  for (const key in defaultTaskForm.value) {
    // @ts-ignore
    scriptTaskForm.value[key] =
      bpmnElement.value?.businessObject[
        key as keyof typeof defaultTaskForm.value
      ] || defaultTaskForm.value[key as keyof typeof defaultTaskForm.value];
  }
  scriptTaskForm.value.scriptType = scriptTaskForm.value.script
    ? 'inline'
    : 'external';
};
const updateElementTask = () => {
  const taskAttr = Object.create(null);
  taskAttr.scriptFormat = scriptTaskForm.value.scriptFormat || null;
  taskAttr.resultVariable = scriptTaskForm.value.resultVariable || null;
  if (scriptTaskForm.value.scriptType === 'inline') {
    taskAttr.script = scriptTaskForm.value.script || null;
    taskAttr.resource = null;
  } else {
    taskAttr.resource = scriptTaskForm.value.resource || null;
    taskAttr.script = null;
  }
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
  <div class="mt-4">
    <FormItem label="脚本格式">
      <Input
        v-model:value="scriptTaskForm.scriptFormat"
        allow-clear
        @input="updateElementTask()"
        @change="updateElementTask()"
      />
    </FormItem>
    <FormItem label="脚本类型">
      <Select v-model:value="scriptTaskForm.scriptType">
        <SelectOption value="inline">内联脚本</SelectOption>
        <SelectOption value="external">外部资源</SelectOption>
      </Select>
    </FormItem>
    <FormItem label="脚本" v-show="scriptTaskForm.scriptType === 'inline'">
      <Textarea
        v-model:value="scriptTaskForm.script"
        :auto-size="{ minRows: 2, maxRows: 4 }"
        allow-clear
        @input="updateElementTask()"
        @change="updateElementTask()"
      />
    </FormItem>
    <FormItem
      label="资源地址"
      v-show="scriptTaskForm.scriptType === 'external'"
    >
      <Input
        v-model:value="scriptTaskForm.resource"
        allow-clear
        @input="updateElementTask()"
        @change="updateElementTask()"
      />
    </FormItem>
    <FormItem label="结果变量">
      <Input
        v-model:value="scriptTaskForm.resultVariable"
        allow-clear
        @input="updateElementTask()"
        @change="updateElementTask()"
      />
    </FormItem>
  </div>
</template>
