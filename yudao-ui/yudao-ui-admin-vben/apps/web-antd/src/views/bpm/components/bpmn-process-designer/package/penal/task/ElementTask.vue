<script lang="ts" setup>
import { ref, watch } from 'vue';

import { Checkbox, Form, FormItem } from 'ant-design-vue';

import { installedComponent } from './data';

defineOptions({ name: 'ElementTaskConfig' });

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
const taskConfigForm = ref({
  asyncAfter: false,
  asyncBefore: false,
  exclusive: false,
});
const witchTaskComponent = ref();

const bpmnElement = ref();

const bpmnInstances = () => (window as any).bpmnInstances;
const changeTaskAsync = () => {
  if (!taskConfigForm.value.asyncBefore && !taskConfigForm.value.asyncAfter) {
    taskConfigForm.value.exclusive = false;
  }
  bpmnInstances().modeling.updateProperties(bpmnInstances().bpmnElement, {
    ...taskConfigForm.value,
  });
};

watch(
  () => props.id,
  () => {
    bpmnElement.value = bpmnInstances().bpmnElement;
    taskConfigForm.value.asyncBefore =
      bpmnElement.value?.businessObject?.asyncBefore;
    taskConfigForm.value.asyncAfter =
      bpmnElement.value?.businessObject?.asyncAfter;
    taskConfigForm.value.exclusive =
      bpmnElement.value?.businessObject?.exclusive;
  },
  { immediate: true },
);
watch(
  () => props.type,
  () => {
    if (props.type) {
      // @ts-ignore
      witchTaskComponent.value = installedComponent[props.type].component;
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="panel-tab__content">
    <Form>
      <!-- add by 芋艿：由于「异步延续」暂时用不到，所以这里 display 为 none -->
      <FormItem label="异步延续" class="hidden">
        <Checkbox
          v-model:checked="taskConfigForm.asyncBefore"
          @change="changeTaskAsync"
        >
          异步前
        </Checkbox>
        <Checkbox
          v-model:checked="taskConfigForm.asyncAfter"
          @change="changeTaskAsync"
        >
          异步后
        </Checkbox>
        <Checkbox
          v-model:checked="taskConfigForm.exclusive"
          v-if="taskConfigForm.asyncAfter || taskConfigForm.asyncBefore"
          @change="changeTaskAsync"
        >
          排除
        </Checkbox>
      </FormItem>
      <component :is="witchTaskComponent" v-bind="$props" />
    </Form>
  </div>
</template>
