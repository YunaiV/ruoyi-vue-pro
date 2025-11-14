<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Form,
  FormItem,
  Input,
  message,
  Modal,
  Table,
  TableColumn,
} from 'ant-design-vue';

defineOptions({ name: 'SignalAndMassage' });
const signalList = ref<any[]>([]);
const messageList = ref<any[]>([]);
const dialogVisible = ref(false);
const modelType = ref('');
const modelObjectForm = ref<any>({});
const rootElements = ref();
const messageIdMap = ref();
const signalIdMap = ref();
const modelConfig = computed(() => {
  return modelType.value === 'message'
    ? { title: '创建消息', idLabel: '消息ID', nameLabel: '消息名称' }
    : { title: '创建信号', idLabel: '信号ID', nameLabel: '信号名称' };
});
const bpmnInstances = () => (window as any)?.bpmnInstances;

const initDataList = () => {
  // console.log(window, 'window');
  rootElements.value = bpmnInstances().modeler.getDefinitions().rootElements;
  messageIdMap.value = {};
  signalIdMap.value = {};
  messageList.value = [];
  signalList.value = [];
  rootElements.value.forEach((el: any) => {
    if (el.$type === 'bpmn:Message') {
      messageIdMap.value[el.id] = true;
      messageList.value.push({ ...el });
    }
    if (el.$type === 'bpmn:Signal') {
      signalIdMap.value[el.id] = true;
      signalList.value.push({ ...el });
    }
  });
};
const openModel = (type: any) => {
  modelType.value = type;
  modelObjectForm.value = {};
  dialogVisible.value = true;
};
const addNewObject = () => {
  if (modelType.value === 'message') {
    if (messageIdMap.value[modelObjectForm.value.id]) {
      message.error('该消息已存在，请修改id后重新保存');
    }
    const messageRef = bpmnInstances().moddle.create(
      'bpmn:Message',
      modelObjectForm.value,
    );
    rootElements.value.push(messageRef);
  } else {
    if (signalIdMap.value[modelObjectForm.value.id]) {
      message.error('该信号已存在，请修改id后重新保存');
    }
    const signalRef = bpmnInstances().moddle.create(
      'bpmn:Signal',
      modelObjectForm.value,
    );
    rootElements.value.push(signalRef);
  }
  dialogVisible.value = false;
  initDataList();
};

onMounted(() => {
  initDataList();
});
</script>
<template>
  <div class="panel-tab__content">
    <div class="panel-tab__content--title">
      <span class="flex items-center">
        <IconifyIcon icon="ep:menu" class="mr-2 text-gray-600" />
        消息列表
      </span>
      <Button type="primary" title="创建新消息" @click="openModel('message')">
        <template #icon>
          <IconifyIcon icon="ep:plus" />
        </template>
        创建新消息
      </Button>
    </div>
    <Table :data-source="messageList" size="small" bordered>
      <TableColumn title="序号" width="60px">
        <template #default="{ index }">
          {{ index + 1 }}
        </template>
      </TableColumn>
      <TableColumn title="消息ID" data-index="id" />
      <TableColumn title="消息名称" data-index="name" />
    </Table>
    <div class="panel-tab__content--title mt-2 border-t border-gray-200 pt-2">
      <span class="flex items-center">
        <IconifyIcon icon="ep:menu" class="mr-2 text-gray-600" />
        信号列表
      </span>
      <Button type="primary" title="创建新信号" @click="openModel('signal')">
        <template #icon>
          <IconifyIcon icon="ep:plus" />
        </template>
        创建新信号
      </Button>
    </div>
    <Table :data-source="signalList" size="small" bordered>
      <TableColumn title="序号" width="60px">
        <template #default="{ index }">
          {{ index + 1 }}
        </template>
      </TableColumn>
      <TableColumn title="信号ID" data-index="id" />
      <TableColumn title="信号名称" data-index="name" />
    </Table>

    <Modal
      v-model:open="dialogVisible"
      :title="modelConfig.title"
      :mask-closable="false"
      width="400px"
      :destroy-on-close="true"
    >
      <Form :model="modelObjectForm">
        <FormItem :label="modelConfig.idLabel">
          <Input v-model:value="modelObjectForm.id" allow-clear />
        </FormItem>
        <FormItem :label="modelConfig.nameLabel">
          <Input v-model:value="modelObjectForm.name" allow-clear />
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="dialogVisible = false">取 消</Button>
        <Button type="primary" @click="addNewObject">保 存</Button>
      </template>
    </Modal>
  </div>
</template>
