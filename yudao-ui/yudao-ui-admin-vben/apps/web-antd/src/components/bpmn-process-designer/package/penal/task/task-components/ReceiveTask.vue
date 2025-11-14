<script lang="ts" setup>
import {
  h,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  toRaw,
  watch,
} from 'vue';

import { PlusOutlined } from '@vben/icons';

import {
  Button,
  Form,
  Input,
  message,
  Modal,
  Select,
  SelectOption,
} from 'ant-design-vue';

defineOptions({ name: 'ReceiveTask' });
const props = defineProps({
  id: { type: String, default: '' },
  type: { type: String, default: '' },
});

const bindMessageId = ref('');
const newMessageForm = ref<Record<string, any>>({});
const messageMap = ref<Record<string, any>>({});
const messageModelVisible = ref(false);
const bpmnElement = ref<any>();
const bpmnMessageRefsMap = ref<Record<string, any>>();
const bpmnRootElements = ref<any>();

const bpmnInstances = () => (window as any).bpmnInstances;
const getBindMessage = () => {
  bpmnElement.value = bpmnInstances().bpmnElement;
  bindMessageId.value =
    bpmnElement.value.businessObject?.messageRef?.id || '-1';
};
const openMessageModel = () => {
  messageModelVisible.value = true;
  newMessageForm.value = {};
};
const createNewMessage = () => {
  if (messageMap.value[newMessageForm.value.id]) {
    message.error('该消息已存在，请修改id后重新保存');
    return;
  }
  const newMessage = bpmnInstances().moddle.create(
    'bpmn:Message',
    newMessageForm.value,
  );
  bpmnRootElements.value.push(newMessage);
  messageMap.value[newMessageForm.value.id] = newMessageForm.value.name;
  // @ts-ignore
  if (bpmnMessageRefsMap.value) {
    bpmnMessageRefsMap.value[newMessageForm.value.id] = newMessage;
  }
  messageModelVisible.value = false;
};
const updateTaskMessage = (messageId: string) => {
  if (messageId === '-1') {
    bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
      messageRef: null,
    });
  } else {
    bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
      messageRef: bpmnMessageRefsMap.value?.[messageId],
    });
  }
};

onMounted(() => {
  bpmnMessageRefsMap.value = Object.create(null);
  bpmnRootElements.value =
    bpmnInstances().modeler.getDefinitions().rootElements;
  bpmnRootElements.value
    .filter((el: any) => el.$type === 'bpmn:Message')
    .forEach((m: any) => {
      // @ts-ignore
      if (bpmnMessageRefsMap.value) {
        bpmnMessageRefsMap.value[m.id] = m;
      }
      messageMap.value[m.id] = m.name;
    });
  messageMap.value['-1'] = '无';
});

onBeforeUnmount(() => {
  bpmnElement.value = null;
});
watch(
  () => props.id,
  () => {
    // bpmnElement.value = bpmnInstances().bpmnElement
    nextTick(() => {
      getBindMessage();
    });
  },
  { immediate: true },
);
</script>

<template>
  <div style="margin-top: 16px">
    <Form.Item label="消息实例">
      <div
        style="
          display: flex;
          flex-wrap: nowrap;
          align-items: center;
          justify-content: space-between;
        "
      >
        <Select
          v-model:value="bindMessageId"
          @change="(value: any) => updateTaskMessage(value)"
        >
          <SelectOption
            v-for="key in Object.keys(messageMap)"
            :value="key"
            :key="key"
          >
            {{ messageMap[key] }}
          </SelectOption>
        </Select>
        <Button
          type="primary"
          :icon="h(PlusOutlined)"
          style="margin-left: 8px"
          @click="openMessageModel"
        />
      </div>
    </Form.Item>
    <Modal
      v-model:open="messageModelVisible"
      :mask-closable="false"
      title="创建新消息"
      width="400px"
      :destroy-on-close="true"
    >
      <Form :model="newMessageForm" size="small">
        <Form.Item label="消息ID">
          <Input v-model:value="newMessageForm.id" allow-clear />
        </Form.Item>
        <Form.Item label="消息名称">
          <Input v-model:value="newMessageForm.name" allow-clear />
        </Form.Item>
      </Form>
      <template #footer>
        <Button size="small" type="primary" @click="createNewMessage">
          确 认
        </Button>
      </template>
    </Modal>
  </div>
</template>
