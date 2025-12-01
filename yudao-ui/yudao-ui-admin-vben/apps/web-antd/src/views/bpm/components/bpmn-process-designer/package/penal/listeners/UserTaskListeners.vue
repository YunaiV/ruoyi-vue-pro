<script lang="ts" setup>
import { inject, nextTick, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import {
  Button,
  Divider,
  Drawer,
  Form,
  FormItem,
  Input,
  Modal,
  Select,
  SelectOption,
  Table,
  TableColumn,
} from 'ant-design-vue';

import ProcessListenerDialog from '#/views/bpm/components/bpmn-process-designer/package/penal/listeners/ProcessListenerDialog.vue';

import { createListenerObject, updateElementExtensions } from '../../utils';
import {
  eventType,
  fieldType,
  initListenerForm,
  initListenerForm2,
  initListenerType,
  listenerType,
} from './utilSelf';

defineOptions({ name: 'UserTaskListeners' });

const props = defineProps<Props>();

interface Props {
  id?: string;
  type?: string;
}

const prefix = inject<string>('prefix');
const width = inject<number>('width');
const elementListenersList = ref<any[]>([]);
const listenerEventTypeObject = ref(eventType);
const listenerTypeObject = ref(listenerType);
const listenerFormModelVisible = ref(false);
const listenerForm = ref<any>({});
const fieldTypeObject = ref(fieldType);
const fieldsListOfListener = ref<any[]>([]);
const listenerFieldFormModelVisible = ref(false); // 监听器 注入字段表单弹窗 显示状态
const editingListenerIndex = ref(-1); // 监听器所在下标，-1 为新增
const editingListenerFieldIndex = ref<any>(-1); // 字段所在下标，-1 为新增
const listenerFieldForm = ref<any>({}); // 监听器 注入字段 详情表单
const bpmnElement = ref<any>();
const bpmnElementListeners = ref<any[]>([]);
const otherExtensionList = ref<any[]>([]);
const listenerFormRef = ref<any>({});
const listenerFieldFormRef = ref<any>({});

interface BpmnInstances {
  bpmnElement: any;
  [key: string]: any;
}

declare global {
  interface Window {
    bpmnInstances?: BpmnInstances;
  }
}

const bpmnInstances = () => window.bpmnInstances;

const resetListenersList = () => {
  // console.log(
  //   bpmnInstances().bpmnElement,
  //   'window.bpmnInstances.bpmnElementwindow.bpmnInstances.bpmnElementwindow.bpmnInstances.bpmnElementwindow.bpmnInstances.bpmnElementwindow.bpmnInstances.bpmnElementwindow.bpmnInstances.bpmnElement',
  // );
  bpmnElement.value = bpmnInstances()?.bpmnElement;
  otherExtensionList.value = [];
  bpmnElementListeners.value =
    bpmnElement.value.businessObject?.extensionElements?.values.filter(
      (ex: any) => ex.$type === `${prefix}:TaskListener`,
    ) ?? [];
  elementListenersList.value = bpmnElementListeners.value.map((listener) =>
    initListenerType(listener),
  );
};
const openListenerForm = (listener: any, index?: number) => {
  if (listener) {
    listenerForm.value = initListenerForm(listener);
    editingListenerIndex.value = index || -1;
  } else {
    listenerForm.value = {};
    editingListenerIndex.value = -1; // 标记为新增
  }
  if (listener && listener.fields) {
    fieldsListOfListener.value = listener.fields.map((field: any) => ({
      ...field,
      fieldType: field.string ? 'string' : 'expression',
    }));
  } else {
    fieldsListOfListener.value = [];
    listenerForm.value.fields = [];
  }
  // 打开侧边栏并清楚验证状态
  listenerFormModelVisible.value = true;
  nextTick(() => {
    if (listenerFormRef.value) listenerFormRef.value.clearValidate();
  });
};
// 移除监听器
const removeListener = (_: any, index: number) => {
  // console.log(listener, 'listener');
  Modal.confirm({
    title: '提示',
    content: '确认移除该监听器吗？',
    okText: '确 认',
    cancelText: '取 消',
    onOk() {
      bpmnElementListeners.value.splice(index, 1);
      elementListenersList.value.splice(index, 1);
      updateElementExtensions(bpmnElement.value, [
        ...otherExtensionList.value,
        ...bpmnElementListeners.value,
      ]);
    },
    onCancel() {
      // console.info('操作取消');
    },
  });
};
// 保存监听器
const saveListenerConfig = async () => {
  const validateStatus = await listenerFormRef.value.validate();
  if (!validateStatus) return; // 验证不通过直接返回
  const listenerObject = createListenerObject(listenerForm.value, true, prefix);
  if (editingListenerIndex.value === -1) {
    bpmnElementListeners.value.push(listenerObject);
    elementListenersList.value.push(listenerForm.value);
  } else {
    bpmnElementListeners.value.splice(
      editingListenerIndex.value,
      1,
      listenerObject,
    );
    elementListenersList.value.splice(
      editingListenerIndex.value,
      1,
      listenerForm.value,
    );
  }
  // 保存其他配置
  otherExtensionList.value =
    bpmnElement.value.businessObject?.extensionElements?.values?.filter(
      (ex: any) => ex.$type !== `${prefix}:TaskListener`,
    ) ?? [];
  updateElementExtensions(bpmnElement.value, [
    ...otherExtensionList.value,
    ...bpmnElementListeners.value,
  ]);
  // 4. 隐藏侧边栏
  listenerFormModelVisible.value = false;
  listenerForm.value = {};
};

// 打开监听器字段编辑弹窗
const openListenerFieldForm = (field: any, index?: number) => {
  listenerFieldForm.value = field ? cloneDeep(field) : {};
  editingListenerFieldIndex.value = field ? index : -1;
  listenerFieldFormModelVisible.value = true;
  nextTick(() => {
    if (listenerFieldFormRef.value) listenerFieldFormRef.value.clearValidate();
  });
};
// 保存监听器注入字段
const saveListenerFiled = async () => {
  const validateStatus = await listenerFieldFormRef.value.validate();
  if (!validateStatus) return; // 验证不通过直接返回
  if (editingListenerFieldIndex.value === -1) {
    fieldsListOfListener.value.push(listenerFieldForm.value);
    listenerForm.value.fields.push(listenerFieldForm.value);
  } else {
    fieldsListOfListener.value.splice(
      editingListenerFieldIndex.value,
      1,
      listenerFieldForm.value,
    );
    listenerForm.value.fields.splice(
      editingListenerFieldIndex.value,
      1,
      listenerFieldForm.value,
    );
  }
  listenerFieldFormModelVisible.value = false;
  nextTick(() => {
    listenerFieldForm.value = {};
  });
};
// 移除监听器字段
const removeListenerField = (_: any, index: number) => {
  // console.log(field, 'field');
  Modal.confirm({
    title: '提示',
    content: '确认移除该字段吗？',
    okText: '确 认',
    cancelText: '取 消',
    onOk() {
      fieldsListOfListener.value.splice(index, 1);
      listenerForm.value.fields.splice(index, 1);
    },
    onCancel() {
      // console.info('操作取消');
    },
  });
};

// 打开监听器弹窗
const processListenerDialogRef = ref<any>();
const openProcessListenerDialog = async () => {
  processListenerDialogRef.value.open('task');
};
const selectProcessListener = (listener: any) => {
  const listenerForm = initListenerForm2(listener);
  const listenerObject = createListenerObject(listenerForm, true, prefix);
  bpmnElementListeners.value.push(listenerObject);
  elementListenersList.value.push(listenerForm);

  // 保存其他配置
  otherExtensionList.value =
    bpmnElement.value.businessObject?.extensionElements?.values?.filter(
      (ex: any) => ex.$type !== `${prefix}:TaskListener`,
    ) ?? [];
  updateElementExtensions(
    bpmnElement.value,
    otherExtensionList.value?.concat(bpmnElementListeners.value),
  );
};

watch(
  () => props.id,
  (val) => {
    val &&
      val.length > 0 &&
      nextTick(() => {
        resetListenersList();
      });
  },
  { immediate: true },
);
</script>
<template>
  <div class="panel-tab__content">
    <Table :data="elementListenersList" size="small" bordered>
      <TableColumn title="序号" width="50px" type="index" />
      <TableColumn
        title="事件类型"
        width="80px"
        :ellipsis="{ showTitle: true }"
        :custom-render="
          ({ record }: any) =>
            listenerEventTypeObject[record.event as keyof typeof eventType]
        "
      />
      <TableColumn
        title="事件id"
        width="80px"
        data-index="id"
        :ellipsis="{ showTitle: true }"
      />
      <TableColumn
        title="监听器类型"
        width="80px"
        :ellipsis="{ showTitle: true }"
        :custom-render="
          ({ record }: any) =>
            listenerTypeObject[record.listenerType as keyof typeof listenerType]
        "
      />
      <TableColumn title="操作" width="90px">
        <template #default="{ record, index }">
          <Button
            size="small"
            type="link"
            @click="openListenerForm(record, index)"
          >
            编辑
          </Button>
          <Divider type="vertical" />
          <Button
            size="small"
            type="link"
            danger
            @click="removeListener(record, index)"
          >
            移除
          </Button>
        </template>
      </TableColumn>
    </Table>
    <div class="element-drawer__button">
      <Button size="small" type="primary" @click="openListenerForm(null)">
        <template #icon> <IconifyIcon icon="ep:plus" /></template>
        添加监听器
      </Button>
      <Button size="small" @click="openProcessListenerDialog">
        <template #icon> <IconifyIcon icon="ep:select" /></template>
        选择监听器
      </Button>
    </div>

    <!-- 监听器 编辑/创建 部分 -->
    <Drawer
      v-model:open="listenerFormModelVisible"
      title="任务监听器"
      :width="width"
      :destroy-on-close="true"
    >
      <Form :model="listenerForm" ref="listenerFormRef">
        <FormItem
          label="事件类型"
          name="event"
          :rules="[{ required: true, message: '请选择事件类型' }]"
        >
          <Select v-model:value="listenerForm.event">
            <SelectOption
              v-for="i in Object.keys(listenerEventTypeObject)"
              :key="i"
              :value="i"
            >
              {{ listenerEventTypeObject[i as keyof typeof eventType] }}
            </SelectOption>
          </Select>
        </FormItem>
        <FormItem
          label="监听器ID"
          name="id"
          :rules="[{ required: true, message: '请输入监听器ID' }]"
        >
          <Input v-model:value="listenerForm.id" allow-clear />
        </FormItem>
        <FormItem
          label="监听器类型"
          name="listenerType"
          :rules="[{ required: true, message: '请选择监听器类型' }]"
        >
          <Select v-model:value="listenerForm.listenerType">
            <SelectOption
              v-for="i in Object.keys(listenerTypeObject)"
              :key="i"
              :value="i"
            >
              {{ listenerTypeObject[i as keyof typeof listenerType] }}
            </SelectOption>
          </Select>
        </FormItem>
        <FormItem
          v-if="listenerForm.listenerType === 'classListener'"
          label="Java类"
          name="class"
          key="listener-class"
          :rules="[{ required: true, message: '请输入Java类' }]"
        >
          <Input v-model:value="listenerForm.class" allow-clear />
        </FormItem>
        <FormItem
          v-if="listenerForm.listenerType === 'expressionListener'"
          label="表达式"
          name="expression"
          key="listener-expression"
          :rules="[{ required: true, message: '请输入表达式' }]"
        >
          <Input v-model:value="listenerForm.expression" allow-clear />
        </FormItem>
        <FormItem
          v-if="listenerForm.listenerType === 'delegateExpressionListener'"
          label="代理表达式"
          name="delegateExpression"
          key="listener-delegate"
          :rules="[{ required: true, message: '请输入代理表达式' }]"
        >
          <Input v-model:value="listenerForm.delegateExpression" allow-clear />
        </FormItem>
        <template v-if="listenerForm.listenerType === 'scriptListener'">
          <FormItem
            label="脚本格式"
            name="scriptFormat"
            key="listener-script-format"
            :rules="[{ required: true, message: '请填写脚本格式' }]"
          >
            <Input v-model:value="listenerForm.scriptFormat" allow-clear />
          </FormItem>
          <FormItem
            label="脚本类型"
            name="scriptType"
            key="listener-script-type"
            :rules="[{ required: true, message: '请选择脚本类型' }]"
          >
            <Select v-model:value="listenerForm.scriptType">
              <SelectOption value="inlineScript">内联脚本</SelectOption>
              <SelectOption value="externalScript">外部脚本</SelectOption>
            </Select>
          </FormItem>
          <FormItem
            v-if="listenerForm.scriptType === 'inlineScript'"
            label="脚本内容"
            name="value"
            key="listener-script"
            :rules="[{ required: true, message: '请填写脚本内容' }]"
          >
            <Input v-model:value="listenerForm.value" allow-clear />
          </FormItem>
          <FormItem
            v-if="listenerForm.scriptType === 'externalScript'"
            label="资源地址"
            name="resource"
            key="listener-resource"
            :rules="[{ required: true, message: '请填写资源地址' }]"
          >
            <Input v-model:value="listenerForm.resource" allow-clear />
          </FormItem>
        </template>

        <template v-if="listenerForm.event === 'timeout'">
          <FormItem
            label="定时器类型"
            name="eventDefinitionType"
            key="eventDefinitionType"
          >
            <Select v-model:value="listenerForm.eventDefinitionType">
              <SelectOption value="date">日期</SelectOption>
              <SelectOption value="duration">持续时长</SelectOption>
              <SelectOption value="cycle">循环</SelectOption>
              <SelectOption value="null">无</SelectOption>
            </Select>
          </FormItem>
          <FormItem
            v-if="
              !!listenerForm.eventDefinitionType &&
              listenerForm.eventDefinitionType !== 'null'
            "
            label="定时器"
            name="eventTimeDefinitions"
            key="eventTimeDefinitions"
            :rules="[{ required: true, message: '请填写定时器配置' }]"
          >
            <Input
              v-model:value="listenerForm.eventTimeDefinitions"
              allow-clear
            />
          </FormItem>
        </template>
      </Form>

      <Divider />
      <div class="mb-2 flex justify-between">
        <span class="flex items-center">
          <IconifyIcon icon="ep:menu" class="mr-2 text-gray-600" />
          注入字段
        </span>
        <Button
          type="primary"
          title="添加字段"
          @click="openListenerFieldForm(null)"
        >
          <template #icon>
            <IconifyIcon icon="ep:plus" />
          </template>
          添加字段
        </Button>
      </div>
      <Table
        :data="fieldsListOfListener"
        size="small"
        :scroll="{ y: 240 }"
        bordered
        style="flex: none"
      >
        <TableColumn title="序号" width="50px" type="index" />
        <TableColumn title="字段名称" width="100px" data-index="name" />
        <TableColumn
          title="字段类型"
          width="80px"
          :ellipsis="{ showTitle: true }"
          :custom-render="
            ({ record }: any) =>
              fieldTypeObject[record.fieldType as keyof typeof fieldType]
          "
        />
        <TableColumn
          title="字段值/表达式"
          width="100px"
          :ellipsis="{ showTitle: true }"
          :custom-render="
            ({ record }: any) => record.string || record.expression
          "
        />
        <TableColumn title="操作" width="100px">
          <template #default="{ record, index }">
            <Button
              size="small"
              type="link"
              @click="openListenerFieldForm(record, index)"
            >
              编辑
            </Button>
            <Divider type="vertical" />
            <Button
              size="small"
              type="link"
              danger
              @click="removeListenerField(record, index)"
            >
              移除
            </Button>
          </template>
        </TableColumn>
      </Table>

      <div class="element-drawer__button">
        <Button size="small" @click="listenerFormModelVisible = false">
          取 消
        </Button>
        <Button size="small" type="primary" @click="saveListenerConfig">
          保 存
        </Button>
      </div>
    </Drawer>

    <!-- 注入字段 编辑/创建 部分 -->
    <Modal
      title="字段配置"
      v-model:open="listenerFieldFormModelVisible"
      :width="600"
      :destroy-on-close="true"
    >
      <Form :model="listenerFieldForm" ref="listenerFieldFormRef">
        <FormItem
          label="字段名称："
          name="name"
          :rules="[{ required: true, message: '请输入字段名称' }]"
        >
          <Input v-model:value="listenerFieldForm.name" allow-clear />
        </FormItem>
        <FormItem
          label="字段类型："
          name="fieldType"
          :rules="[{ required: true, message: '请选择字段类型' }]"
        >
          <Select v-model:value="listenerFieldForm.fieldType">
            <SelectOption
              v-for="i in Object.keys(fieldTypeObject)"
              :key="i"
              :value="i"
            >
              {{ fieldTypeObject[i as keyof typeof fieldType] }}
            </SelectOption>
          </Select>
        </FormItem>
        <FormItem
          v-if="listenerFieldForm.fieldType === 'string'"
          label="字段值："
          name="string"
          key="field-string"
          :rules="[{ required: true, message: '请输入字段值' }]"
        >
          <Input v-model:value="listenerFieldForm.string" allow-clear />
        </FormItem>
        <FormItem
          v-if="listenerFieldForm.fieldType === 'expression'"
          label="表达式："
          name="expression"
          key="field-expression"
          :rules="[{ required: true, message: '请输入表达式' }]"
        >
          <Input v-model:value="listenerFieldForm.expression" allow-clear />
        </FormItem>
      </Form>
      <template #footer>
        <Button size="small" @click="listenerFieldFormModelVisible = false">
          取 消
        </Button>
        <Button size="small" type="primary" @click="saveListenerFiled">
          确 定
        </Button>
      </template>
    </Modal>
  </div>

  <!-- 选择弹窗 -->
  <ProcessListenerDialog
    ref="processListenerDialogRef"
    @select="selectProcessListener"
  />
</template>
